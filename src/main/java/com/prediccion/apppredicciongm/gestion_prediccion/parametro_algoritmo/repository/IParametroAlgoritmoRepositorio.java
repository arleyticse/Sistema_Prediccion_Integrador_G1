package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.repository;

import com.prediccion.apppredicciongm.models.ParametroAlgoritmo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de ParametroAlgoritmo
 */
@Repository
public interface IParametroAlgoritmoRepositorio extends JpaRepository<ParametroAlgoritmo, Integer> {

    /**
     * Busca parámetros por tipo de algoritmo
     */
    List<ParametroAlgoritmo> findByTipoAlgoritmo(String tipoAlgoritmo);

    /**
     * Busca un parámetro específico por nombre y tipo de algoritmo
     */
    Optional<ParametroAlgoritmo> findByNombreParametroAndTipoAlgoritmo(String nombreParametro, String tipoAlgoritmo);

    /**
     * Busca parámetros activos
     */
    List<ParametroAlgoritmo> findByActivo(Boolean activo);

    /**
     * Busca todos los parámetros paginados
     */
    Page<ParametroAlgoritmo> findAll(Pageable pageable);
}
