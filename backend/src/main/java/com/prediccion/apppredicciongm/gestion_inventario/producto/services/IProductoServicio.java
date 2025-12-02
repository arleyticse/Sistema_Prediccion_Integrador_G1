package com.prediccion.apppredicciongm.gestion_inventario.producto.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoEliminadoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;

/**
 * Interfaz para el servicio de gestión de productos.
 * 
 * Define las operaciones CRUD y búsqueda de productos del inventario,
 * incluyendo filtrado por categoría y nombre.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface IProductoServicio {
    
    /**
     * Crea un nuevo producto en el catálogo.
     * 
     * @param request Datos del producto a crear
     * @return Producto creado con ID generado
     */
    ProductoResponse crearProducto(ProductoCreateRequest request);
    
    /**
     * Actualiza la información de un producto existente.
     * 
     * @param productoId ID del producto a actualizar
     * @param request Nuevos datos del producto
     * @return Producto actualizado
     */
    ProductoResponse actualizarProducto(Integer productoId, ProductoCreateRequest request);
    
    /**
     * Elimina un producto del catálogo.
     * 
     * @param productoId ID del producto a eliminar
     * @return Confirmación de eliminación
     */
    ProductoEliminadoResponse eliminarProducto(Integer productoId);
    
    /**
     * Obtiene una página de todos los productos.
     * 
     * @param pagina Número de página (base 0)
     * @param tamanioPagina Cantidad de registros por página
     * @return Página con productos
     */
    Page<ProductoResponse> listarProductos(int pagina, int tamanioPagina);

    /**
     * Obtiene una página de todos los productos con ordenamiento personalizado.
     * 
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página con productos
     */
    Page<ProductoResponse> listarProductos(Pageable pageable);
    
    /**
     * Obtiene un producto por su ID.
     * 
     * @param productoId ID del producto
     * @return Datos del producto
     */
    ProductoResponse obtenerProductoPorId(Integer productoId);
    
    /**
     * Busca productos por categoría.
     * 
     * @param categoriaId ID de la categoría
     * @param pagina Número de página
     * @param tamanioPagina Cantidad de registros por página
     * @return Página con productos de la categoría
     */
    Page<ProductoResponse> buscarPorCategoria(Integer categoriaId, int pagina, int tamanioPagina);
    
    /**
     * Busca productos por nombre (búsqueda parcial).
     * 
     * @param nombre Texto a buscar en el nombre
     * @param pagina Número de página
     * @param tamanioPagina Cantidad de registros por página
     * @return Página con productos que coinciden
     */
    Page<ProductoResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina);
    
    /**
     * Busca todos los productos por nombre con paginación (búsqueda global).
     * 
     * Realiza una búsqueda case-insensitive en todos los registros de la base de datos,
     * no solo en la página actual. Mantiene escalabilidad mediante paginación.
     * Los resultados se ordenan alfabéticamente.
     * 
     * @param nombre Texto a buscar en el nombre
     * @param pagina Número de página (base 0)
     * @param tamanioPagina Cantidad de registros por página
     * @return Página con productos que coinciden
     */
    Page<ProductoResponse> buscarGlobalPorNombre(String nombre, int pagina, int tamanioPagina);
    
    /**
     * Obtiene la lista completa de todos los productos.
     * 
     * @return Lista de todos los productos
     */
    List<ProductoResponse> listarTodos();
    
    /**
     * Obtiene lista simplificada de productos para dropdowns y selects.
     * Optimizado para rendimiento con campos mínimos.
     * 
     * @return Lista de productos con solo id, nombre y categoría
     */
    List<com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoSimpleResponse> listarTodosSimple();
}
