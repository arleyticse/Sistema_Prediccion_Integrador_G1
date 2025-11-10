package com.prediccion.apppredicciongm.gestion_inventario.producto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de la entidad Producto.
 * 
 * Proporciona métodos para CRUD y búsquedas específicas sobre la tabla productos.
 * 
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface IProductoRepositorio extends JpaRepository<Producto, Integer> {
    
    /**
     * Busca productos por categoría con paginación.
     * 
     * @param categoriaId ID de la categoría a filtrar
     * @param pageable Configuración de paginación
     * @return Página con productos de la categoría especificada
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.categoriaId = :categoriaId")
    Page<Producto> buscarPorCategoria(Integer categoriaId, Pageable pageable);

    /**
     * Busca productos por nombre con paginación.
     * Realiza búsqueda case-insensitive mediante LIKE.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @param pageable Configuración de paginación
     * @return Página con productos que coinciden con el nombre
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Producto> buscarPorNombre(@Param("nombre") String nombre, Pageable pageable);
    
    /**
     * Busca todos los productos por nombre con paginación (búsqueda global).
     * 
     * Realiza búsqueda case-insensitive mediante LIKE sobre la base de datos completa,
     * pero devuelve resultados paginados para mantener escalabilidad y rendimiento
     * incluso en catálogos muy grandes. Los resultados se ordenan alfabéticamente.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos que coinciden con el criterio
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY p.nombre ASC")
    Page<Producto> buscarGlobalPorNombre(@Param("nombre") String nombre, Pageable pageable);

    /**
     * Busca un producto por nombre exacto (case-insensitive)
     * 
     * @param nombre Nombre exacto del producto a buscar
     * @return Optional con el producto si existe
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nombre)")
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe un producto con el nombre dado (case-insensitive)
     * 
     * @param nombre Nombre del producto a verificar
     * @return true si existe un producto con ese nombre, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
