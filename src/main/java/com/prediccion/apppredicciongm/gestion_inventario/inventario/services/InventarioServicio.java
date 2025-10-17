package com.prediccion.apppredicciongm.gestion_inventario.inventario.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

@Service
@RequiredArgsConstructor
public class InventarioServicio implements IInventarioServicio {

    private final IInventarioRepositorio inventarioRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final InventarioMapper inventarioMapper;

    @Override
    @Transactional
    public InventarioResponse crearInventario(InventarioCreateRequest request) {
        // Verificar que el producto existe
        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + request.getProductoId()));
        
        // Verificar que no exista ya un inventario para este producto
        if (inventarioRepositorio.findByProducto(request.getProductoId()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un inventario para el producto con ID: " + request.getProductoId());
        }
        
        Inventario inventario = inventarioMapper.toEntity(request);
        inventario.setProducto(producto);
        inventario.setEstado(EstadoInventario.NORMAL);
        inventario.setDiasSinVenta(0);
        
        Inventario inventarioGuardado = inventarioRepositorio.save(inventario);
        return inventarioMapper.toResponse(inventarioGuardado);
    }

    @Override
    @Transactional
    public InventarioResponse actualizarInventario(Integer inventarioId, InventarioUpdateRequest request) {
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        
        inventarioMapper.updateEntityFromDto(request, inventario);
        Inventario inventarioActualizado = inventarioRepositorio.save(inventario);
        return inventarioMapper.toResponse(inventarioActualizado);
    }

    @Override
    @Transactional
    public void eliminarInventario(Integer inventarioId) {
        if (!inventarioRepositorio.existsById(inventarioId)) {
            throw new IllegalArgumentException("Inventario no encontrado con ID: " + inventarioId);
        }
        inventarioRepositorio.deleteById(inventarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse obtenerInventarioPorId(Integer inventarioId) {
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        return inventarioMapper.toResponse(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> listarInventarios(int pagina, int tamanioPagina) {
        Page<Inventario> inventarios = inventarioRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse buscarPorProducto(Integer productoId) {
        Inventario inventario = inventarioRepositorio.findByProducto(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró inventario para el producto con ID: " + productoId));
        return inventarioMapper.toResponse(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> buscarPorCategoria(Integer categoriaId) {
        List<Inventario> inventarios = inventarioRepositorio.findByCategoria(categoriaId);
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> buscarPorEstado(EstadoInventario estado, int pagina, int tamanioPagina) {
        Page<Inventario> inventarios = inventarioRepositorio.findByEstado(estado, 
                PageRequest.of(pagina, tamanioPagina));
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina) {
        Page<Inventario> inventarios = inventarioRepositorio.findByNombreProductoContaining(nombre, 
                PageRequest.of(pagina, tamanioPagina));
        return inventarios.map(inventarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> buscarPorRangoStock(Integer minStock, Integer maxStock) {
        List<Inventario> inventarios = inventarioRepositorio.findByRangoStock(minStock, maxStock);
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasStockBajo() {
        List<Inventario> inventarios = inventarioRepositorio.findInventariosBajoStockReorden();
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasCriticas() {
        List<Inventario> inventarios = inventarioRepositorio.findInventariosCriticos();
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasAgotados() {
        List<Inventario> inventarios = inventarioRepositorio.findInventariosAgotados();
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioAlertaResponse> obtenerAlertasSinMovimiento(Integer diasSinMovimiento) {
        List<Inventario> inventarios = inventarioRepositorio.findInventariosSinMovimiento(diasSinMovimiento);
        return inventarios.stream()
                .map(inventarioMapper::toAlertaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponse> obtenerInventariosSobreMaximo() {
        List<Inventario> inventarios = inventarioRepositorio.findInventariosSobreStockMaximo();
        return inventarios.stream()
                .map(inventarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventarioResponse ajustarStock(AjusteStockRequest request) {
        Inventario inventario = inventarioRepositorio.findById(request.getInventarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + request.getInventarioId()));
        
        Integer nuevoStock = inventario.getStockDisponible() + request.getCantidad();
        
        if (nuevoStock < 0) {
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
        return inventarioMapper.toResponse(inventarioActualizado);
    }

    @Override
    @Transactional
    public void actualizarStockDesdeMovimiento(Integer productoId, Integer cantidad, boolean esEntrada) {
        Inventario inventario = inventarioRepositorio.findByProducto(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró inventario para el producto con ID: " + productoId));
        
        Integer nuevoStock;
        if (esEntrada) {
            nuevoStock = inventario.getStockDisponible() + cantidad;
        } else {
            nuevoStock = inventario.getStockDisponible() - cantidad;
            if (nuevoStock < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente. Stock actual: " + inventario.getStockDisponible() + 
                        ", Cantidad solicitada: " + cantidad);
            }
        }
        
        inventario.setStockDisponible(nuevoStock);
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        inventario.setDiasSinVenta(0); // Resetear días sin venta
        
        // Actualizar estado
        if (nuevoStock == 0) {
            inventario.setEstado(EstadoInventario.CRITICO);
        } else if (inventario.bajoPuntoMinimo()) {
            inventario.setEstado(EstadoInventario.BAJO);
        } else {
            inventario.setEstado(EstadoInventario.NORMAL);
        }
        
        inventarioRepositorio.save(inventario);
    }

    @Override
    @Transactional
    public void actualizarFechaUltimoMovimiento(Integer inventarioId) {
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        inventarioRepositorio.save(inventario);
    }

    @Override
    @Transactional
    public void actualizarDiasSinVenta(Integer inventarioId, Integer dias) {
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        
        inventario.setDiasSinVenta(dias);
        inventarioRepositorio.save(inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResumenResponse obtenerResumenGeneral() {
        Long totalProductos = inventarioRepositorio.count();
        Long productosActivos = inventarioRepositorio.countProductosActivos();
        Long productosInactivos = inventarioRepositorio.countProductosInactivos();
        Long productosConStockBajo = inventarioRepositorio.countProductosStockBajo();
        Long productosAgotados = inventarioRepositorio.countProductosAgotados();
        Long productosSinMovimiento = inventarioRepositorio.countProductosSinMovimiento(30);
        Double valorTotal = inventarioRepositorio.calcularValorTotalInventario();
        Long stockTotal = inventarioRepositorio.sumStockTotalDisponible();
        
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
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        return inventario.necesitaReorden();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaBajoPuntoMinimo(Integer inventarioId) {
        Inventario inventario = inventarioRepositorio.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventario no encontrado con ID: " + inventarioId));
        return inventario.bajoPuntoMinimo();
    }
}
