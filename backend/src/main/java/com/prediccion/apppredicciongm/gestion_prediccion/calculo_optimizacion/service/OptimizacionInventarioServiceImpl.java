package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalcularOptimizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.OptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.mapper.CalculoObtimizacionMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.repository.ICalculoObtimizacionRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.CalculoObtimizacion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de optimización de inventario usando modelos EOQ y ROP.
 * 
 * Implementa:
 * - EOQ (Economic Order Quantity): Cantidad óptima de pedido
 * - ROP (Reorder Point): Punto de reorden
 * - Stock de Seguridad: Protección contra variabilidad
 * 
 * Integrado con predicción ML para demanda futura.
 * 
 * @author Sistema de Predicción Unificado
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizacionInventarioServiceImpl implements IOptimizacionInventarioService {
    
    private final ICalculoObtimizacionRepositorio calculoRepository;
    private final IProductoRepositorio productoRepository;
    private final IKardexRepositorio kardexRepository;
    private final CalculoObtimizacionMapper calculoMapper;
    
    // Constantes para nivel de servicio
    private static final double NIVEL_SERVICIO_95 = 0.95;
    private static final double FACTOR_Z_95 = 1.65; // Factor Z para 95% de confianza
    
    // Valores por defecto
    private static final double COSTO_PEDIDO_DEFAULT = 50.0;
    private static final double PORCENTAJE_MANTENIMIENTO_DEFAULT = 0.25; // 25% del costo unitario
    private static final int LEAD_TIME_DEFAULT = 7; // 7 días por defecto
    
    /**
     * Calcula EOQ y ROP desde una predicción ML.
     * 
     * Este es el método principal que integra predicción ML con optimización.
     * Por defecto persiste el resultado en BD.
     * 
     * @param prediccion Resultado de predicción ML con demanda estimada
     * @param productoId ID del producto
     * @return Cálculo de optimización con EOQ y ROP
     */
    @Override
    @Transactional
    public CalculoOptimizacionResponse calcularEOQROPDesdePrediccion(
            SmartPrediccionResponse prediccion,
            Long productoId) {
        return calcularEOQROPDesdePrediccion(prediccion, productoId, true);
    }
    
    /**
     * Calcula EOQ y ROP desde una predicción ML con opción de no guardar.
     * 
     * @param prediccion Resultado de predicción ML con demanda estimada
     * @param productoId ID del producto
     * @param persistir Si true, guarda en BD; si false, solo retorna el cálculo sin persistir
     * @return Cálculo de optimización con EOQ y ROP
     */
    @Override
    @Transactional
    public CalculoOptimizacionResponse calcularEOQROPDesdePrediccion(
            SmartPrediccionResponse prediccion,
            Long productoId,
            boolean persistir) {
        
        log.info("[OPTIMIZACION] Calculando EOQ/ROP para producto {} (persistir: {})", productoId, persistir);
        
        // 1. Obtener producto
        Producto producto = productoRepository.findById(Math.toIntExact(productoId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado: " + productoId));
        
        // 2. Extraer parámetros del producto
        double costoUnitario = obtenerCostoUnitario(producto);
        double costoPedido = obtenerCostoPedido(producto);
        double costoMantenimientoAnual = obtenerCostoMantenimiento(producto, costoUnitario);
        int leadTimeDias = obtenerLeadTime(producto);
        
        // 3. Calcular demanda anual desde predicción
        double demandaAnualEstimada = calcularDemandaAnual(prediccion);
        double demandaDiaria = demandaAnualEstimada / 365.0;
        
        log.info("[OPTIMIZACION] Demanda anual estimada: {}, Diaria: {:.2f}", 
                demandaAnualEstimada, demandaDiaria);
        
        // 4. Calcular variabilidad de la demanda
        double desviacionDemanda = calcularDesviacionDemanda(productoId);
        
        // 5. Calcular EOQ (Economic Order Quantity)
        int EOQ = calcularEOQ(
                demandaAnualEstimada, 
                costoPedido, 
                costoMantenimientoAnual
        );
        
        // 6. Calcular Stock de Seguridad
        int stockSeguridad = calcularStockSeguridad(
                desviacionDemanda, 
                leadTimeDias, 
                FACTOR_Z_95
        );
        
        // 7. Calcular ROP (Reorder Point)
        int ROP = calcularROP(demandaDiaria, leadTimeDias, stockSeguridad);
        
        // 8. Calcular métricas adicionales
        int numeroOrdenesAnuales = calcularNumeroOrdenes(demandaAnualEstimada, EOQ);
        int diasEntreLotes = calcularDiasEntreLotes(numeroOrdenesAnuales);
        double costoTotalInventario = calcularCostoTotalInventario(
                demandaAnualEstimada, EOQ, costoPedido, costoMantenimientoAnual
        );
        
        // 9. Crear cálculo
        CalculoObtimizacion calculo = CalculoObtimizacion.builder()
                .producto(producto)
                .demandaAnualEstimada((int) Math.ceil(demandaAnualEstimada))
                .eoqCantidadOptima(EOQ)
                .ropPuntoReorden(ROP)
                .stockSeguridadSugerido(stockSeguridad)
                .stockSeguridad(stockSeguridad)
                .numeroOrdenesAnuales(numeroOrdenesAnuales)
                .diasEntreLotes(diasEntreLotes)
                .costoTotalInventario(BigDecimal.valueOf(costoTotalInventario)
                        .setScale(2, RoundingMode.HALF_UP))
                .costoPedido(BigDecimal.valueOf(costoPedido))
                .costoMantenimiento(BigDecimal.valueOf(costoMantenimientoAnual))
                .costoUnitario(BigDecimal.valueOf(costoUnitario))
                .diasLeadTime(leadTimeDias)
                .fechaCalculo(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .observaciones(generarObservaciones(EOQ, ROP, stockSeguridad, NIVEL_SERVICIO_95))
                .build();
        
        // 10. Guardar solo si se requiere persistencia
        if (persistir) {
            calculo = calculoRepository.save(calculo);
            log.info("[OPTIMIZACION] Optimización calculada y guardada: EOQ={}, ROP={}, SS={}", EOQ, ROP, stockSeguridad);
        } else {
            log.debug("[OPTIMIZACION] Optimización calculada (sin persistir): EOQ={}, ROP={}, SS={}", EOQ, ROP, stockSeguridad);
        }
        
        return calculoMapper.toResponse(calculo);
    }
    
    /**
     * Calcula EOQ usando la fórmula de Wilson.
     * 
     * EOQ = √((2 × D × S) / H)
     * 
     * Donde:
     * - D = Demanda anual
     * - S = Costo por pedido
     * - H = Costo de mantenimiento anual por unidad
     * 
     * @param demandaAnual Demanda anual estimada
     * @param costoPedido Costo fijo por realizar un pedido
     * @param costoMantenimiento Costo anual de mantener una unidad
     * @return Cantidad económica de pedido
     */
    private int calcularEOQ(
            double demandaAnual, 
            double costoPedido, 
            double costoMantenimiento) {
        
        if (costoMantenimiento <= 0 || costoPedido <= 0) {
            log.warn("[OPTIMIZACION] Advertencia: Costos inválidos, usando valores por defecto");
            costoMantenimiento = PORCENTAJE_MANTENIMIENTO_DEFAULT * 10; // Estimación
            costoPedido = COSTO_PEDIDO_DEFAULT;
        }
        
        double eoq = Math.sqrt((2 * demandaAnual * costoPedido) / costoMantenimiento);
        
        int eoqRedondeado = (int) Math.ceil(eoq);
        
        log.debug("[OPTIMIZACION] EOQ = √((2 × {} × {}) / {}) = {}", 
                demandaAnual, costoPedido, costoMantenimiento, eoqRedondeado);
        
        return eoqRedondeado;
    }
    
    /**
     * Calcula el punto de reorden (ROP).
     * 
     * ROP = (Demanda diaria × Lead time) + Stock de seguridad
     * 
     * @param demandaDiaria Demanda promedio diaria
     * @param leadTimeDias Tiempo de entrega del proveedor
     * @param stockSeguridad Stock de seguridad calculado
     * @return Punto de reorden
     */
    private int calcularROP(
            double demandaDiaria, 
            int leadTimeDias, 
            int stockSeguridad) {
        
        double demandaDuranteLeadTime = demandaDiaria * leadTimeDias;
        double rop = demandaDuranteLeadTime + stockSeguridad;
        
        int ropRedondeado = (int) Math.ceil(rop);
        
        log.debug("📍 ROP = ({:.2f} × {}) + {} = {}", 
                demandaDiaria, leadTimeDias, stockSeguridad, ropRedondeado);
        
        return ropRedondeado;
    }
    
    /**
     * Calcula el stock de seguridad.
     * 
     * SS = Z × σ × √(Lead time)
     * 
     * Donde:
     * - Z = Factor de servicio (1.65 para 95% de confianza)
     * - σ = Desviación estándar de la demanda diaria
     * - Lead time = Tiempo de entrega en días
     * 
     * @param desviacionDemanda Desviación estándar de la demanda
     * @param leadTimeDias Tiempo de entrega
     * @param factorZ Factor Z para nivel de servicio
     * @return Stock de seguridad
     */
    private int calcularStockSeguridad(
            double desviacionDemanda, 
            int leadTimeDias, 
            double factorZ) {
        
        double stockSeguridad = factorZ * desviacionDemanda * Math.sqrt(leadTimeDias);
        
        int ssRedondeado = (int) Math.ceil(stockSeguridad);
        
        log.debug("🛡️ Stock Seguridad = {:.2f} × {:.2f} × √{} = {}", 
                factorZ, desviacionDemanda, leadTimeDias, ssRedondeado);
        
        return Math.max(ssRedondeado, 1); // Mínimo 1 unidad
    }
    
    /**
     * Calcula la demanda anual desde la predicción ML.
     * 
     * Extrapola la demanda predicha al horizonte anual.
     */
    private double calcularDemandaAnual(SmartPrediccionResponse prediccion) {
        double demandaTotal = prediccion.getDemandaTotalPredicha();
        int horizonteDias = prediccion.getHorizonteTiempo();
        
        // Extrapolar a 365 días
        double demandaAnual = (demandaTotal / horizonteDias) * 365.0;
        
        return demandaAnual;
    }
    
    /**
     * Calcula la desviación estándar de la demanda histórica.
     * 
     * Optimización: Usa query SQL con STDDEV() para calcular directamente
     * en la base de datos, evitando cargar todos los registros en memoria.
     */
    private double calcularDesviacionDemanda(Long productoId) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(180);
        
        try {
            List<Object[]> resultados = kardexRepository.findEstadisticasDemandaByProducto(
                    Math.toIntExact(productoId), fechaInicio);
            
            if (resultados == null || resultados.isEmpty()) {
                log.warn("[OPTIMIZACION] Sin historial de demanda para producto {}, usando desviación por defecto", 
                        productoId);
                return 5.0;
            }
            
            // La query retorna una lista con una única fila: [count, avg, stddev]
            Object[] fila = resultados.get(0);
            
            if (fila == null || fila.length < 3) {
                log.warn("[OPTIMIZACION] Resultado incompleto para producto {}, usando desviación por defecto", 
                        productoId);
                return 5.0;
            }
            
            Number count = fila[0] != null ? (Number) fila[0] : 0;
            Number desviacion = fila[2] != null ? (Number) fila[2] : null;
            
            if (count.longValue() == 0) {
                log.warn("[OPTIMIZACION] Sin historial de demanda para producto {}, usando desviación por defecto", 
                        productoId);
                return 5.0;
            }
            
            double desviacionFinal = desviacion != null ? desviacion.doubleValue() : 5.0;
            
            log.debug("[OPTIMIZACION] Desviación demanda calculada en BD: {} (n={})", 
                    desviacionFinal, count);
            
            return Math.max(desviacionFinal, 1.0);
            
        } catch (Exception e) {
            log.warn("[OPTIMIZACION] Error calculando desviación para producto {}: {}. Usando valor por defecto.", 
                    productoId, e.getMessage());
            return 5.0;
        }
    }
    
    /**
     * Calcula el número de órdenes anuales necesarias.
     */
    private int calcularNumeroOrdenes(double demandaAnual, int eoq) {
        if (eoq == 0) return 0;
        return (int) Math.ceil(demandaAnual / eoq);
    }
    
    /**
     * Calcula los días entre cada lote.
     */
    private int calcularDiasEntreLotes(int numeroOrdenes) {
        if (numeroOrdenes == 0) return 0;
        return 365 / numeroOrdenes;
    }
    
    /**
     * Calcula el costo total de inventario anual.
     * 
     * Costo Total = Costo de Pedido + Costo de Mantenimiento
     *             = (D/Q × S) + (Q/2 × H)
     */
    private double calcularCostoTotalInventario(
            double demandaAnual, 
            int eoq, 
            double costoPedido, 
            double costoMantenimiento) {
        
        double costoPedidoTotal = (demandaAnual / eoq) * costoPedido;
        double costoMantenimientoTotal = (eoq / 2.0) * costoMantenimiento;
        
        return costoPedidoTotal + costoMantenimientoTotal;
    }
    
    // ========== MÉTODOS DE OBTENCIÓN DE PARÁMETROS ==========
    
    private double obtenerCostoUnitario(Producto producto) {
        return producto.getCostoAdquisicion() != null ? 
                producto.getCostoAdquisicion().doubleValue() : 10.0;
    }
    
    private double obtenerCostoPedido(Producto producto) {
        return producto.getCostoPedido() != null ? 
                producto.getCostoPedido().doubleValue() : COSTO_PEDIDO_DEFAULT;
    }
    
    private double obtenerCostoMantenimiento(Producto producto, double costoUnitario) {
        if (producto.getCostoMantenimientoAnual() != null) {
            return producto.getCostoMantenimientoAnual().doubleValue();
        }
        
        // Calcular como porcentaje del costo unitario
        return costoUnitario * PORCENTAJE_MANTENIMIENTO_DEFAULT;
    }
    
    private int obtenerLeadTime(Producto producto) {
        return producto.getDiasLeadTime() != null ? 
                producto.getDiasLeadTime() : LEAD_TIME_DEFAULT;
    }
    
    private String generarObservaciones(
            int eoq, int rop, int stockSeguridad, double nivelServicio) {
        return String.format(
                "EOQ: %d unidades | ROP: %d unidades | Stock Seguridad: %d unidades | " +
                "Nivel de servicio: %.0f%% | Calculado automáticamente desde predicción ML",
                eoq, rop, stockSeguridad, nivelServicio * 100
        );
    }
    
    
    // ========== IMPLEMENTACIÓN DE MÉTODOS DE INTERFACE (PUBLIC) ==========
    
    @Override
    public Double calcularEOQ(Double demandaAnual, Double costoPedido, Double costoAlmacenamiento) {
        if (costoAlmacenamiento <= 0 || costoPedido <= 0 || demandaAnual <= 0) {
            log.warn("[OPTIMIZACION] Advertencia: Parámetros inválidos para EOQ");
            return 0.0;
        }
        return Math.sqrt((2 * demandaAnual * costoPedido) / costoAlmacenamiento);
    }
    
    @Override
    public Double calcularROP(Double demandaDiaria, Integer tiempoEntregaDias, Double stockSeguridad) {
        if (demandaDiaria == null || tiempoEntregaDias == null || stockSeguridad == null) {
            return 0.0;
        }
        return (demandaDiaria * tiempoEntregaDias) + stockSeguridad;
    }
    
    @Override
    public Double calcularStockSeguridad(Double factorZ, Double desviacionEstandar, Integer tiempoEntregaDias) {
        if (factorZ == null || desviacionEstandar == null || tiempoEntregaDias == null) {
            return 0.0;
        }
        return factorZ * desviacionEstandar * Math.sqrt(tiempoEntregaDias);
    }
    
    @Override
    public Double obtenerFactorZ(Double nivelServicio) {
        if (nivelServicio == null) return 1.0;
        if (nivelServicio >= 0.99) return 2.33;
        if (nivelServicio >= 0.975) return 1.96;
        if (nivelServicio >= 0.95) return 1.65;
        if (nivelServicio >= 0.90) return 1.28;
        return 1.0;
    }
    
    @Override
    public OptimizacionResponse calcularOptimizacion(CalcularOptimizacionRequest request) {
        log.info("Calculando optimización desde request (método legacy)");
        // TODO: Implementar conversión de request → respuesta
        // Este método es legacy, usar calcularEOQROPDesdePrediccion() en su lugar
        return OptimizacionResponse.builder()
            .fechaCalculo(LocalDateTime.now())
            .build();
    }
    
    @Override
    public OptimizacionResponse obtenerOptimizacionPorPrediccion(Long prediccionId) {
        log.info("Obteniendo optimización para predicción {}", prediccionId);
        // TODO: Implementar búsqueda por predicción
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CalculoOptimizacionResponse> obtenerCalculoPorProducto(Long productoId) {
        return calculoRepository.findByProducto_ProductoId(Math.toIntExact(productoId))
                .map(calculoMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CalculoOptimizacionResponse> obtenerTodosLosCalculos() {
        return calculoRepository.findAll().stream()
                .map(calculoMapper::toResponse)
                .toList();
    }
}
