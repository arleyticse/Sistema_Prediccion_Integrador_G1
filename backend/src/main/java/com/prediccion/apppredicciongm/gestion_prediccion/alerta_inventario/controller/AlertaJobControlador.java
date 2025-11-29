package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service.AlertaInventarioJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para ejecución manual de jobs de alertas.
 * 
 * Permite ejecutar los jobs de generación de alertas sin esperar
 * a la próxima ejecución programada por cron.
 * 
 * Base URL: /api/alertas-inventario/jobs
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@RestController
@RequestMapping("/api/alertas-inventario/jobs")
@RequiredArgsConstructor
@Slf4j
public class AlertaJobControlador {

    private final AlertaInventarioJobService alertaJobService;

    /**
     * Ejecuta manualmente el job de generación de alertas.
     * 
     * POST /api/alertas-inventario/jobs/ejecutar
     * 
     * Útil para:
     * - Pruebas durante desarrollo
     * - Generar alertas inmediatamente sin esperar al cron
     * - Ejecutar el job después de cambiar la configuración
     * 
     * Detecta:
     * - Stock bajo (si está habilitado)
     * - Predicciones vencidas (si está habilitado)
     * - Estacionalidad próxima (si está habilitado)
     * 
     * @return Respuesta con estado de ejecución
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<Map<String, Object>> ejecutarJobManual() {
        log.info("POST /api/alertas-inventario/jobs/ejecutar - Ejecución manual solicitada");
        
        try {
            // Ejecutar job
            alertaJobService.ejecutarGeneracionAutomaticaAlertas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("mensaje", "Job ejecutado exitosamente");
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Job ejecutado manualmente con éxito");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al ejecutar job manualmente: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", "Error al ejecutar job: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Obtiene información sobre la configuración del job.
     * 
     * GET /api/alertas-inventario/jobs/info
     * 
     * @return Configuración actual del job
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoJob() {
        log.info("GET /api/alertas-inventario/jobs/info");
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Consultar application.properties para ver configuración del job");
        response.put("propiedades", Map.of(
            "alerta.job.enabled", "Habilitar/deshabilitar jobs",
            "alerta.job.cron", "Expresión cron para programar ejecución",
            "alerta.job.stock-bajo.enabled", "Habilitar detección de stock bajo",
            "alerta.job.prediccion-vencida.enabled", "Habilitar detección de predicciones vencidas",
            "alerta.job.estacionalidad.enabled", "Habilitar detección de estacionalidad",
            "alerta.job.auto-resolver.enabled", "Habilitar auto-resolución de alertas obsoletas"
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Ejecuta manualmente la auto-resolución de alertas obsoletas.
     * 
     * POST /api/alertas-inventario/jobs/auto-resolver
     * 
     * Verifica todas las alertas pendientes de tipo STOCK_BAJO, PUNTO_REORDEN
     * o STOCK_CRITICO y las resuelve automáticamente si el stock actual
     * es mayor al punto de reorden.
     * 
     * @return Resultado con número de alertas resueltas
     */
    @PostMapping("/auto-resolver")
    public ResponseEntity<Map<String, Object>> ejecutarAutoResolucion() {
        log.info("POST /api/alertas-inventario/jobs/auto-resolver - Ejecución manual solicitada");
        
        try {
            int alertasResueltas = alertaJobService.autoResolverAlertasObsoletas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("alertasResueltas", alertasResueltas);
            response.put("mensaje", alertasResueltas > 0 
                ? "Se resolvieron " + alertasResueltas + " alertas obsoletas"
                : "No se encontraron alertas obsoletas para resolver");
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Auto-resolución ejecutada: {} alertas resueltas", alertasResueltas);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al ejecutar auto-resolución: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", "Error al ejecutar auto-resolución: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
