package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ImportacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ImportacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Interfaz de servicio para ImportacionDatos
 */
public interface IImportacionServicio {

    /**
     * Registra una nueva importación
     */
    ImportacionResponse registrarImportacion(ImportacionCreateRequest request);

    /**
     * Actualiza el estado de una importación
     */
    ImportacionResponse actualizarEstadoImportacion(Long importacionId, String estado);

    /**
     * Registra un error en una importación
     */
    void registrarErrorImportacion(Long importacionId, String error);

    /**
     * Obtiene una importación por ID
     */
    ImportacionResponse obtenerImportacionPorId(Long importacionId);

    /**
     * Lista importaciones por tipo de datos
     */
    Page<ImportacionResponse> listarImportacionesPorTipo(String tipoDatos, Pageable pageable);

    /**
     * Lista importaciones por usuario
     */
    Page<ImportacionResponse> listarImportacionesPorUsuario(Integer usuarioId, Pageable pageable);

    /**
     * Obtiene estadísticas de importaciones
     */
    Map<String, Object> obtenerEstadisticasImportacion();

    /**
     * Lista todas las importaciones
     */
    Page<ImportacionResponse> listarTodasLasImportaciones(Pageable pageable);

    /**
     * Elimina una importación
     */
    void eliminarImportacion(Long importacionId);
}
