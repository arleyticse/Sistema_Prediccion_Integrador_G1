package com.prediccion.apppredicciongm.gestion_inventario.producto.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductoService implements IProductoServicio {

    private final IProductoRepositorio productoRepositorio;
    @Override
    public Producto actualizarProducto(Integer productoId, Producto producto) {
        producto.setProductoId(productoId);
        return productoRepositorio.save(producto);
    }

    @Override
    public Page<Producto> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina) {
        return productoRepositorio.buscarPorCategoria(categoriaId, PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public Page<Producto> buscarPorNombre(String nombre, int pagina, int tamanioPagina) {
        return productoRepositorio.buscarPorNombre(nombre, PageRequest.of(pagina, tamanioPagina));
    }

    @Override   
    public Producto crearProducto(Producto producto) {
        return productoRepositorio.save(producto);
    }

    @Override
    public void eliminarProducto(Integer productoId) {
        productoRepositorio.deleteById(productoId);
    }

    @Override
    public Page<Producto> listarProductos(int pagina, int tamanioPagina) {
        return productoRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public Producto obtenerProductoPorId(Integer productoId) {
        return productoRepositorio.findById(productoId).orElse(null);
    }
}
