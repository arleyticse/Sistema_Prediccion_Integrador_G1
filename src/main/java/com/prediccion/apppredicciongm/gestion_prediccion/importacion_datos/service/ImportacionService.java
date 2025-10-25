package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ImportacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.errors.ImportacionNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.mapper.ImportacionMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de ImportacionDatos
 * Auditoría y rastreo de importaciones de datos CSV/Excel
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportacionService implements IImportacionServicio {

    private final IImportacionRepositorio importacionRepositorio;
    private final ImportacionMapper mapper;

    private static final String IMPORTACION_NO_ENCONTRADA = "Importación no encontrada con ID: ";

    /**
     * Registra una nueva importación
     */
    @Override
    public ImportacionResponse registrarImportacion(ImportacionCreateRequest request) {
        log.info("Registrando nueva importación - Tipo: {}, Archivo: {}", 
            request.getTipoDatos(), request.getNombreArchivo());

        // Convertir DTO enum a modelo enum
        com.prediccion.apppredicciongm.enums.TipoDatosImportacion tipoDatos = 
            com.prediccion.apppredicciongm.enums.TipoDatosImportacion.valueOf(request.getTipoDatos().name());

        ImportacionDatos importacion = ImportacionDatos.builder()
                .tipoDatos(tipoDatos)
                .nombreArchivo(request.getNombreArchivo())
                .rutaArchivo(request.getRutaArchivo())
                .fechaImportacion(LocalDateTime.now())
                .estadoImportacion(com.prediccion.apppredicciongm.enums.EstadoImportacion.EN_PROCESO)
                .registrosProcesados(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .tiempoProcesamiento(0L)
                .observaciones(request.getObservaciones())
                .build();

        ImportacionDatos guardada = importacionRepositorio.save(importacion);

        log.info("Importación registrada con ID: {}", guardada.getImportacionId());

        return mapper.toResponse(guardada);
    }

    /**
     * Actualiza el estado de una importación
     */
    @Override
    public ImportacionResponse actualizarEstadoImportacion(Long importacionId, String estado) {
        log.info("Actualizando estado de importación {} a: {}", importacionId, estado);

        ImportacionDatos importacion = importacionRepositorio.findById(importacionId)
                .orElseThrow(() -> new ImportacionNoEncontradaException(
                    IMPORTACION_NO_ENCONTRADA + importacionId));

        try {
            importacion.setEstadoImportacion(com.prediccion.apppredicciongm.enums.EstadoImportacion.valueOf(estado.toUpperCase()));
            importacion.setFechaActualizacion(LocalDateTime.now());

            ImportacionDatos actualizada = importacionRepositorio.save(importacion);
            return mapper.toResponse(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("Estado inválido: {}", estado);
            throw new IllegalArgumentException("Estado de importación inválido: " + estado, e);
        }
    }

    /**
     * Registra un error en una importación
     */
    @Override
    public void registrarErrorImportacion(Long importacionId, String error) {
        log.error("Registrando error en importación {}: {}", importacionId, error);

        ImportacionDatos importacion = importacionRepositorio.findById(importacionId)
                .orElseThrow(() -> new ImportacionNoEncontradaException(
                    IMPORTACION_NO_ENCONTRADA + importacionId));

        String erroresActuales = importacion.getErrores() != null ? 
            importacion.getErrores() : "";
        
        String erroresActualizados = erroresActuales.isEmpty() ? 
            error : erroresActuales + " | " + error;

        importacion.setErrores(erroresActualizados);
        importacion.setEstadoImportacion(com.prediccion.apppredicciongm.enums.EstadoImportacion.FALLIDA);
        importacion.setFechaActualizacion(LocalDateTime.now());

        importacionRepositorio.save(importacion);
    }

    /**
     * Obtiene una importación por ID
     */
    @Override
    @Transactional(readOnly = true)
    public ImportacionResponse obtenerImportacionPorId(Long importacionId) {
        log.debug("Obteniendo importación ID: {}", importacionId);

        ImportacionDatos importacion = importacionRepositorio.findById(importacionId)
                .orElseThrow(() -> new ImportacionNoEncontradaException(
                    IMPORTACION_NO_ENCONTRADA + importacionId));

        return mapper.toResponse(importacion);
    }

    /**
     * Lista importaciones por tipo de datos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ImportacionResponse> listarImportacionesPorTipo(String tipoDatos, Pageable pageable) {
        log.debug("Listando importaciones por tipo: {}", tipoDatos);

        return importacionRepositorio.findByTipoDatos(tipoDatos, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Lista importaciones por usuario
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ImportacionResponse> listarImportacionesPorUsuario(Integer usuarioId, Pageable pageable) {
        log.debug("Listando importaciones por usuario: {}", usuarioId);

        return importacionRepositorio.findByUsuarioId(usuarioId, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Obtiene estadísticas de importaciones
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasImportacion() {
        log.info("Obteniendo estadísticas de importaciones");

        List<ImportacionDatos> todas = importacionRepositorio.findAll();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalImportaciones", todas.size());
        estadisticas.put("importacionesExitosas", 
            todas.stream().filter(i -> com.prediccion.apppredicciongm.enums.EstadoImportacion.COMPLETADA.equals(i.getEstadoImportacion())).count());
        estadisticas.put("importacionesEnError", 
            todas.stream().filter(i -> com.prediccion.apppredicciongm.enums.EstadoImportacion.FALLIDA.equals(i.getEstadoImportacion())).count());
        estadisticas.put("registrosTotalesProcesados", 
            todas.stream().mapToInt(ImportacionDatos::getRegistrosProcesados).sum());
        estadisticas.put("tiempoPromedioProcesamiento", 
            todas.stream().mapToLong(ImportacionDatos::getTiempoProcesamiento).average().orElse(0.0));

        return estadisticas;
    }

    /**
     * Lista todas las importaciones
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ImportacionResponse> listarTodasLasImportaciones(Pageable pageable) {
        log.debug("Listando todas las importaciones");

        return importacionRepositorio.findAll(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Elimina una importación
     */
    @Override
    public void eliminarImportacion(Long importacionId) {
        log.info("Eliminando importación ID: {}", importacionId);

        if (!importacionRepositorio.existsById(importacionId)) {
            throw new ImportacionNoEncontradaException(
                IMPORTACION_NO_ENCONTRADA + importacionId);
        }

        importacionRepositorio.deleteById(importacionId);
        log.info("Importación eliminada correctamente");
    }
}
