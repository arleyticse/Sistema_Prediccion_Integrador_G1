package com.prediccion.apppredicciongm.gestion_configuracion.repository;

import com.prediccion.apppredicciongm.models.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar la configuración de la empresa.
 * 
 * Solo existe un registro (id = 1) que contiene toda la configuración.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Repository
public interface IConfiguracionEmpresaRepositorio extends JpaRepository<ConfiguracionEmpresa, Integer> {

    /**
     * Obtiene la configuración de la empresa (siempre id = 1).
     * 
     * @return Optional con la configuración o vacío si no existe
     */
    @Query("SELECT c FROM ConfiguracionEmpresa c WHERE c.id = 1")
    Optional<ConfiguracionEmpresa> obtenerConfiguracion();

    /**
     * Verifica si existe la configuración.
     * 
     * @return true si existe configuración
     */
    default boolean existeConfiguracion() {
        return existsById(1);
    }
}
