package com.prediccion.apppredicciongm.gestion_inventario.inventario.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.AjusteStockRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioUpdateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioAlertaResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.StockResumenResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.mapper.InventarioMapper;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de inventario del sistema.
 * 
 * Proporciona operaciones CRUD, búsquedas avanzadas, control de alertas de stock,
 * ajustes manuales y generación de reportes de inventario. Mantiene la coherencia
 * entre el stock disponible y los movimientos registrados.
 * 
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class InventarioServicio implements IInventarioServicio {

    private static final Logger log = LoggerFactory.getLogger(InventarioServicio.class);

    private final IInventarioRepositorio inventarioRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final InventarioMapper inventarioMapper;

    @Override
    @Transactional
    public InventarioResponse crearInventario(InventarioCreateRequest request) {
        log.info("Creando nuevo inventario para producto ID: {}", request.getProductoId());
        
        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> {
                    log.error("Producto no encontrado para crear inventario - ID: {}", request.getProductoId());
                    return new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId());
                });
        
        if (inventarioRepositorio.findByProducto(request.getProductoId()).isPresent()) {
            log.warn("Intento de crear inventario duplicado para producto: {}", producto.getNombre());
            throw new IllegalArgumentException(
                    "Ya existe un inventario para el producto con ID: " + request.getProductoId());
        }
        
        Inventario inventario = inventarioMapper.toEntity(request);
        inventario.setProducto(producto);
        inventario.setEstado(EstadoInventario.NORMAL);
        inventario.setDiasSinVenta(0);
        
        Inventario inventarioGuardado = inventarioRepositorio.save(inventario);
        log.info("Inventario creado exitosamente - ID: {}, Producto: {}, Stock Inicial: {}", 
            inventarioGuardado.getInventarioId(), producto.getNombre(), inventarioGuardado.getStockDisponible());
        
        return inventarioMapper.toResponse(inventarioGuardado);
    }

    @Override
    @Transactional
    public InventarioResponse actualizarInventario(Integer inventarioId, InventarioUpdateRequest request) {
        log.info("Actualizando inventario con ID: {}", inventarioId);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para actualizar - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        inventarioMapper.updateEntityFromDto(request, inventario);
        Inventario inventarioActualizado = inventarioRepositorio.save(inventario);
        
        log.info("Inventario actualizado exitosamente - ID: {}, Stock Minimo: {}, Stock Máximo: {}", 
            inventarioId, request.getStockMinimo(), request.getStockMaximo());
        
        return inventarioMapper.toResponse(inventarioActualizado);
    }

    @Override
    @Transactional
    public void eliminarInventario(Integer inventarioId) {
        log.info("Eliminando inventario con ID: {}", inventarioId);
        
        if (!inventarioRepositorio.existsById(inventarioId)) {
            log.error("Intento de eliminar inventario no existente - ID: {}", inventarioId);
            throw new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
        }
        
        inventarioRepositorio.deleteById(inventarioId);
        log.info("Inventario eliminado exitosamente - ID: {}", inventarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse obtenerInventarioPorId(Integer inventarioId) {
        log.debug("Obteniendo inventario con ID: {}", inventarioId);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.warn("Inventario no encontrado - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        return inventarioMapper.toResponse(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> listarInventarios(int pagina, int tamanioPagina) {
        log.debug("Listando inventarios - Página: {}, Tamaño: {}", pagina, tamanioPagina);
        
        Page<Inventario> inventarios = inventarioRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
        log.info("Se encontraron {} inventarios en total", inventarios.getTotalElements());
        
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse buscarPorProducto(Integer productoId) {
        log.debug("Buscando inventario para producto ID: {}", productoId);
        
        Inventario inventario = inventarioRepositorio.findByProducto(productoId)
                .orElseThrow(() -> {
                    log.warn("No se encontró inventario para producto ID: {}", productoId);
                    return new IllegalArgumentException("No se encontró inventario para el producto con ID: " + productoId);
                });
        
        return inventarioMapper.toResponse(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> buscarPorCategoria(Integer categoriaId) {
        log.debug("Buscando inventarios por categoría - ID: {}", categoriaId);
        
        List<Inventario> inventarios = inventarioRepositorio.findByCategoria(categoriaId);
        log.info("Se encontraron {} inventarios en la categoría: {}", inventarios.size(), categoriaId);
        
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> buscarPorEstado(EstadoInventario estado, int pagina, int tamanioPagina) {
        log.debug("Buscando inventarios por estado - Estado: {}, Página: {}", estado, pagina);
        
        Page<Inventario> inventarios = inventarioRepositorio.findByEstado(estado, 
                PageRequest.of(pagina, tamanioPagina));
        
        log.info("Se encontraron {} inventarios en estado: {}", inventarios.getTotalElements(), estado);
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina) {
        log.debug("Buscando inventarios por nombre: {}", nombre);
        
        Page<Inventario> inventarios = inventarioRepositorio.findByNombreProductoContaining(nombre, 
                PageRequest.of(pagina, tamanioPagina));
        
        log.info("Se encontraron {} inventarios con nombre similar a: {}", inventarios.getTotalElements(), nombre);
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> buscarPorRangoStock(Integer minStock, Integer maxStock) {
        log.debug("Buscando inventarios por rango de stock - Min: {}, Max: {}", minStock, maxStock);
        
        List<Inventario> inventarios = inventarioRepositorio.findByRangoStock(minStock, maxStock);
        log.info("Se encontraron {} inventarios en rango de stock {}-{}", inventarios.size(), minStock, maxStock);
        
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasStockBajo() {
        log.info("Obteniendo alertas de stock bajo");
        
        List<Inventario> inventarios = inventarioRepositorio.findInventariosBajoStockReorden();
        log.info("Se encontraron {} inventarios con stock bajo", inventarios.size());
        
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasCriticas() {
        log.info("Obteniendo alertas críticas de inventario");
        
        List<Inventario> inventarios = inventarioRepositorio.findInventariosCriticos();
        log.warn("Se encontraron {} inventarios en estado crítico", inventarios.size());
        
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasAgotados() {
        log.info("Obteniendo alertas de productos agotados");
        
        List<Inventario> inventarios = inventarioRepositorio.findInventariosAgotados();
        log.warn("Se encontraron {} inventarios agotados (sin stock)", inventarios.size());
        
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasSinMovimiento(Integer diasSinMovimiento) {
        log.info("Obteniendo alertas de inventarios sin movimiento - Días: {}", diasSinMovimiento);
        
        List<Inventario> inventarios = inventarioRepositorio.findInventariosSinMovimiento(diasSinMovimiento);
        log.info("Se encontraron {} inventarios sin movimiento en los últimos {} días", 
            inventarios.size(), diasSinMovimiento);
        
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> obtenerInventariosSobreMaximo() {
        log.info("Obteniendo inventarios con stock sobre el máximo permitido");
        
        List<Inventario> inventarios = inventarioRepositorio.findInventariosSobreStockMaximo();
        log.warn("Se encontraron {} inventarios por encima del máximo permitido", inventarios.size());
        
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventarioResponse ajustarStock(AjusteStockRequest request) {
        log.info("Ajustando stock para inventario ID: {}, Cantidad: {} ({})", 
            request.getInventarioId(), request.getCantidad(), request.getMotivo());
        
        Inventario inventario = inventarioRepositorio.findById(request.getInventarioId())
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para ajuste de stock - ID: {}", request.getInventarioId());
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + request.getInventarioId());
                });
        
        Integer stockAnterior = inventario.getStockDisponible();
        Integer nuevoStock = inventario.getStockDisponible() + request.getCantidad();
        
        if (nuevoStock < 0) {
            log.error("Ajuste de stock resultaría en valor negativo - ID: {}, Stock Actual: {}, Ajuste: {}", 
                request.getInventarioId(), stockAnterior, request.getCantidad());
            throw new IllegalArgumentException(
                    "El ajuste resultaría en stock negativo. Stock actual: " + 
                    inventario.getStockDisponible() + ", Ajuste: " + request.getCantidad());
        }
        
        inventario.setStockDisponible(nuevoStock);
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        
        // Actualizar estado según el nuevo stock
        if (nuevoStock == 0) {
            inventario.setEstado(EstadoInventario.CRITICO);
        } else if (inventario.bajoPuntoMinimo()) {
            inventario.setEstado(EstadoInventario.BAJO);
        } else {
            inventario.setEstado(EstadoInventario.NORMAL);
        }
        
        Inventario inventarioActualizado = inventarioRepositorio.save(inventario);
        log.info("Stock ajustado exitosamente - ID: {}, Stock Anterior: {}, Stock Nuevo: {}, Estado: {}", 
            request.getInventarioId(), stockAnterior, nuevoStock, inventarioActualizado.getEstado());
        
        return inventarioMapper.toResponse(inventarioActualizado);
    }

    @Override
    @Transactional
    public void actualizarStockDesdeMovimiento(Integer productoId, Integer cantidad, boolean esEntrada) {
        log.debug("Actualizando stock desde movimiento - Producto ID: {}, Cantidad: {}, Tipo: {}", 
            productoId, cantidad, esEntrada ? "ENTRADA" : "SALIDA");
        
        Inventario inventario = inventarioRepositorio.findByProducto(productoId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para actualizar stock - Producto ID: {}", productoId);
                    return new IllegalArgumentException("No se encontró inventario para el producto con ID: " + productoId);
                });
        
        Integer stockAnterior = inventario.getStockDisponible();
        Integer nuevoStock;
        
        if (esEntrada) {
            nuevoStock = inventario.getStockDisponible() + cantidad;
            log.debug("Entrada de stock procesada - Producto ID: {}, Cantidad: {}", productoId, cantidad);
        } else {
            nuevoStock = inventario.getStockDisponible() - cantidad;
            if (nuevoStock < 0) {
                log.error("Stock insuficiente para salida - Producto ID: {}, Stock Actual: {}, Cantidad Solicitada: {}", 
                    productoId, inventario.getStockDisponible(), cantidad);
                throw new IllegalArgumentException(
                        "Stock insuficiente. Stock actual: " + inventario.getStockDisponible() + 
                        ", Cantidad solicitada: " + cantidad);
            }
            log.debug("Salida de stock procesada - Producto ID: {}, Cantidad: {}", productoId, cantidad);
        }
        
        inventario.setStockDisponible(nuevoStock);
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        inventario.setDiasSinVenta(0);
        
        // Actualizar estado
        if (nuevoStock == 0) {
            inventario.setEstado(EstadoInventario.CRITICO);
        } else if (inventario.bajoPuntoMinimo()) {
            inventario.setEstado(EstadoInventario.BAJO);
        } else {
            inventario.setEstado(EstadoInventario.NORMAL);
        }
        
        inventarioRepositorio.save(inventario);
        log.info("Stock actualizado exitosamente - Producto ID: {}, Stock Anterior: {}, Stock Nuevo: {}, Estado: {}", 
            productoId, stockAnterior, nuevoStock, inventario.getEstado());
    }

    @Override
    @Transactional
    public void actualizarFechaUltimoMovimiento(Integer inventarioId) {
        log.debug("Actualizando fecha de último movimiento - Inventario ID: {}", inventarioId);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para actualizar fecha - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        inventarioRepositorio.save(inventario);
        log.debug("Fecha de último movimiento actualizada - Inventario ID: {}", inventarioId);
    }

    @Override
    @Transactional
    public void actualizarDiasSinVenta(Integer inventarioId, Integer dias) {
        log.debug("Actualizando días sin venta - Inventario ID: {}, Días: {}", inventarioId, dias);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para actualizar días - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        inventario.setDiasSinVenta(dias);
        inventarioRepositorio.save(inventario);
        log.debug("Días sin venta actualizados - Inventario ID: {}, Días: {}", inventarioId, dias);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResumenResponse obtenerResumenGeneral() {
        log.info("Generando resumen general del inventario");
        
        Long totalProductos = inventarioRepositorio.count();
        Long productosActivos = inventarioRepositorio.countProductosActivos();
        Long productosInactivos = inventarioRepositorio.countProductosInactivos();
        Long productosConStockBajo = inventarioRepositorio.countProductosStockBajo();
        Long productosAgotados = inventarioRepositorio.countProductosAgotados();
        Long productosSinMovimiento = inventarioRepositorio.countProductosSinMovimiento(30);
        Double valorTotal = inventarioRepositorio.calcularValorTotalInventario();
        Long stockTotal = inventarioRepositorio.sumStockTotalDisponible();
        
        log.info("Resumen generado - Total: {}, Activos: {}, Stock Bajo: {}, Agotados: {}, Valor Total: ${}", 
            totalProductos, productosActivos, productosConStockBajo, productosAgotados, valorTotal);
        
        return StockResumenResponse.builder()
                .totalProductos(totalProductos.intValue())
                .productosActivos(productosActivos.intValue())
                .productosInactivos(productosInactivos.intValue())
                .productosConStockBajo(productosConStockBajo.intValue())
                .productosAgotados(productosAgotados.intValue())
                .productosSinMovimiento(productosSinMovimiento.intValue())
                .valorTotalInventario(valorTotal != null ? BigDecimal.valueOf(valorTotal) : BigDecimal.ZERO)
                .stockTotalDisponible(stockTotal != null ? stockTotal.intValue() : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean necesitaReorden(Integer inventarioId) {
        log.debug("Verificando si inventario necesita reorden - ID: {}", inventarioId);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para verificar reorden - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        boolean necesita = inventario.necesitaReorden();
        log.debug("Verificación de reorden completada - ID: {}, Necesita: {}", inventarioId, necesita);
        
        return necesita;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaBajoPuntoMinimo(Integer inventarioId) {
        log.debug("Verificando si inventario está bajo punto mínimo - ID: {}", inventarioId);
        
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> {
                    log.error("Inventario no encontrado para verificar mínimo - ID: {}", inventarioId);
                    return new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
                });
        
        boolean estaBajo = inventario.bajoPuntoMinimo();
        if (estaBajo) {
            log.warn("Inventario por debajo del punto mínimo - ID: {}, Stock: {}, Mínimo: {}", 
                inventarioId, inventario.getStockDisponible(), inventario.getStockMinimo());
        }
        
        return estaBajo;
    }
}
