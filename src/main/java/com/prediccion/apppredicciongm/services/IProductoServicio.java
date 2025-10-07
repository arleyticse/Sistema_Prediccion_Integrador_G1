package com.prediccion.apppredicciongm.services;

import java.util.List;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

public interface IProductoServicio {
    void registrarProducto(Producto producto);
    Producto obtenerProductoPorId(Integer id);
    List<Producto> listarProductos();
    void actualizarProducto(Producto producto);
}
