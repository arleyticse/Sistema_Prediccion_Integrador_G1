package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ImportacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service.IImportacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestión de ImportacionDatos
 * Endpoints para auditoría y rastreo de importaciones
 */
@Slf4j
@RestController
@RequestMapping("/api/importaciones")
@RequiredArgsConstructor
@Tag(name = "Importación de Datos", description = "Gestión de auditoría y rastreo de importaciones de datos")
public class ImportacionControlador {

    private final IImportacionServicio importacionServicio;

    /**
     * Registra una nueva importación
     */
    @PostMapping
    @Operation(summary = "Registrar importación", description = "Registra una nueva importación de datos")
    public ResponseEntity<ImportacionResponse> registrarImportacion(
            @Valid @RequestBody ImportacionCreateRequest request) {
        
        log.info("POST /api/importaciones - Registrando: {}", request.getNombreArchivo());
        
        ImportacionResponse respuesta = importacionServicio.registrarImportacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Obtiene una importación por ID
     */
    @GetMapping("/{importacionId}")
    @Operation(summary = "Obtener importación", description = "Retorna los detalles de una importación")
    public ResponseEntity<ImportacionResponse> obtenerImportacion(
            @Parameter(description = "ID de la importación")
            @PathVariable Long importacionId) {
        
        log.info("GET /api/importaciones/{}", importacionId);
        
        ImportacionResponse respuesta = importacionServicio.obtenerImportacionPorId(importacionId);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista importaciones por tipo
     */
    @GetMapping("/tipo/{tipoDatos}")
    @Operation(summary = "Listar por tipo", description = "Lista importaciones por tipo de datos")
    public ResponseEntity<Page<ImportacionResponse>> listarPorTipo(
            @Parameter(description = "Tipo de datos (KARDEX, DEMANDA, PROVEEDORES, ESTACIONALIDAD)")
            @PathVariable String tipoDatos,
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/importaciones/tipo/{}", tipoDatos);
        
        Page<ImportacionResponse> respuesta = importacionServicio.listarImportacionesPorTipo(tipoDatos, pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista importaciones por usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar por usuario", description = "Lista importaciones de un usuario específico")
    public ResponseEntity<Page<ImportacionResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario")
            @PathVariable Integer usuarioId,
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/importaciones/usuario/{}", usuarioId);
        
        Page<ImportacionResponse> respuesta = importacionServicio.listarImportacionesPorUsuario(usuarioId, pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista todas las importaciones
     */
    @GetMapping
    @Operation(summary = "Listar todas", description = "Lista todas las importaciones registradas")
    public ResponseEntity<Page<ImportacionResponse>> listarTodas(
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/importaciones");
        
        Page<ImportacionResponse> respuesta = importacionServicio.listarTodasLasImportaciones(pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene estadísticas de importaciones
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas", description = "Obtiene estadísticas generales de importaciones")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        
        log.info("GET /api/importaciones/estadisticas");
        
        Map<String, Object> respuesta = importacionServicio.obtenerEstadisticasImportacion();
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Actualiza el estado de una importación
     */
    @PatchMapping("/{importacionId}/estado")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de una importación")
    public ResponseEntity<ImportacionResponse> actualizarEstado(
            @Parameter(description = "ID de la importación")
            @PathVariable Long importacionId,
            @Parameter(description = "Nuevo estado (EN_PROCESO, COMPLETADO, ERROR)")
            @RequestParam String estado) {
        
        log.info("PATCH /api/importaciones/{}/estado", importacionId);
        
        ImportacionResponse respuesta = importacionServicio.actualizarEstadoImportacion(importacionId, estado);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Registra un error en una importación
     */
    @PostMapping("/{importacionId}/error")
    @Operation(summary = "Registrar error", description = "Registra un error en una importación")
    public ResponseEntity<String> registrarError(
            @Parameter(description = "ID de la importación")
            @PathVariable Long importacionId,
            @Parameter(description = "Descripción del error")
            @RequestParam String error) {
        
        log.info("POST /api/importaciones/{}/error", importacionId);
        
        importacionServicio.registrarErrorImportacion(importacionId, error);
        return ResponseEntity.ok("Error registrado correctamente");
    }

    /**
     * Elimina una importación
     */
    @DeleteMapping("/{importacionId}")
    @Operation(summary = "Eliminar importación", description = "Elimina una importación")
    public ResponseEntity<Void> eliminarImportacion(
            @Parameter(description = "ID de la importación")
            @PathVariable Long importacionId) {
        
        log.info("DELETE /api/importaciones/{}", importacionId);
        
        importacionServicio.eliminarImportacion(importacionId);
        return ResponseEntity.noContent().build();
    }
}
