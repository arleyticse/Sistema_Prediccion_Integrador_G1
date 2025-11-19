package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service.AnalisisEstacionalidadService;
import com.prediccion.apppredicciongm.models.AnalisisEstacionalidad;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestión de estacionalidad
 */
@Slf4j
@RestController
@RequestMapping("/api/estacionalidad")
@RequiredArgsConstructor
@Tag(name = "Estacionalidad", description = "Gestión de análisis de estacionalidad de productos")
public class EstacionalidadController {

    private final AnalisisEstacionalidadService analisisEstacionalidadService;

    /**
     * Obtiene el análisis de estacionalidad para un producto específico
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener estacionalidad de producto", 
               description = "Obtiene el análisis de estacionalidad para un producto específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    public ResponseEntity<AnalisisEstacionalidad> obtenerEstacionalidadProducto(@PathVariable Long productoId) {
        try {
            log.info("[ESTACIONALIDAD] Consultando estacionalidad para producto ID: {}", productoId);
            
            AnalisisEstacionalidad estacionalidad = analisisEstacionalidadService.obtenerEstacionalidad(productoId);
            
            if (estacionalidad != null) {
                return ResponseEntity.ok(estacionalidad);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error consultando estacionalidad producto {}: {}", productoId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Analiza la estacionalidad de un producto específico
     */
    @PostMapping("/analizar/{productoId}")
    @Operation(summary = "Analizar estacionalidad de producto", 
               description = "Ejecuta análisis de estacionalidad para un producto específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<AnalisisEstacionalidad> analizarEstacionalidadProducto(@PathVariable Long productoId) {
        try {
            log.info("[ESTACIONALIDAD] Iniciando análisis de estacionalidad para producto ID: {}", productoId);
            
            AnalisisEstacionalidad analisis = analisisEstacionalidadService.analizarEstacionalidadProducto(productoId);
            
            if (analisis != null) {
                return ResponseEntity.ok(analisis);
            } else {
                return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Datos insuficientes para análisis")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error analizando estacionalidad producto {}: {}", productoId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .header("X-Error-Message", e.getMessage())
                .build();
        }
    }

    /**
     * Fuerza análisis completo de estacionalidad para todos los productos
     */
    @PostMapping("/analizar-todo")
    @Operation(summary = "Analizar estacionalidad completa", 
               description = "Ejecuta análisis de estacionalidad para todos los productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> analizarEstacionalidadCompleta() {
        try {
            log.info("[ESTACIONALIDAD] Iniciando análisis completo de estacionalidad...");
            
            // Ejecutar análisis asíncrono
            analisisEstacionalidadService.forzarAnalisisCompleto();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Análisis de estacionalidad iniciado. El proceso se ejecutará en segundo plano.",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error iniciando análisis completo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", "Error iniciando análisis: " + e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
                ));
        }
    }

    /**
     * Obtiene estadísticas generales de estacionalidad
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de estacionalidad", 
               description = "Obtiene estadísticas generales del análisis de estacionalidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasEstacionalidad() {
        try {
            // Aquí podrías implementar estadísticas como:
            // - Total de productos analizados
            // - Productos con estacionalidad detectada
            // - Meses de mayor/menor demanda general
            // - Fecha del último análisis
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Endpoint de estadísticas en desarrollo",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error obteniendo estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}