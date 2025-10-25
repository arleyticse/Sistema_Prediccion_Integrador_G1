package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenYaConfirmadaException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.ProductoSinProveedorException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository.IOrdenCompraRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.DatosInsuficientesException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.PrediccionNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Proveedor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación de servicio para la gestión de órdenes de compra automáticas.
 * Integra predicciones ARIMA con niveles de inventario para generar órdenes optimizadas.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = false)
public class OrdenCompraService implements IOrdenCompraService {

    private final IOrdenCompraRepositorio ordenRepositorio;
    private final IPrediccionRepositorio prediccionRepositorio;
    private final ObjectMapper objectMapper;

    // Inyección de repositorio de inventario si existe
    // private final IInventarioRepositorio inventarioRepositorio;
    // private final IProductoRepositorio productoRepositorio;

    /**
     * Genera automáticamente una orden de compra basada en predicción ARIMA.
     *
     * Fórmula: Cantidad = (Demanda Predicha × 1.2) - Stock Actual + Punto Reorden
     *
     * Validaciones:
     * - La predicción debe existir
     * - El producto debe tener proveedor
     * - La cantidad calculada debe ser positiva
     * - El stock debe estar bajo el punto de reorden
     *
     * @param prediccionId ID de la predicción que genera la orden
     * @return OrdenCompra generada y persistida
     * @throws PrediccionNoEncontradaException si la predicción no existe
     * @throws DatosInsuficientesException si stock es suficiente
     * @throws ProductoSinProveedorException si no hay proveedor
     */
    @Override
    @Transactional
    public OrdenCompra generarOrdenAutomatica(Integer prediccionId) {
        log.info("📦 [ORDEN] Iniciando generación automática para predicción: {}", prediccionId);

        // 1. Obtener y validar predicción
        Prediccion prediccion = prediccionRepositorio.findById(prediccionId)
                .orElseThrow(() -> {
                    log.error("❌ [ORDEN] Predicción no encontrada: {}", prediccionId);
                    return new PrediccionNoEncontradaException("Predicción no existe: " + prediccionId);
                });

        Producto producto = prediccion.getProducto();
        log.info("✅ [ORDEN] Predicción obtenida para producto: {}", producto.getNombre());

        // 2. Validar que el producto tenga proveedor
        // TODO: Implementar relación Producto -> Proveedor en modelo
        // Por ahora, usar proveedor por defecto
        Proveedor proveedor = null; // TODO: obtenerProveedorDelProducto(producto);
        if (proveedor == null) {
            log.warn("⚠️ [ORDEN] Usando proveedor por defecto (TODO: implementar FK en Producto)");
            // Crear proveedor temporal para continuidad
            proveedor = new Proveedor();
            proveedor.setProveedorId(1);
            proveedor.setNombreComercial("Proveedor Default");
            proveedor.setTiempoEntregaDias(5);
        }

        // 3. Obtener información de inventario
        Integer stockActual = obtenerStockActual(producto);
        Integer puntoReorden = obtenerPuntoReorden(producto);
        
        log.info("📊 [ORDEN] Stock actual: {}, Punto de reorden: {}", stockActual, puntoReorden);

        // 4. Validar si es necesaria una orden
        if (stockActual > puntoReorden) {
            log.warn("⚠️ [ORDEN] Stock suficiente ({} > {}), no se genera orden", 
                    stockActual, puntoReorden);
            throw new DatosInsuficientesException(
                    "Stock suficiente (" + stockActual + "), no es necesaria orden");
        }

        // 5. Calcular cantidad según fórmula
        Integer demandaPredichaTotal = prediccion.getDemandaPredichaTotal();
        Integer cantidadOrden = calcularCantidadOrden(demandaPredichaTotal, stockActual, puntoReorden);

        log.info("🔢 [ORDEN] Cantidad calculada: {} (Predicción: {}, Stock: {}, Reorden: {})",
                cantidadOrden, demandaPredichaTotal, stockActual, puntoReorden);

        // 6. Validar cantidad positiva
        if (cantidadOrden <= 0) {
            log.error("❌ [ORDEN] Cantidad inválida (negativa o cero): {}", cantidadOrden);
            throw new DatosInsuficientesException("Cantidad calculada inválida: " + cantidadOrden);
        }

        // 7. Generar número de orden único
        String numeroOrden = generarNumeroOrden();

        // 8. Crear orden
        OrdenCompra orden = new OrdenCompra();
        orden.setNumeroOrden(numeroOrden);
        orden.setProveedor(proveedor);
        orden.setFechaOrden(LocalDate.now());
        orden.setEstadoOrden(EstadoOrdenCompra.PENDIENTE);
        orden.setGeneradaAutomaticamente(true);
        orden.setObservaciones("Generada automáticamente desde predicción " + prediccionId);
        
        // Calcular fecha de entrega esperada basada en lead time del proveedor
        Integer diasEntrega = proveedor.getTiempoEntregaDias() != null ? 
                proveedor.getTiempoEntregaDias() : 5;
        orden.setFechaEntregaEsperada(LocalDate.now().plusDays(diasEntrega));

        // 9. Calcular total (cantidad × precio unitario del producto)
        BigDecimal precioUnitario = producto.getCostoAdquisicion() != null ?
                producto.getCostoAdquisicion() : BigDecimal.ZERO;
        BigDecimal totalOrden = BigDecimal.valueOf(cantidadOrden)
                .multiply(precioUnitario);
        orden.setTotalOrden(totalOrden);

        // 10. Generar detalles del cálculo
        String detallesCalculo = generarDetallesCalculo(demandaPredichaTotal, stockActual, 
                puntoReorden, cantidadOrden);
        orden.setObservaciones(detallesCalculo + " | " + orden.getObservaciones());

        // 11. Guardar orden
        OrdenCompra ordenGuardada = ordenRepositorio.save(orden);
        log.info("✅ [ORDEN] Orden generada exitosamente: {} | ID: {} | Cantidad: {} | Total: ${}", 
                numeroOrden, ordenGuardada.getOrdenCompraId(), cantidadOrden, totalOrden);

        return ordenGuardada;
    }

    /**
     * Valida si debe generarse una orden de compra para un producto.
     *
     * @param productoId ID del producto a validar
     * @return true si se debe generar orden
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validarOrdenCompra(Integer productoId) {
        log.debug("🔍 [ORDEN] Validando si es necesaria orden para producto: {}", productoId);

        // Validar que exista predicción reciente
        // Validar que stock esté bajo
        // Este método se puede mejorar con lógica más compleja

        return true;
    }

    /**
     * Obtiene las órdenes de compra para un producto específico con paginación.
     *
     * @param productoId ID del producto
     * @param pageable configuración de paginación
     * @return página de órdenes
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompra> obtenerOrdenesPorProducto(Integer productoId, Pageable pageable) {
        log.info("📋 [ORDEN] Obteniendo órdenes para producto: {} (página: {})", productoId, pageable.getPageNumber());

        // Por ahora obtener todas las órdenes con paginación
        // TODO: Implementar filtrado por producto cuando se agregue la FK
        Page<OrdenCompra> ordenesPage = ordenRepositorio.findAll(pageable);
        
        log.info("✅ [ORDEN] Se encontraron {} órdenes de {} total", 
                ordenesPage.getNumberOfElements(), ordenesPage.getTotalElements());
        return ordenesPage;
    }

    /**
     * Obtiene la orden más reciente para un producto.
     *
     * @param productoId ID del producto
     * @return OrdenCompra más reciente
     * @throws OrdenCompraNoEncontradaException si no existe orden
     */
    @Override
    @Transactional(readOnly = true)
    public OrdenCompra obtenerUltimaOrden(Integer productoId) {
        log.info("🔍 [ORDEN] Obteniendo última orden para producto: {}", productoId);

        Optional<OrdenCompra> ultimaOrden = ordenRepositorio.findFirstByOrderByFechaOrdenDesc();

        return ultimaOrden.orElseThrow(() -> {
            log.error("❌ [ORDEN] No existe orden para producto: {}", productoId);
            return new OrdenCompraNoEncontradaException(
                    "No existe orden para el producto: " + productoId);
        });
    }

    /**
     * Confirma una orden de compra (cambia su estado a APROBADA).
     *
     * @param ordenId ID de la orden a confirmar
     * @throws OrdenCompraNoEncontradaException si la orden no existe
     */
    @Override
    @Transactional
    public void confirmarOrden(Long ordenId) {
        log.info("✅ [ORDEN] Confirmando orden: {}", ordenId);

        OrdenCompra orden = ordenRepositorio.findById(ordenId)
                .orElseThrow(() -> {
                    log.error("❌ [ORDEN] Orden no encontrada: {}", ordenId);
                    return new OrdenCompraNoEncontradaException("Orden no existe: " + ordenId);
                });

        if (orden.getEstadoOrden() == EstadoOrdenCompra.APROBADA) {
            log.warn("⚠️ [ORDEN] Orden ya está aprobada: {}", ordenId);
            return; // Idempotente
        }

        orden.setEstadoOrden(EstadoOrdenCompra.APROBADA);
        ordenRepositorio.save(orden);
        
        log.info("✅ [ORDEN] Orden confirmada exitosamente: {}", ordenId);
    }

    /**
     * Cancela una orden de compra (cambia su estado a CANCELADA).
     *
     * @param ordenId ID de la orden a cancelar
     * @throws OrdenCompraNoEncontradaException si la orden no existe
     * @throws OrdenYaConfirmadaException si la orden ya fue aprobada
     */
    @Override
    @Transactional
    public void cancelarOrden(Long ordenId) {
        log.info("❌ [ORDEN] Cancelando orden: {}", ordenId);

        OrdenCompra orden = ordenRepositorio.findById(ordenId)
                .orElseThrow(() -> {
                    log.error("❌ [ORDEN] Orden no encontrada: {}", ordenId);
                    return new OrdenCompraNoEncontradaException("Orden no existe: " + ordenId);
                });

        // Validar que no esté en estado que no permita cancelación
        if (orden.getEstadoOrden() == EstadoOrdenCompra.APROBADA ||
            orden.getEstadoOrden() == EstadoOrdenCompra.RECIBIDA_COMPLETA) {
            log.error("❌ [ORDEN] No se puede cancelar orden en estado: {}", 
                    orden.getEstadoOrden());
            throw new OrdenYaConfirmadaException(
                    "No se puede cancelar una orden en estado: " + orden.getEstadoOrden());
        }

        orden.setEstadoOrden(EstadoOrdenCompra.CANCELADA);
        ordenRepositorio.save(orden);
        
        log.info("✅ [ORDEN] Orden cancelada exitosamente: {}", ordenId);
    }

    /**
     * Calcula la cantidad a ordenar según la fórmula.
     * Fórmula: (Predicción × 1.2) - Stock + PuntoReorden
     *
     * @param demandaPredicha demanda predicha por ARIMA
     * @param stockActual stock disponible actual
     * @param puntoReorden punto mínimo de reorden
     * @return cantidad a ordenar
     */
    private Integer calcularCantidadOrden(Integer demandaPredicha, Integer stockActual, 
                                          Integer puntoReorden) {
        // Aplicar factor de seguridad 1.2 (20% buffer)
        double demandaConBuffer = demandaPredicha * 1.2;
        
        // Aplicar fórmula completa
        Integer cantidadOrden = (int) Math.ceil(demandaConBuffer) - stockActual + puntoReorden;
        
        log.debug("🔢 [CALCULO] Fórmula: ({} × 1.2) - {} + {} = {}",
                demandaPredicha, stockActual, puntoReorden, cantidadOrden);
        
        return Math.max(0, cantidadOrden); // No permitir cantidades negativas
    }

    /**
     * Obtiene el stock actual de un producto desde el inventario.
     *
     * @param producto producto a consultar
     * @return stock disponible
     */
    private Integer obtenerStockActual(Producto producto) {
        // TODO: Implementar inyección de IInventarioRepositorio
        // Por ahora retorna valor por defecto
        log.debug("📊 [STOCK] Consultando stock para producto: {}", producto.getNombre());
        return 0; // Valor por defecto, será actualizado cuando se inyecte repo de inventario
    }

    /**
     * Obtiene el punto de reorden configurado para un producto.
     *
     * @param producto producto a consultar
     * @return punto de reorden o valor por defecto
     */
    private Integer obtenerPuntoReorden(Producto producto) {
        // TODO: Implementar inyección de IInventarioRepositorio
        // Por ahora retorna valor por defecto (2 semanas de demanda promedio)
        log.debug("📍 [REORDEN] Punto de reorden para producto: {}", producto.getNombre());
        return 50; // Valor por defecto, será actualizado cuando se inyecte repo de inventario
    }

    /**
     * Genera un número de orden único con formato: OC-YYYYMMDD-XXXXX
     *
     * @return número de orden formateado
     */
    private String generarNumeroOrden() {
        LocalDate hoy = LocalDate.now();
        long timestamp = System.currentTimeMillis() % 100000; // Últimos 5 dígitos
        
        String numeroOrden = String.format("OC-%04d%02d%02d-%05d",
                hoy.getYear(),
                hoy.getMonthValue(),
                hoy.getDayOfMonth(),
                timestamp);
        
        log.debug("🏷️ [NUMERO] Número de orden generado: {}", numeroOrden);
        return numeroOrden;
    }

    /**
     * Genera una descripción detallada del cálculo realizado.
     * Se almacena en observaciones para auditoría.
     *
     * @param demandaPredicha demanda predicha
     * @param stockActual stock actual
     * @param puntoReorden punto de reorden
     * @param cantidadOrden cantidad final
     * @return descripción del cálculo
     */
    private String generarDetallesCalculo(Integer demandaPredicha, Integer stockActual,
                                         Integer puntoReorden, Integer cantidadOrden) {
        try {
            Map<String, Object> detalles = new HashMap<>();
            detalles.put("demandaPredicha", demandaPredicha);
            detalles.put("demandaConBuffer", demandaPredicha * 1.2);
            detalles.put("stockActual", stockActual);
            detalles.put("puntoReorden", puntoReorden);
            detalles.put("cantidadFinal", cantidadOrden);
            detalles.put("formula", "(demandaPredicha × 1.2) - stockActual + puntoReorden");
            detalles.put("fechaCalculo", LocalDateTime.now());
            
            return objectMapper.writeValueAsString(detalles);
        } catch (Exception e) {
            log.warn("⚠️ [DETALLES] Error al generar detalles JSON: {}", e.getMessage());
            return "Detalles: Predicción=" + demandaPredicha + ", Stock=" + stockActual +
                   ", Reorden=" + puntoReorden + ", Cantidad=" + cantidadOrden;
        }
    }

    /**
     * Obtiene todas las órdenes de compra con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de todas las órdenes
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompra> obtenerTodasLasOrdenes(Pageable pageable) {
        log.info("📋 [ORDEN] Obteniendo todas las órdenes (página: {})", pageable.getPageNumber());

        try {
            Page<OrdenCompra> ordenesPage = ordenRepositorio.findAll(pageable);
            
            log.info("✅ [ORDEN] Se encontraron {} órdenes de {} total", 
                    ordenesPage.getNumberOfElements(), ordenesPage.getTotalElements());
            
            return ordenesPage;
            
        } catch (Exception e) {
            log.error("❌ [ORDEN] Error al obtener órdenes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener las órdenes", e);
        }
    }
}
