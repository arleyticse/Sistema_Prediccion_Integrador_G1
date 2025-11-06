package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;

import java.util.Optional;

@Repository
public interface IUnidadMedidaRepositorio extends JpaRepository<UnidadMedida, Integer> {

    /**
     * Busca una unidad de medida por su abreviatura exacta (case-insensitive)
     */
    @Query("SELECT um FROM UnidadMedida um WHERE LOWER(um.abreviatura) = LOWER(:abreviatura)")
    Optional<UnidadMedida> findByAbreviaturaIgnoreCase(String abreviatura);

    /**
     * Verifica si existe una unidad de medida con la abreviatura dada
     */
    boolean existsByAbreviaturaIgnoreCase(String abreviatura);
}
