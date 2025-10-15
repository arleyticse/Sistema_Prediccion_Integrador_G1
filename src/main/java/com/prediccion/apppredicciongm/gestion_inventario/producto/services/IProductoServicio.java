package com.prediccion.apppredicciongm.gestion_inventario.producto.services;


import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

public interface IProductoServicio {
    Producto crearProducto(Producto producto);
    Producto actualizarProducto(Integer productoId, Producto producto);
    void eliminarProducto(Integer productoId);
    Page<Producto> listarProductos(int pagina, int tamanioPagina);
    Producto obtenerProductoPorId(Integer productoId);
    Page<Producto> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina);
    Page<Producto> buscarPorNombre(String nombre, int pagina, int tamanioPagina);
}
