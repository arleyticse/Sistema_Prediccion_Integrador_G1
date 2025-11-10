package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service.IReporteDemandaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones de normalización de demanda.
 * 
 * Permite ejecutar normalización manual de datos históricos
 * cuando se importan movimientos masivos de kardex.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-07
 */
@RestController
@RequestMapping("/api/normalizacion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NormalizacionController {

    private final IReporteDemandaService reporteDemandaService;

    /**
     * Normaliza datos históricos de demanda para todos los productos.
     * 
     * Útil cuando se importan movimientos de kardex masivos (históricos)
     * que el listener no procesó porque no son eventos en tiempo real.
     * 
     * @param dias Número de días hacia atrás a procesar (default: 365)
     * @return Respuesta con cantidad de registros procesados
     */
    @PostMapping("/historico")
    public ResponseEntity<Map<String, Object>> normalizarHistorico(
            @RequestParam(defaultValue = "365") int dias) {
        
        log.info("🔄 [API] Iniciando normalización histórica masiva. Días: {}", dias);
        
        try {
            long inicio = System.currentTimeMillis();
            
            // Ejecutar normalización para todos los productos
            int registrosProcesados = reporteDemandaService.normalizarDemandaTodos(dias);
            
            long fin = System.currentTimeMillis();
            long tiempoEjecucion = fin - inicio;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("registrosProcesados", registrosProcesados);
            response.put("diasProcesados", dias);
            response.put("tiempoEjecucionMs", tiempoEjecucion);
            response.put("mensaje", String.format(
                "Normalización histórica completada: %d registros procesados en %d ms",
                registrosProcesados, tiempoEjecucion
            ));
            
            log.info("✅ [API] Normalización histórica completada. Registros: {} Tiempo: {}ms", 
                    registrosProcesados, tiempoEjecucion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [API] Error en normalización histórica: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error en normalización: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene estadísticas de normalización actual.
     * 
     * @return Estadísticas de registros de demanda
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("📊 [API] Obteniendo estadísticas de normalización");
        
        try {
            // Aquí podrías agregar consultas para obtener estadísticas
            Map<String, Object> stats = new HashMap<>();
            stats.put("success", true);
            stats.put("mensaje", "Estadísticas disponibles");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ [API] Error obteniendo estadísticas: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error obteniendo estadísticas: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
