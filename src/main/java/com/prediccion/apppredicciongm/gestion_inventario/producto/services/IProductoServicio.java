package com.prediccion.apppredicciongm.gestion_inventario.producto.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoEliminadoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;

public interface IProductoServicio {
    ProductoResponse crearProducto(ProductoCreateRequest request);
    ProductoResponse actualizarProducto(Integer productoId, ProductoCreateRequest request);
    ProductoEliminadoResponse eliminarProducto(Integer productoId);
    Page<ProductoResponse> listarProductos(int pagina, int tamanioPagina);
    ProductoResponse obtenerProductoPorId(Integer productoId);
    Page<ProductoResponse> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina);
    Page<ProductoResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina);
    List<ProductoResponse> listarTodos();
}
