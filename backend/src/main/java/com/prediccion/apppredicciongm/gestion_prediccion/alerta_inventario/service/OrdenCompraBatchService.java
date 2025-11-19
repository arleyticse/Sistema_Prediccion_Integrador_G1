package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.utils.SKUGenerator;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProveedorBasicoDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenOrdenDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.ErrorProcesamientoLoteException;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.repository.ICalculoObtimizacionRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository.IOrdenCompraRepositorio;
import com.prediccion.apppredicciongm.models.*;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de generacion batch de ordenes de compra.
 * 
 * Agrupa productos por proveedor y genera ordenes optimizadas
 * basadas en calculos EOQ/ROP.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrdenCompraBatchService implements IOrdenCompraBatchService {

    private final IAlertaInventarioRepositorio alertaRepositorio;
    private final IOrdenCompraRepositorio ordenCompraRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IProveedorRepositorio proveedorRepositorio;
    private final IUsuarioRepository usuarioRepositorio;
    private final ICalculoObtimizacionRepositorio calculoOptimizacionRepositorio;
    private final ProveedorService proveedorService;
    private final AlertaInventarioService alertaInventarioService;

    @Override
    @Transactional
    public ProcesamientoBatchResponse generarOrdenesPorProveedor(
            List<Long> alertaIds, 
            Integer usuarioId) {
        
        log.info("Iniciando generacion de ordenes de compra para {} alertas", alertaIds.size());
        
        LocalDateTime inicio = LocalDateTime.now();
        ProcesamientoBatchResponse response = ProcesamientoBatchResponse.builder()
            .fechaInicio(inicio)
            .totalProcesadas(alertaIds.size())
            .exitosos(0)
            .fallidos(0)
            .exitoTotal(false)
            .build();

        try {
            // 1. Obtener alertas
            List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
            
            if (alertas.isEmpty()) {
                throw new ErrorProcesamientoLoteException(
                    "No se encontraron alertas con los IDs proporcionados"
                );
            }

            // 2. Obtener usuario
            Usuario usuario = null;
            if (usuarioId != null) {
                usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
            }

            // 3. Agrupar productos por proveedor
            Map<Integer, List<ItemOrdenInterno>> productosPorProveedor = 
                agruparProductosPorProveedor(alertas);

            log.info("Productos agrupados en {} proveedores", productosPorProveedor.size());

            // 4. Generar orden para cada proveedor
            for (Map.Entry<Integer, List<ItemOrdenInterno>> entry : productosPorProveedor.entrySet()) {
                Integer proveedorId = entry.getKey();
                List<ItemOrdenInterno> items = entry.getValue();

                try {
                    OrdenCompra orden = generarOrdenParaProveedorInterno(
                        proveedorId, 
                        items, 
                        usuario
                    );

                    response.setExitosos(response.getExitosos() + items.size());
                    response.getOrdenesGeneradas().add(orden.getOrdenCompraId());
                    
                    // Marcar alertas como resueltas
                    List<Long> alertasIds = items.stream()
                        .map(item -> item.alertaId)
                        .collect(Collectors.toList());
                    
                    alertaInventarioService.marcarAlertasComoResueltas(
                        alertasIds,
                        "Orden de compra generada: " + orden.getNumeroOrden()
                    );

                    log.info("Orden generada exitosamente: {} para proveedor ID: {}", 
                        orden.getNumeroOrden(), proveedorId);

                } catch (Exception e) {
                    log.error("Error al generar orden para proveedor ID: {}", proveedorId, e);
                    response.setFallidos(response.getFallidos() + items.size());
                    response.getMensajesError().add(
                        "Proveedor ID " + proveedorId + ": " + e.getMessage()
                    );
                }
            }

            // 5. Finalizar
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setExitoTotal(response.getFallidos() == 0);
            
            String observaciones = String.format(
                "Ordenes generadas: %d proveedores, %d productos procesados. Tiempo: %d ms",
                productosPorProveedor.size(),
                response.getExitosos(),
                response.getTiempoEjecucionMs()
            );
            response.setObservaciones(observaciones);

            log.info(observaciones);
            return response;

        } catch (Exception e) {
            log.error("Error en generacion batch de ordenes", e);
            
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setFallidos(alertaIds.size());
            response.setExitoTotal(false);
            response.getMensajesError().add("Error general: " + e.getMessage());
            
            throw new ErrorProcesamientoLoteException(
                "Error al generar ordenes de compra en lote", e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<ItemOrdenPreview>> obtenerPreviewOrdenes(List<Long> alertaIds) {
        log.info("Generando preview de ordenes para {} alertas", alertaIds.size());
        
        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        Map<String, List<ItemOrdenPreview>> preview = new LinkedHashMap<>();

        for (AlertaInventario alerta : alertas) {
            Producto producto = alerta.getProducto();
            if (producto == null) continue;

            // Obtener proveedor principal
            ProveedorBasicoDTO proveedorInfo = proveedorService.obtenerProveedorPrincipal(
                producto.getProductoId()
            );
            
            if (proveedorInfo == null) {
                log.warn("No se encontro proveedor para producto ID: {}", 
                    producto.getProductoId());
                continue;
            }

            String proveedorNombre = proveedorInfo.getNombreComercial();
            
            // Obtener cantidad de EOQ
            Integer cantidad = obtenerCantidadOptima(alerta, producto);

            // Crear item de preview
            ItemOrdenPreview item = new ItemOrdenPreview();
            item.productoId = producto.getProductoId();
            item.productoNombre = producto.getNombre();
            item.cantidad = cantidad;
            item.precioUnitario = producto.getCostoAdquisicion() != null 
                ? producto.getCostoAdquisicion().doubleValue() : 0.0;
            item.subtotal = item.precioUnitario * cantidad;

            // Agregar a la lista del proveedor
            preview.computeIfAbsent(proveedorNombre, k -> new ArrayList<>()).add(item);
        }

        return preview;
    }

    @Override
    @Transactional
    public OrdenCompra generarOrdenParaProveedor(
            Integer proveedorId,
            List<ItemOrden> items,
            Integer usuarioId) {
        
        log.info("Generando orden para proveedor ID: {} con {} items", 
            proveedorId, items.size());

        // Convertir a ItemOrdenInterno
        List<ItemOrdenInterno> itemsInternos = items.stream()
            .map(item -> {
                ItemOrdenInterno interno = new ItemOrdenInterno();
                interno.productoId = item.productoId;
                interno.cantidad = item.cantidad;
                interno.alertaId = item.alertaId;
                return interno;
            })
            .collect(Collectors.toList());

        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
        }

        return generarOrdenParaProveedorInterno(proveedorId, itemsInternos, usuario);
    }

    /**
     * Agrupa productos por proveedor principal.
     */
    private Map<Integer, List<ItemOrdenInterno>> agruparProductosPorProveedor(
            List<AlertaInventario> alertas) {
        
        Map<Integer, List<ItemOrdenInterno>> agrupacion = new HashMap<>();

        for (AlertaInventario alerta : alertas) {
            Producto producto = alerta.getProducto();
            if (producto == null) {
                log.warn("Alerta ID {} no tiene producto asociado", alerta.getAlertaId());
                continue;
            }

            // Obtener proveedor principal
            ProveedorBasicoDTO proveedorInfo = proveedorService.obtenerProveedorPrincipal(
                producto.getProductoId()
            );
            
            if (proveedorInfo == null) {
                log.warn("No se encontro proveedor para producto ID: {}", 
                    producto.getProductoId());
                continue;
            }

            // Obtener cantidad optima (EOQ o cantidad sugerida)
            Integer cantidad = obtenerCantidadOptima(alerta, producto);

            // Crear item
            ItemOrdenInterno item = new ItemOrdenInterno();
            item.productoId = producto.getProductoId();
            item.cantidad = cantidad;
            item.alertaId = alerta.getAlertaId();
            item.precioUnitario = producto.getCostoAdquisicion() != null
                ? producto.getCostoAdquisicion().doubleValue() : null;

            // Agregar a la lista del proveedor
            agrupacion.computeIfAbsent(proveedorInfo.getProveedorId(), k -> new ArrayList<>())
                .add(item);
        }

        return agrupacion;
    }

    /**
     * Obtiene la cantidad optima a pedir para un producto.
     * Prioridad: EOQ > Cantidad sugerida > Stock minimo * 2
     */
    private Integer obtenerCantidadOptima(AlertaInventario alerta, Producto producto) {
        // 1. Intentar obtener EOQ de la ultima optimizacion
        Integer eoq = obtenerEOQDelProducto(producto.getProductoId());
        if (eoq != null && eoq > 0) {
            log.debug("Usando EOQ para producto ID {}: {}", producto.getProductoId(), eoq);
            return eoq;
        }

        // 2. Usar cantidad sugerida de la alerta
        if (alerta.getCantidadSugerida() != null && alerta.getCantidadSugerida() > 0) {
            log.debug("Usando cantidad sugerida para producto ID {}: {}", 
                producto.getProductoId(), alerta.getCantidadSugerida());
            return alerta.getCantidadSugerida();
        }

        // 3. Fallback: stock minimo * 2
        if (alerta.getStockMinimo() != null && alerta.getStockMinimo() > 0) {
            Integer fallback = alerta.getStockMinimo() * 2;
            log.debug("Usando fallback (stock minimo * 2) para producto ID {}: {}", 
                producto.getProductoId(), fallback);
            return fallback;
        }

        // 4. Default: 100 unidades
        log.warn("No se pudo determinar cantidad optima para producto ID {}, usando default: 100", 
            producto.getProductoId());
        return 100;
    }

    /**
     * Obtiene el EOQ del producto desde la ultima optimizacion.
     */
    private Integer obtenerEOQDelProducto(Integer productoId) {
        try {
            var calculoOpt = calculoOptimizacionRepositorio
                .findByProductoProductoIdOrderByFechaCalculoDesc(productoId)
                .stream()
                .findFirst();
            
            if (calculoOpt.isPresent() && calculoOpt.get().getEoqCantidadOptima() != null) {
                return calculoOpt.get().getEoqCantidadOptima();
            }
        } catch (Exception e) {
            log.debug("No se encontro EOQ para producto ID: {}", productoId);
        }
        return null;
    }

    /**
     * Genera la orden de compra para un proveedor.
     */
    private OrdenCompra generarOrdenParaProveedorInterno(
            Integer proveedorId,
            List<ItemOrdenInterno> items,
            Usuario usuario) {
        
        // 1. Obtener proveedor
        Proveedor proveedor = proveedorRepositorio.findById(proveedorId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Proveedor no encontrado: " + proveedorId
            ));

        // 2. Crear orden
        OrdenCompra orden = new OrdenCompra();
        orden.setNumeroOrden(generarNumeroOrden());
        orden.setProveedor(proveedor);
        orden.setFechaOrden(LocalDate.now());
        orden.setEstadoOrden(EstadoOrdenCompra.BORRADOR);
        orden.setGeneradaAutomaticamente(true);
        orden.setUsuario(usuario);
        
        // Calcular fecha entrega esperada
        if (proveedor.getTiempoEntregaDias() != null) {
            orden.setFechaEntregaEsperada(
                LocalDate.now().plusDays(proveedor.getTiempoEntregaDias())
            );
        }

        orden.setObservaciones("Orden generada automaticamente por el sistema de alertas");

        // 3. Crear detalles
        List<DetalleOrdenCompra> detalles = new ArrayList<>();
        BigDecimal totalOrden = BigDecimal.ZERO;

        for (ItemOrdenInterno item : items) {
            Producto producto = productoRepositorio.findById(item.productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Producto no encontrado: " + item.productoId
                ));

            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setOrdenCompra(orden);
            detalle.setProducto(producto);
            detalle.setCantidadSolicitada(item.cantidad);
            
            BigDecimal precioUnitario = item.precioUnitario != null 
                ? BigDecimal.valueOf(item.precioUnitario)
                : BigDecimal.ZERO;
            
            detalle.setPrecioUnitario(precioUnitario);
            detalle.calcularSubtotal();
            
            totalOrden = totalOrden.add(detalle.getSubtotal());
            detalles.add(detalle);
        }

        orden.setDetalles(detalles);
        orden.setTotalOrden(totalOrden);

        // 4. Guardar
        OrdenCompra ordenGuardada = ordenCompraRepositorio.save(orden);
        
        log.info("Orden creada: {} - Total: {} - Items: {}", 
            ordenGuardada.getNumeroOrden(), 
            ordenGuardada.getTotalOrden(),
            detalles.size());

        return ordenGuardada;
    }

    /**
     * Genera un numero de orden unico.
     */
    private String generarNumeroOrden() {
        String prefijo = "OC-AUTO-";
        String fecha = LocalDate.now().toString().replace("-", "");
        long contador = ordenCompraRepositorio.count() + 1;
        return String.format("%s%s-%04d", prefijo, fecha, contador);
    }

    @Override
    public List<ResumenOrdenDTO> obtenerResumenOrdenes(List<Long> ordenIds) {
        log.info("Obteniendo resumen de {} ordenes de compra", ordenIds.size());
        
        List<OrdenCompra> ordenes = ordenCompraRepositorio.findAllById(ordenIds);
        
        return ordenes.stream()
            .map(this::mapearAResumenOrden)
            .collect(Collectors.toList());
    }

    /**
     * Mapea una OrdenCompra a ResumenOrdenDTO.
     */
    private ResumenOrdenDTO mapearAResumenOrden(OrdenCompra orden) {
        // Mapear proveedor
        ResumenOrdenDTO.ProveedorResumenDTO proveedorDTO = ResumenOrdenDTO.ProveedorResumenDTO.builder()
            .proveedorId(orden.getProveedor().getProveedorId())
            .nombreComercial(orden.getProveedor().getNombreComercial())
            .razonSocial(orden.getProveedor().getRazonSocial())
            .ruc(orden.getProveedor().getRucNit())
            .tiempoEntrega(orden.getProveedor().getTiempoEntregaDias())
            .build();

        // Mapear productos
        List<ResumenOrdenDTO.ProductoResumenDTO> productosDTO = orden.getDetalles().stream()
            .map(detalle -> ResumenOrdenDTO.ProductoResumenDTO.builder()
                .productoId(detalle.getProducto().getProductoId())
                .nombre(detalle.getProducto().getNombre())
                .codigoSKU(SKUGenerator.generarSKU(detalle.getProducto())) // Generar SKU dinámicamente
                .cantidadSolicitada(detalle.getCantidadSolicitada())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build())
            .collect(Collectors.toList());

        // Construir ResumenOrdenDTO
        return ResumenOrdenDTO.builder()
            .ordenId(orden.getOrdenCompraId())
            .numeroOrden(orden.getNumeroOrden())
            .proveedor(proveedorDTO)
            .fechaOrden(orden.getFechaOrden())
            .fechaEntregaEsperada(orden.getFechaEntregaEsperada())
            .totalOrden(orden.getTotalOrden())
            .estadoOrden(orden.getEstadoOrden() != null ? orden.getEstadoOrden().name() : "PENDIENTE")
            .cantidadProductos(orden.getDetalles().size())
            .productos(productosDTO)
            .generadaAutomaticamente(orden.getGeneradaAutomaticamente())
            .observaciones(orden.getObservaciones())
            .build();
    }

    /**
     * Clase interna para items de orden con info completa.
     */
    private static class ItemOrdenInterno {
        Integer productoId;
        Integer cantidad;
        Long alertaId;
        Double precioUnitario;
    }
}
