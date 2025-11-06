package com.prediccion.apppredicciongm.gestion_inventario.producto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

import java.util.Optional;

@Repository
public interface IProductoRepositorio extends JpaRepository<Producto, Integer> {
    
    @Query("SELECT p FROM Producto p WHERE p.categoria.categoriaId = :categoriaId")
    Page<Producto> buscarPorCategoria(Integer categoriaId, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.nombre = :nombre")
    Page<Producto> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * Busca un producto por nombre exacto (case-insensitive)
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nombre)")
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe un producto con el nombre dado
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
