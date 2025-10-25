package com.prediccion.apppredicciongm.gestion_inventario.producto.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoEliminadoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.mapper.ProductoMapper;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Categoria;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;
import com.prediccion.apppredicciongm.repository.ICategoriaRepositorio;
import com.prediccion.apppredicciongm.repository.IUnidadMedidaRepositorio;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de productos del inventario.
 * 
 * Maneja todas las operaciones relacionadas con productos incluyendo CRUD,
 * búsqueda, filtrado y cálculos de costos de mantenimiento.
 * 
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoServicio {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final IProductoRepositorio productoRepositorio;
    private final ICategoriaRepositorio categoriaRepositorio;
    private final IUnidadMedidaRepositorio unidadMedidaRepositorio;
    private final IInventarioRepositorio inventarioRepositorio;
    private final IKardexRepositorio kardexRepositorio;
    private final ProductoMapper productoMapper;

    /**
     * Crea un nuevo producto en el catálogo.
     * 
     * Valida que la categoría y unidad de medida existan, calcula el costo
     * de mantenimiento anual e inicializa el inventario en cero.
     * 
     * @param request Datos del producto a crear
     * @return Producto creado con ID generado
     * @throws IllegalArgumentException Si la categoría o unidad de medida no existen
     */
    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoCreateRequest request) {
        log.info("Creando nuevo producto: {}", request.getNombre());
        
        Categoria categoria = categoriaRepositorio.findById(request.getCategoriaId())
                .orElseThrow(() -> {
                    log.error("Categoría no encontrada: {}", request.getCategoriaId());
                    return new IllegalArgumentException("Categoría no encontrada con ID: " + request.getCategoriaId());
                });
        
        UnidadMedida unidadMedida = unidadMedidaRepositorio.findById(request.getUnidadMedidaId())
                .orElseThrow(() -> {
                    log.error("Unidad de medida no encontrada: {}", request.getUnidadMedidaId());
                    return new IllegalArgumentException("Unidad de medida no encontrada con ID: " + request.getUnidadMedidaId());
                });
        
        Producto producto = productoMapper.toEntity(request);
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        
        BigDecimal costoMantenimientoAnual = request.getCostoMantenimiento()
                .multiply(BigDecimal.valueOf(365));
        producto.setCostoMantenimientoAnual(costoMantenimientoAnual);
        
        Producto productoGuardado = productoRepositorio.save(producto);
        log.info("Producto creado exitosamente - ID: {}, Nombre: {}", productoGuardado.getProductoId(), productoGuardado.getNombre());
        
        ProductoResponse response = productoMapper.toResponse(productoGuardado);
        response.setTieneInventario(false);
        response.setStockDisponible(0);
        
        return response;
    }

    @Override
    @Transactional
    public ProductoResponse actualizarProducto(Integer productoId, ProductoCreateRequest request) {
        log.info("Actualizando producto con ID: {}", productoId);
        
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado: {}", productoId);
                    return new IllegalArgumentException("Producto no encontrado con ID: " + productoId);
                });
        
        productoMapper.updateEntityFromDto(request, producto);
        
        if (request.getCostoMantenimiento() != null) {
            BigDecimal costoMantenimientoAnual = request.getCostoMantenimiento()
                    .multiply(BigDecimal.valueOf(365));
            producto.setCostoMantenimientoAnual(costoMantenimientoAnual);
        }
        
        Producto productoActualizado = productoRepositorio.save(producto);
        log.info("Producto actualizado exitosamente - ID: {}, Nombre: {}", productoActualizado.getProductoId(), productoActualizado.getNombre());
        
        return enrichProductoResponse(productoActualizado);
    }

    /**
     * Elimina un producto del catálogo.
     * 
     * Realiza validaciones para asegurar que el producto no tenga inventario activo
     * con stock disponible. Elimina el inventario si existe y archiva sus movimientos
     * para historial.
     * 
     * @param productoId ID del producto a eliminar
     * @return Respuesta con detalles de eliminación (inventario eliminado, movimientos archivados)
     * @throws IllegalArgumentException Si el producto no existe
     * @throws IllegalStateException Si el producto tiene inventario activo con stock
     */
    @Override
    @Transactional
    public ProductoEliminadoResponse eliminarProducto(Integer productoId) {
        log.info("Eliminando producto con ID: {}", productoId);
        
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado para eliminación: {}", productoId);
                    return new IllegalArgumentException("Producto no encontrado con ID: " + productoId);
                });
        
        // Verificar si tiene inventario con stock
        Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(productoId);
        boolean inventarioEliminado = false;
        
        if (inventarioOpt.isPresent()) {
            Inventario inventario = inventarioOpt.get();
            if (inventario.getStockDisponible() > 0) {
                log.warn("Intento de eliminar producto con inventario activo - ID: {}, Stock: {}", 
                    productoId, inventario.getStockDisponible());
                throw new IllegalStateException(
                        "No se puede eliminar el producto porque tiene inventario activo con stock disponible: " 
                        + inventario.getStockDisponible() + " unidades");
            }
            // Si no tiene stock, eliminar el inventario
            inventarioRepositorio.delete(inventario);
            inventarioEliminado = true;
            log.debug("Inventario eliminado para producto: {}", productoId);
        }
        
        // Contar movimientos antes de eliminar (opcional: archivar en lugar de eliminar)
        Page<com.prediccion.apppredicciongm.models.Inventario.Kardex> movimientos = 
                kardexRepositorio.findAllByProducto(productoId, PageRequest.of(0, Integer.MAX_VALUE));
        int movimientosCount = (int) movimientos.getTotalElements();
        
        // Eliminar movimientos asociados (si hay algún método de eliminación masiva, usarlo)
        // Por ahora se mantienen los movimientos para historial
        // kardexRepositorio.deleteAll(movimientos.getContent());
        
        // Eliminar producto
        productoRepositorio.delete(producto);
        log.info("Producto eliminado exitosamente - ID: {}, Nombre: {}, Movimientos archivados: {}", 
            productoId, producto.getNombre(), movimientosCount);
        
        return ProductoEliminadoResponse.builder()
                .mensaje("Producto eliminado exitosamente")
                .productoId(productoId)
                .inventarioEliminado(inventarioEliminado)
                .movimientosArchivados(movimientosCount)
                .build();
    }

    /**
     * Obtiene un producto específico por su ID.
     * 
     * Enriquece la respuesta con información de inventario asociado incluyendo
     * stock disponible, punto de reorden y valor de inventario.
     * 
     * @param productoId ID del producto a obtener
     * @return Respuesta del producto con datos de inventario
     * @throws IllegalArgumentException Si el producto no existe
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(Integer productoId) {
        log.debug("Obteniendo producto con ID: {}", productoId);
        
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado: {}", productoId);
                    return new IllegalArgumentException("Producto no encontrado con ID: " + productoId);
                });
        
        log.debug("Producto encontrado exitosamente: {}", producto.getNombre());
        return enrichProductoResponse(producto);
    }

    /**
     * Lista todos los productos con paginación.
     * 
     * Enriquece cada producto con su información de inventario asociada.
     * 
     * @param pagina Número de página (0-indexado)
     * @param tamanioPagina Cantidad de productos por página
     * @return Página de productos con información de inventario
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listarProductos(int pagina, int tamanioPagina) {
        log.debug("Listando productos - Página: {}, Tamaño: {}", pagina, tamanioPagina);
        
        Page<Producto> productos = productoRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
        log.info("Se encontraron {} productos en total", productos.getTotalElements());
        
        return productos.map(this::enrichProductoResponse);
    }

    /**
     * Busca productos por categoría con paginación.
     * 
     * Filtra productos que pertenecen a una categoría específica y retorna
     * resultados enriquecidos con información de inventario.
     * 
     * @param categoriaId ID de la categoría a filtrar
     * @param pagina Número de página (0-indexado)
     * @param tamanioPagina Cantidad de productos por página
     * @return Página de productos de la categoría
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina) {
        log.debug("Buscando productos por categoría - ID: {}, Página: {}", categoriaId, pagina);
        
        Page<Producto> productos = productoRepositorio.buscarPorCategoria(categoriaId, 
                PageRequest.of(pagina, tamanioPagina));
        
        log.info("Se encontraron {} productos en la categoría: {}", productos.getTotalElements(), categoriaId);
        return productos.map(this::enrichProductoResponse);
    }

    /**
     * Busca productos por nombre con paginación.
     * 
     * Realiza búsqueda case-insensitive por nombre de producto. Retorna resultados
     * enriquecidos con información de inventario.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @param pagina Número de página (0-indexado)
     * @param tamanioPagina Cantidad de productos por página
     * @return Página de productos que coinciden con el nombre
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina) {
        log.debug("Buscando productos por nombre: {}", nombre);
        
        Page<Producto> productos = productoRepositorio.buscarPorNombre(nombre, 
                PageRequest.of(pagina, tamanioPagina));
        
        log.info("Se encontraron {} productos con nombre similar a: {}", productos.getTotalElements(), nombre);
        return productos.map(this::enrichProductoResponse);
    }
    
    /**
     * Enriquece la respuesta del producto con información de inventario.
     * 
     * Busca el inventario asociado al producto y añade datos como stock disponible,
     * stock mínimo, punto de reorden y valor total del inventario.
     * 
     * @param producto Entidad producto a enriquecer
     * @return Respuesta enriquecida con datos de inventario
     */
    private ProductoResponse enrichProductoResponse(Producto producto) {
        log.debug("Enriqueciendo respuesta del producto: {}", producto.getNombre());
        
        ProductoResponse response = productoMapper.toResponse(producto);
        
        // Buscar inventario asociado
        Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(producto.getProductoId());
        
        if (inventarioOpt.isPresent()) {
            Inventario inventario = inventarioOpt.get();
            response.setTieneInventario(true);
            response.setStockDisponible(inventario.getStockDisponible());
            response.setStockMinimo(inventario.getStockMinimo());
            response.setPuntoReorden(inventario.getPuntoReorden());
            response.setEstadoInventario(inventario.getEstado().name());
            
            // Calcular valor de inventario
            BigDecimal valorInventario = producto.getCostoAdquisicion()
                    .multiply(BigDecimal.valueOf(inventario.getStockDisponible()));
            response.setValorInventario(valorInventario);
            
            log.debug("Inventario encontrado - Producto: {}, Stock: {}, Valor: {}", 
                producto.getNombre(), inventario.getStockDisponible(), valorInventario);
        } else {
            response.setTieneInventario(false);
            response.setStockDisponible(0);
            log.debug("Sin inventario registrado para producto: {}", producto.getNombre());
        }
        
        return response;
    }

    /**
     * Lista todos los productos sin paginación.
     * 
     * Retorna la lista completa de productos enriquecida con información de inventario.
     * Usar con cuidado en catálogos grandes; preferir versión paginada para optimización.
     * 
     * @return Lista completa de productos
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        log.info("Listando todos los productos sin paginación");
        
        List<Producto> productos = productoRepositorio.findAll();
        log.info("Total de productos en catálogo: {}", productos.size());
        
        return productos.stream()
                .map(this::enrichProductoResponse)
                .collect(Collectors.toList());
    }
}
