package com.prediccion.apppredicciongm.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.repository.InventarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioServicio implements IInventarioServicio {

    private final InventarioRepositorio inventarioRepositorio;

    @Override
    public Inventario actualizarInventario(Integer inventarioId, Inventario inventario) {
        inventario.setInventarioId(inventarioId);
        return inventarioRepositorio.save(inventario);
    }

    @Override
    public Inventario crearInventario(Inventario inventario) {
        return inventarioRepositorio.save(inventario);
    }

    @Override
    public void eliminarInventario(Integer inventarioId) {
        inventarioRepositorio.deleteById(inventarioId);
    }

    @Override
    public Page<Inventario> listarInventarios(int pagina, int tamanioPagina) {
        return inventarioRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public void ajustarStock(Integer productoId, Integer cantidad) {

    }

    @Override
    public Page<Inventario> buscarPorProducto(Integer productoId, int pagina, int tamanioPagina) {
        return inventarioRepositorio.findByProducto(productoId, PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public BigDecimal calcularValorInventario(Integer productoId) {
        Inventario inventario = inventarioRepositorio.findById(productoId).orElseThrow(
                () -> new IllegalArgumentException("Producto no encontrado"));
        Producto producto = inventario.getProducto();

        if (inventario.getStockDisponible() != null && producto.getCostoAdquisicion() != null) {
            return producto.getCostoAdquisicion().multiply(BigDecimal.valueOf(inventario.getStockDisponible()));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean necesitaReorden(Integer productoId) {
        Inventario inventario = inventarioRepositorio.findByProducto(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para el producto ID: " + productoId));

        return inventario.getStockDisponible() <= inventario.getPuntoReorden();
    }

    @Override
    public Inventario obtenerInventarioPorId(Integer inventarioId) {
        return inventarioRepositorio.findById(inventarioId).orElse(null);
    }

}
