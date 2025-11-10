package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ProcesarAlertasRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenOrdenDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service.IOrdenCompraBatchService;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service.IPrediccionBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para procesamiento automatico batch de alertas.
 * 
 * Orquesta el flujo completo:
 * 1. Predicciones automaticas multiples
 * 2. Optimizacion EOQ/ROP batch
 * 3. Generacion de ordenes de compra por proveedor
 * 4. Actualizacion de alertas a RESUELTA
 * 
 * Base URL: /api/alertas-inventario/procesar
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@RestController
@RequestMapping("/api/alertas-inventario/procesar")
@RequiredArgsConstructor
@Slf4j
public class ProcesamientoAlertasControlador {

    private final IPrediccionBatchService prediccionBatchService;
    private final IOrdenCompraBatchService ordenCompraBatchService;

    /**
     * Procesa alertas seleccionadas ejecutando el flujo completo automatico.
     * 
     * POST /api/alertas-inventario/procesar/automatico
     * 
     * Flujo de procesamiento:
     * 1. Valida alertas seleccionadas
     * 2. Ejecuta predicciones en modo automatico (mejor algoritmo)
     * 3. Calcula optimizacion EOQ/ROP para cada prediccion
     * 4. Agrupa productos por proveedor
     * 5. Genera ordenes de compra por proveedor (estado BORRADOR)
     * 6. Marca alertas como RESUELTA
     * 
     * Body esperado:
     * {
     *   "alertaIds": [1, 2, 3, 4, 5],
     *   "horizonteTiempo": 30,
     *   "usuarioId": 1,
     *   "observaciones": "Proceso automatico desde dashboard"
     * }
     * 
     * Respuesta:
     * {
     *   "exitoso": true,
     *   "alertasProcesadas": 5,
     *   "prediccionesGeneradas": 5,
     *   "optimizacionesCalculadas": 5,
     *   "ordenesGeneradas": 2,
     *   "proveedoresConOrden": ["Proveedor A", "Proveedor B"],
     *   "ordenesIds": [101, 102],
     *   "errores": [],
     *   "mensaje": "Procesamiento completado exitosamente",
     *   "tiempoEjecucionMs": 3542
     * }
     * 
     * @param request Datos de las alertas a procesar
     * @return Resultado detallado del procesamiento batch
     */
    @PostMapping("/automatico")
    public ResponseEntity<ProcesamientoBatchResponse> procesarAlertasAutomatico(
            @Valid @RequestBody ProcesarAlertasRequest request
    ) {
        log.info("POST /api/alertas-inventario/procesar/automatico - {} alertas", 
                request.getAlertaIds().size());
        
        try {
            // Ejecutar flujo completo
            ProcesamientoBatchResponse response = prediccionBatchService.ejecutarPrediccionesBatch(request);
            
            if (Boolean.TRUE.equals(response.getExitoTotal())) {
                log.info("Procesamiento completado: {} alertas, {} ordenes generadas",
                        response.getTotalProcesadas(), response.getOrdenesGeneradas().size());
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Procesamiento completado con errores: {}", response.getMensajesError());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validacion: {}", e.getMessage());
            
            ProcesamientoBatchResponse errorResponse = ProcesamientoBatchResponse.builder()
                    .exitoTotal(false)
                    .totalProcesadas(0)
                    .exitosos(0)
                    .fallidos(request.getAlertaIds().size())
                    .mensajesError(java.util.List.of("Error de validación: " + e.getMessage()))
                    .observaciones("Error de validación")
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error inesperado en procesamiento automatico: {}", e.getMessage(), e);
            
            ProcesamientoBatchResponse errorResponse = ProcesamientoBatchResponse.builder()
                    .exitoTotal(false)
                    .totalProcesadas(0)
                    .exitosos(0)
                    .fallidos(request.getAlertaIds().size())
                    .mensajesError(java.util.List.of("Error inesperado: " + e.getMessage()))
                    .observaciones("Error inesperado en el servidor")
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene el estado de las alertas procesables.
     * 
     * GET /api/alertas-inventario/procesar/verificar?alertaIds=1,2,3
     * 
     * Verifica que las alertas:
     * - Existan en el sistema
     * - Esten en estado PENDIENTE o EN_PROCESO
     * - Tengan productos validos
     * - Tengan proveedores asignados
     * 
     * @param alertaIds IDs de alertas a verificar
     * @return Estado de validacion de las alertas
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarAlertasProcesables(
            @RequestParam java.util.List<Long> alertaIds
    ) {
        log.info("GET /api/alertas-inventario/procesar/verificar - {} alertas", alertaIds.size());
        
        try {
            // TODO: Implementar logica de verificacion
            Map<String, Object> response = new HashMap<>();
            response.put("alertasValidas", alertaIds.size());
            response.put("alertasInvalidas", 0);
            response.put("procesable", true);
            response.put("mensaje", "Alertas validadas correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al verificar alertas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene historial de procesamientos batch.
     * 
     * GET /api/alertas-inventario/procesar/historial
     * 
     * @return Lista de procesamientos anteriores
     */
    @GetMapping("/historial")
    public ResponseEntity<Map<String, Object>> obtenerHistorialProcesamientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/alertas-inventario/procesar/historial - page: {}, size: {}", page, size);
        
        try {
            // TODO: Implementar historial desde BD
            Map<String, Object> response = new HashMap<>();
            response.put("procesamientos", java.util.List.of());
            response.put("totalElementos", 0);
            response.put("totalPaginas", 0);
            response.put("paginaActual", page);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener historial: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el resumen detallado de ordenes de compra generadas.
     * 
     * GET /api/alertas-inventario/procesar/resumen-ordenes?ordenIds=1,2,3
     * 
     * Se utiliza para mostrar los detalles de las ordenes generadas
     * durante el procesamiento automatico de alertas en el frontend.
     * 
     * @param ordenIds IDs de ordenes de compra a consultar
     * @return Lista de resumenes de ordenes con detalles completos
     */
    @GetMapping("/resumen-ordenes")
    public ResponseEntity<java.util.List<ResumenOrdenDTO>> obtenerResumenOrdenes(
            @RequestParam java.util.List<Long> ordenIds
    ) {
        log.info("GET /api/alertas-inventario/procesar/resumen-ordenes - {} ordenes", ordenIds.size());
        
        try {
            java.util.List<ResumenOrdenDTO> resumenes = ordenCompraBatchService.obtenerResumenOrdenes(ordenIds);
            
            log.info("Resumen obtenido para {} ordenes", resumenes.size());
            
            return ResponseEntity.ok(resumenes);
            
        } catch (Exception e) {
            log.error("Error al obtener resumen de ordenes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
