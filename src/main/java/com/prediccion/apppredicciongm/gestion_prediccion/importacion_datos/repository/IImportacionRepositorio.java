package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository;

import com.prediccion.apppredicciongm.models.ImportacionDatos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceso a datos de ImportacionDatos
 * Proporciona métodos de consulta para importaciones de datos
 */
@Repository
public interface IImportacionRepositorio extends JpaRepository<ImportacionDatos, Long> {

    /**
     * Busca importaciones por tipo de datos
     * @param tipoDatos el tipo de datos a buscar
     * @param pageable información de paginación
     * @return página de importaciones del tipo especificado
     */
    Page<ImportacionDatos> findByTipoDatos(String tipoDatos, Pageable pageable);

    /**
     * Busca importaciones por estado
     * @param estadoImportacion el estado de la importación a buscar
     * @param pageable información de paginación
     * @return página de importaciones con el estado especificado
     */
    Page<ImportacionDatos> findByEstadoImportacion(String estadoImportacion, Pageable pageable);

    /**
     * Busca importaciones por usuario
     * @param usuarioId el ID del usuario
     * @param pageable información de paginación
     * @return página de importaciones del usuario
     */
    @Query("SELECT i FROM ImportacionDatos i WHERE i.usuario.usuarioId = :usuarioId")
    Page<ImportacionDatos> findByUsuarioId(@Param("usuarioId") Integer usuarioId, Pageable pageable);

    /**
     * Busca todas las importaciones paginadas
     * @param pageable información de paginación
     * @return página de todas las importaciones
     */
    @NonNull
    Page<ImportacionDatos> findAll(@NonNull Pageable pageable);
}
