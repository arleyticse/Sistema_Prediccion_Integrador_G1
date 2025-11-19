package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service.AnalisisEstacionalidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para ejecutar análisis de estacionalidad manualmente
 */
@RestController
@RequestMapping("/api/estacionalidad/ejecutar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Análisis Estacionalidad Manual", description = "Ejecutar análisis de estacionalidad manualmente")
public class EjecucionEstacionalidadController {

    private final AnalisisEstacionalidadService analisisService;

    /**
     * Ejecuta el análisis de estacionalidad manualmente
     */
    @PostMapping("/manual")
    @Operation(summary = "Ejecutar análisis estacionalidad", 
               description = "Ejecuta manualmente el análisis de estacionalidad para todos los productos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> ejecutarAnalisisManual() {
        
        try {
            log.info("Iniciando análisis de estacionalidad manual");
            analisisService.analizarEstacionalidadAutomatico();
            
            return ResponseEntity.ok("Análisis de estacionalidad ejecutado correctamente");
            
        } catch (Exception e) {
            log.error("Error ejecutando análisis manual: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error: " + e.getMessage());
        }
    }

    /**
     * Verifica el estado del análisis de estacionalidad
     */
    @GetMapping("/estado")
    @Operation(summary = "Verificar estado análisis", 
               description = "Verifica cuántos productos tienen análisis de estacionalidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    public ResponseEntity<String> verificarEstado() {
        
        try {
            // Aquí podrías implementar una consulta para verificar el estado
            return ResponseEntity.ok("Endpoint de verificación disponible");
            
        } catch (Exception e) {
            log.error("Error verificando estado: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error: " + e.getMessage());
        }
    }
}