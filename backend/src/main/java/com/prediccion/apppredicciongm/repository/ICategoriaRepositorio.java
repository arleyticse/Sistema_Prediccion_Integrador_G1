package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;

import java.util.Optional;

@Repository
public interface ICategoriaRepositorio extends JpaRepository<Categoria, Integer> {

    /**
     * Busca una categoría por su nombre exacto (case-insensitive)
     */
    @Query("SELECT c FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre)")
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe una categoría con el nombre dado
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
