package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

// Imports para los modelos y repositorios
import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.models.DetalleOrdenCompra;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository.IOrdenCompraRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.*;
import com.prediccion.apppredicciongm.gestion_configuracion.service.ConfiguracionEmpresaService;
import com.prediccion.apppredicciongm.models.ConfiguracionEmpresa;

import java.util.stream.Collectors;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.services.IKardexService;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request.RecibirOrdenRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request.DetalleRecibidoRequest;
import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.models.AlertaInventario;

/**
 * Servicio principal para órdenes de compra automáticas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrdenCompraService implements IOrdenCompraService {

    private final IOrdenCompraRepositorio ordenCompraRepositorio;
    private final IPrediccionRepositorio prediccionRepositorio;
    private final IProveedorRepositorio proveedorRepositorio;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final IKardexService kardexService;
    private final IAlertaInventarioRepositorio alertaInventarioRepositorio;

    @Override
    public OrdenCompra generarOrdenAutomatica(Integer prediccionId) {
        return generarOrdenDesdePredicion(prediccionId);
    }

    @Override
    public boolean validarOrdenCompra(Integer productoId) {
        // Implementación básica - siempre retorna true para testing
        log.info("[ORDEN][VALIDACION] Validando orden de compra para producto ID: {}", productoId);
        return true;
    }

    @Override
    public Page<OrdenCompra> obtenerOrdenesPorProducto(Integer productoId, Pageable pageable) {
        log.info("[ORDEN][CONSULTA] Obteniendo órdenes para producto ID: {}", productoId);
        // Implementación básica
        List<OrdenCompra> todasLasOrdenes = ordenCompraRepositorio.findAll();
        return new PageImpl<>(todasLasOrdenes, pageable, todasLasOrdenes.size());
    }

    @Override
    public Page<OrdenCompra> obtenerTodasLasOrdenes(Pageable pageable) {
        log.info("[ORDEN][CONSULTA] Obteniendo todas las órdenes con paginación");
        return ordenCompraRepositorio.findAll(pageable);
    }

    @Override
    public OrdenCompra obtenerUltimaOrden(Integer productoId) {
        log.info("[ORDEN][CONSULTA] Obteniendo última orden para producto ID: {}", productoId);
        return ordenCompraRepositorio.findAll().stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public OrdenCompra obtenerOrdenPorId(Long ordenId) {
        log.info("[ORDEN][CONSULTA] Obteniendo orden por ID: {}", ordenId);
        return ordenCompraRepositorio.findById(ordenId)
                .orElseThrow(() -> new com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException("Orden no encontrada: " + ordenId));
    }

    @Override
    public void confirmarOrden(Long ordenId) {
        log.info("[ORDEN][CONFIRMACION] Confirmando orden ID: {}", ordenId);
        Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
        if (ordenOpt.isPresent()) {
            OrdenCompra orden = ordenOpt.get();
            orden.setEstadoOrden(EstadoOrdenCompra.APROBADA);
            ordenCompraRepositorio.save(orden);
            log.info("[ORDEN][CONFIRMACION] Orden {} confirmada exitosamente", ordenId);
        } else {
            log.error("[ORDEN][ERROR] Orden no encontrada: {}", ordenId);
            throw new RuntimeException("Orden no encontrada: " + ordenId);
        }
    }

    @Override
    public void cancelarOrden(Long ordenId) {
        log.info("[ORDEN][CANCELACION] Cancelando orden ID: {}", ordenId);
        Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
        if (ordenOpt.isPresent()) {
            OrdenCompra orden = ordenOpt.get();
            orden.setEstadoOrden(EstadoOrdenCompra.CANCELADA);
            ordenCompraRepositorio.save(orden);
            log.info("[ORDEN][CANCELACION] Orden {} cancelada exitosamente", ordenId);
        } else {
            log.error("[ORDEN][ERROR] Orden no encontrada: {}", ordenId);
            throw new RuntimeException("Orden no encontrada: " + ordenId);
        }
    }

    @Override
    public void recibirOrden(Long ordenId, RecibirOrdenRequest request) {
        log.info("[ORDEN][RECEPCION] Recibiendo orden ID: {}", ordenId);

        OrdenCompra orden = ordenCompraRepositorio.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenId));

        // Validar estados permitidos
        if (orden.getEstadoOrden() == EstadoOrdenCompra.CANCELADA ||
                orden.getEstadoOrden() == EstadoOrdenCompra.BORRADOR) {
            throw new IllegalArgumentException("No se puede recibir una orden en estado: " + orden.getEstadoOrden());
        }

        // Recibir cada detalle
        for (DetalleRecibidoRequest detalleRecibido : request.getDetalles()) {
            Long detalleId = detalleRecibido.getDetalleId();
            Integer cantidadRecibida = detalleRecibido.getCantidadRecibida();

            DetalleOrdenCompra detalle = orden.getDetalles().stream()
                    .filter(d -> d.getDetalleId().equals(detalleId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Detalle no encontrado: " + detalleId));

            int pendiente = detalle.getCantidadSolicitada() - (detalle.getCantidadRecibida() == null ? 0 : detalle.getCantidadRecibida());
            if (cantidadRecibida == null || cantidadRecibida <= 0) {
                continue; // ignorar cero
            }
            if (cantidadRecibida > pendiente) {
                throw new IllegalArgumentException("Cantidad recibida mayor a la solicitada para detalle " + detalleId);
            }

            // Registrar movimiento kardex por la cantidad recibida
            // Usar precio del detalle, o costo del producto como fallback
            BigDecimal costoUnitario = detalle.getPrecioUnitario();
            if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) == 0) {
                costoUnitario = detalle.getProducto().getCostoAdquisicion();
            }
            if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) == 0) {
                costoUnitario = detalle.getProducto().getCostoPedido();
            }
            if (costoUnitario == null) {
                costoUnitario = BigDecimal.ZERO;
                log.warn("[ORDEN][RECEPCION] Producto {} sin precio definido, usando 0", 
                        detalle.getProducto().getProductoId());
            }
            
            KardexCreateRequest kardexReq = KardexCreateRequest.builder()
                    .productoId(detalle.getProducto().getProductoId())
                    .tipoMovimiento(TipoMovimiento.ENTRADA_COMPRA)
                    .cantidad(cantidadRecibida)
                    .costoUnitario(costoUnitario)
                    .proveedorId(orden.getProveedor() != null ? orden.getProveedor().getProveedorId() : null)
                    .numeroDocumento(request.getNumeroDocumentoProveedor())
                    .tipoDocumento("FACTURA")
                    .motivo("Recepción orden: " + orden.getNumeroOrden())
                    .observaciones(request.getObservaciones())
                    .build();

            kardexService.registrarMovimiento(kardexReq);

            // Actualizar cantidad recibida en detalle
            Integer actual = detalle.getCantidadRecibida() == null ? 0 : detalle.getCantidadRecibida();
            detalle.setCantidadRecibida(actual + cantidadRecibida);
            
            // Resolver alertas pendientes del producto
            resolverAlertasPendientesProducto(detalle.getProducto().getProductoId(), orden.getNumeroOrden());
        }

        // Determinar estado final de la orden
        boolean todosRecibidos = orden.getDetalles().stream()
                .allMatch(d -> d.getCantidadRecibida() != null && d.getCantidadRecibida() >= d.getCantidadSolicitada());

        orden.setEstadoOrden(todosRecibidos ? EstadoOrdenCompra.RECIBIDA_COMPLETA : EstadoOrdenCompra.RECIBIDA_PARCIAL);
        orden.setFechaEntregaReal(java.time.LocalDate.now());
        ordenCompraRepositorio.save(orden);

        log.info("[ORDEN][RECEPCION] Orden {} procesada: estado {}", ordenId, orden.getEstadoOrden());
    }
    
    /**
     * Resuelve automáticamente las alertas pendientes de un producto 
     * cuando se recibe stock.
     */
    private void resolverAlertasPendientesProducto(Integer productoId, String numeroOrden) {
        try {
            List<AlertaInventario> alertasPendientes = alertaInventarioRepositorio
                    .findAlertasPendientesByProducto(productoId);
            
            if (!alertasPendientes.isEmpty()) {
                log.info("[ALERTA] Resolviendo {} alertas pendientes para producto {}", 
                        alertasPendientes.size(), productoId);
                
                for (AlertaInventario alerta : alertasPendientes) {
                    alerta.setEstado(EstadoAlerta.RESUELTA);
                    alerta.setFechaResolucion(java.time.LocalDateTime.now());
                    alerta.setAccionTomada("Stock reabastecido mediante orden: " + numeroOrden);
                    alertaInventarioRepositorio.save(alerta);
                }
                
                log.info("[ALERTA] Alertas resueltas exitosamente para producto {}", productoId);
            }
        } catch (Exception e) {
            log.warn("[ALERTA] Error al resolver alertas para producto {}: {}", productoId, e.getMessage());
        }
    }
    

    @Override
    public OrdenCompra generarOrdenDesdePredicion(Integer prediccionId) {
        try {
            log.info("[ORDEN] Generando orden automática desde predicción ID: {}", prediccionId);
            
            // Buscar la predicción
            Optional<Prediccion> prediccionOpt = prediccionRepositorio.findById(prediccionId);
            if (!prediccionOpt.isPresent()) {
                throw new RuntimeException("Predicción no encontrada: " + prediccionId);
            }
            
            Prediccion prediccion = prediccionOpt.get();
            
            if (prediccion.getProducto() == null) {
                throw new RuntimeException("Producto no encontrado en predicción");
            }

            // Obtener el primer proveedor disponible
            List<Proveedor> proveedores = proveedorRepositorio.findAll();
            if (proveedores.isEmpty()) {
                throw new RuntimeException("No hay proveedores disponibles");
            }
            Proveedor proveedor = proveedores.get(0);

            // Calcular cantidad basada en predicción
            Integer cantidadPedido = 1;
            if (prediccion.getDemandaPredichaTotal() != null) {
                cantidadPedido = Math.max(prediccion.getDemandaPredichaTotal(), 1);
            }
            
            // Calcular precio total estimado
            BigDecimal precioUnitario = BigDecimal.valueOf(10.0);
            BigDecimal totalOrden = precioUnitario.multiply(BigDecimal.valueOf(cantidadPedido));

            // Crear la orden
            OrdenCompra orden = new OrdenCompra();
            orden.setProveedor(proveedor);
            orden.setTotalOrden(totalOrden);
            orden.setGeneradaAutomaticamente(true);
            orden.setEstadoOrden(EstadoOrdenCompra.PENDIENTE);
            orden.setObservaciones(String.format(
                "Orden generada automáticamente desde predicción %d. Demanda predicha: %d", 
                prediccionId, prediccion.getDemandaPredichaTotal()
            ));
            orden.setNumeroOrden("AUTO-" + System.currentTimeMillis());
            orden.setFechaOrden(LocalDate.now());
            orden.setFechaEntregaEsperada(LocalDate.now().plusDays(7));

            // Crear detalle de la orden
            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setProducto(prediccion.getProducto());
            detalle.setCantidadSolicitada(cantidadPedido);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(totalOrden);
            detalle.setOrdenCompra(orden);
            
            // Crear lista de detalles
            List<DetalleOrdenCompra> detalles = new ArrayList<>();
            detalles.add(detalle);
            orden.setDetalles(detalles);

            // Guardar la orden
            OrdenCompra ordenGuardada = ordenCompraRepositorio.save(orden);
            
            log.info("[ORDEN] Orden automática creada: {} para {} unidades", 
                    ordenGuardada.getNumeroOrden(), cantidadPedido);
            
            return ordenGuardada;
            
        } catch (Exception e) {
            log.error("[ORDEN] Error generando orden automática: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar orden automática: " + e.getMessage());
        }
    }

    @Override
    public List<OrdenCompra> obtenerOrdenesBorrador() {
        log.info("[ORDEN][CONSULTA] Obteniendo órdenes en estado BORRADOR");
        List<OrdenCompra> ordenes = ordenCompraRepositorio.findByEstadoOrden(EstadoOrdenCompra.BORRADOR);
        log.info("[ORDEN][CONSULTA] Se encontraron {} órdenes en BORRADOR", ordenes.size());
        return ordenes;
    }

    @Override
    public void aprobarOrdenesBorrador(List<Long> ordenIds) {
        log.info("[ORDEN][APROBACION] Aprobando {} órdenes BORRADOR", ordenIds.size());
        
        for (Long ordenId : ordenIds) {
            Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
            
            if (!ordenOpt.isPresent()) {
                log.warn("[ORDEN][APROBACION] Advertencia: Orden {} no encontrada, se omite", ordenId);
                continue;
            }
            
            OrdenCompra orden = ordenOpt.get();
            
            if (orden.getEstadoOrden() != EstadoOrdenCompra.BORRADOR) {
                log.warn("[ORDEN][APROBACION] Advertencia: Orden {} no está en BORRADOR (estado actual: {}), se omite", 
                        ordenId, orden.getEstadoOrden());
                continue;
            }
            
            orden.setEstadoOrden(EstadoOrdenCompra.PENDIENTE);
            ordenCompraRepositorio.save(orden);
            
            log.info("[ORDEN][APROBACION] Orden {} aprobada: BORRADOR → PENDIENTE", ordenId);
        }
        
        log.info("[ORDEN][APROBACION] Proceso de aprobación completado");
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenOrdenCompraDTO obtenerResumenOrdenCompra(Long ordenId) {
        
        OrdenCompra orden = ordenCompraRepositorio.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con ID: " + ordenId));
        
        // Obtener configuración de la empresa desde la base de datos
        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerConfiguracion();
        
        // Construir datos de la empresa desde la configuración
        DatosEmpresaDTO datosEmpresa = DatosEmpresaDTO.builder()
                .razonSocial(config.getNombreEmpresa())
                .nombreComercial(config.getNombreEmpresa())
                .ruc(config.getRuc() != null ? config.getRuc() : "N/A")
                .direccion(config.getDireccion() != null ? config.getDireccion() : "")
                .telefono(config.getTelefono() != null ? config.getTelefono() : "")
                .email(config.getEmail() != null ? config.getEmail() : "")
                .logoBase64(config.getLogoBase64())
                .logoMimeType(config.getLogoMimeType())
                .build();
        
        // Construir datos del proveedor
        DatosProveedorDTO datosProveedor = DatosProveedorDTO.builder()
                .razonSocial(orden.getProveedor().getRazonSocial())
                .nombreComercial(orden.getProveedor().getNombreComercial())
                .rucNit(orden.getProveedor().getRucNit())
                .direccion(orden.getProveedor().getDireccion())
                .ciudad(orden.getProveedor().getCiudad())
                .pais(orden.getProveedor().getPais())
                .telefono(orden.getProveedor().getTelefono())
                .email(orden.getProveedor().getEmail())
                .personaContacto(orden.getProveedor().getPersonaContacto())
                .build();
        
        // Construir lista de detalles
        List<DetalleProductoOrdenDTO> detalles = orden.getDetalles().stream()
                .map(detalle -> {
                    // Obtener el nombre de la unidad de medida correctamente (evitar proxy de Hibernate)
                    String unidadMedida = "N/A";
                    if (detalle.getProducto() != null && detalle.getProducto().getUnidadMedida() != null) {
                        unidadMedida = detalle.getProducto().getUnidadMedida().getNombre();
                    }
                    
                    return DetalleProductoOrdenDTO.builder()
                            .detalleId(detalle.getDetalleId())
                            .nombreProducto(detalle.getProducto().getNombre())
                            .unidadMedida(unidadMedida)
                            .cantidadSolicitada(detalle.getCantidadSolicitada())
                            .cantidadRecibida(detalle.getCantidadRecibida())
                            .precioUnitario(detalle.getPrecioUnitario())
                            .subtotal(detalle.getSubtotal())
                            .build();
                })
                .collect(Collectors.toList());
        
        // Calcular totales
        BigDecimal subtotalBD = orden.getTotalOrden() != null ? orden.getTotalOrden() : BigDecimal.ZERO;
        BigDecimal impuestos = BigDecimal.ZERO; // Si se implementa IVA, calcular aquí
        BigDecimal total = subtotalBD.add(impuestos);
        
        return ResumenOrdenCompraDTO.builder()
                .ordenCompraId(orden.getOrdenCompraId())
                .numeroOrden(orden.getNumeroOrden())
                .estadoOrden(orden.getEstadoOrden())
                .fechaOrden(orden.getFechaOrden())
                .fechaEntregaEsperada(orden.getFechaEntregaEsperada())
                .fechaEntregaReal(orden.getFechaEntregaReal())
                .empresa(datosEmpresa)
                .proveedor(datosProveedor)
                .detalles(detalles)
                .subtotal(subtotalBD)
                .impuestos(impuestos)
                .totalOrden(total)
                .generadaAutomaticamente(orden.getGeneradaAutomaticamente())
                .observaciones(orden.getObservaciones())
                .fechaCreacion(orden.getFechaRegistro())
                .usuarioCreador(orden.getUsuario() != null ? orden.getUsuario().getNombre() : null)
                .build();
    }
}