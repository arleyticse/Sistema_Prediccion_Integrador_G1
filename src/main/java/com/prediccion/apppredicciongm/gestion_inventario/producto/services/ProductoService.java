package com.prediccion.apppredicciongm.gestion_inventario.producto.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoUpdateRequest;
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

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoServicio {

    private final IProductoRepositorio productoRepositorio;
    private final ICategoriaRepositorio categoriaRepositorio;
    private final IUnidadMedidaRepositorio unidadMedidaRepositorio;
    private final IInventarioRepositorio inventarioRepositorio;
    private final IKardexRepositorio kardexRepositorio;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoCreateRequest request) {
        // Validar categoría
        Categoria categoria = categoriaRepositorio.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada con ID: " + request.getCategoriaId()));
        
        // Validar unidad de medida
        UnidadMedida unidadMedida = unidadMedidaRepositorio.findById(request.getUnidadMedidaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidad de medida no encontrada con ID: " + request.getUnidadMedidaId()));
        
        Producto producto = productoMapper.toEntity(request);
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        
        // Calcular costo de mantenimiento anual (costo mantenimiento * 365 días)
        BigDecimal costoMantenimientoAnual = request.getCostoMantenimiento()
                .multiply(BigDecimal.valueOf(365));
        producto.setCostoMantenimientoAnual(costoMantenimientoAnual);
        
        Producto productoGuardado = productoRepositorio.save(producto);
        
        ProductoResponse response = productoMapper.toResponse(productoGuardado);
        response.setTieneInventario(false);
        response.setStockDisponible(0);
        
        return response;
    }

    @Override
    @Transactional
    public ProductoResponse actualizarProducto(Integer productoId, ProductoCreateRequest request) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + productoId));
        
        productoMapper.updateEntityFromDto(request, producto);
        
        // Recalcular costo de mantenimiento anual si se actualizó el costo de mantenimiento
        if (request.getCostoMantenimiento() != null) {
            BigDecimal costoMantenimientoAnual = request.getCostoMantenimiento()
                    .multiply(BigDecimal.valueOf(365));
            producto.setCostoMantenimientoAnual(costoMantenimientoAnual);
        }
        
        Producto productoActualizado = productoRepositorio.save(producto);
        return enrichProductoResponse(productoActualizado);
    }

    @Override
    @Transactional
    public ProductoEliminadoResponse eliminarProducto(Integer productoId) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + productoId));
        
        // Verificar si tiene inventario con stock
        Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(productoId);
        boolean inventarioEliminado = false;
        
        if (inventarioOpt.isPresent()) {
            Inventario inventario = inventarioOpt.get();
            if (inventario.getStockDisponible() > 0) {
                throw new IllegalStateException(
                        "No se puede eliminar el producto porque tiene inventario activo con stock disponible: " 
                        + inventario.getStockDisponible() + " unidades");
            }
            // Si no tiene stock, eliminar el inventario
            inventarioRepositorio.delete(inventario);
            inventarioEliminado = true;
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
        
        return ProductoEliminadoResponse.builder()
                .mensaje("Producto eliminado exitosamente")
                .productoId(productoId)
                .inventarioEliminado(inventarioEliminado)
                .movimientosArchivados(movimientosCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(Integer productoId) {
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + productoId));
        return enrichProductoResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listarProductos(int pagina, int tamanioPagina) {
        Page<Producto> productos = productoRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
        return productos.map(this::enrichProductoResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina) {
        Page<Producto> productos = productoRepositorio.buscarPorCategoria(categoriaId, 
                PageRequest.of(pagina, tamanioPagina));
        return productos.map(this::enrichProductoResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina) {
        Page<Producto> productos = productoRepositorio.buscarPorNombre(nombre, 
                PageRequest.of(pagina, tamanioPagina));
        return productos.map(this::enrichProductoResponse);
    }
    
    /**
     * Enriquece la respuesta del producto con información de inventario
     */
    private ProductoResponse enrichProductoResponse(Producto producto) {
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
        } else {
            response.setTieneInventario(false);
            response.setStockDisponible(0);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        List<Producto> productos = productoRepositorio.findAll();
        return productos.stream()
                .map(this::enrichProductoResponse)
                .collect(Collectors.toList());
    }
}
