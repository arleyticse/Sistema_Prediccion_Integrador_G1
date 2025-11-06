package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalcularOptimizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.OptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service.IOptimizacionInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la optimización de inventario (EOQ/ROP)
 * 
 * Endpoints:
 * - POST /api/optimizacion/calcular → Calcular optimización EOQ/ROP
 * - GET /api/optimizacion/prediccion/{id} → Obtener optimización guardada
 * 
 * @author Sistema de Predicción
 * @version 1.0
 */
@RestController
@RequestMapping("/api/optimizacion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OptimizacionControlador {
    
    private final IOptimizacionInventarioService optimizacionService;
    
    /**
     * Calcular optimización EOQ/ROP para una predicción
     * 
     * @param request Datos de costos y parámetros
     * @return Resultados de optimización (EOQ, ROP, Stock Seguridad, Costos)
     */
    @PostMapping("/calcular")
    public ResponseEntity<OptimizacionResponse> calcularOptimizacion(
            @Valid @RequestBody CalcularOptimizacionRequest request) {
        
        log.info("Solicitud de cálculo de optimización para predicción ID: {}", request.getPrediccionId());
        log.debug("Parámetros recibidos - nivelServicio={}%, costoPedido={}, costoAlmacenamiento={}, costoUnitario={}, leadTime={}",
                  request.getNivelServicioDeseado() != null ? request.getNivelServicioDeseado() * 100 : null,
                  request.getCostoPedido() != null ? request.getCostoPedido() : "BD", 
                  request.getCostoAlmacenamiento() != null ? request.getCostoAlmacenamiento() : "BD",
                  request.getCostoUnitario() != null ? request.getCostoUnitario() : "BD",
                  request.getTiempoEntregaDias() != null ? request.getTiempoEntregaDias() + " días" : "BD");
        
        try {
            OptimizacionResponse response = optimizacionService.calcularOptimizacion(request);
            
            log.info("Optimización calculada exitosamente: EOQ={}, ROP={}, Stock Seguridad={}",
                     response.getCantidadEconomicaPedido(),
                     response.getPuntoReorden(),
                     response.getStockSeguridad());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            log.error("Error al calcular optimización", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener el último cálculo de optimización guardado para una predicción
     * 
     * @param prediccionId ID de la predicción
     * @return Optimización guardada o 404 si no existe
     */
    @GetMapping("/prediccion/{prediccionId}")
    public ResponseEntity<OptimizacionResponse> obtenerOptimizacionPorPrediccion(
            @PathVariable Long prediccionId) {
        
        log.info("Solicitud de optimización guardada para predicción ID: {}", prediccionId);
        
        try {
            OptimizacionResponse response = optimizacionService.obtenerOptimizacionPorPrediccion(prediccionId);
            
            if (response == null) {
                log.warn("No se encontró optimización guardada para predicción ID: {}", prediccionId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener optimización guardada", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint de prueba para validar fórmulas
     * 
     * @param demanda Demanda anual
     * @param costoPedido Costo por pedido
     * @param costoAlmacenamiento Costo de almacenamiento unitario/año
     * @return EOQ calculado
     */
    @GetMapping("/test/eoq")
    public ResponseEntity<Double> testEOQ(
            @RequestParam Double demanda,
            @RequestParam Double costoPedido,
            @RequestParam Double costoAlmacenamiento) {
        
        log.debug("Test EOQ: D={}, S={}, H={}", demanda, costoPedido, costoAlmacenamiento);
        
        Double eoq = optimizacionService.calcularEOQ(demanda, costoPedido, costoAlmacenamiento);
        
        return ResponseEntity.ok(eoq);
    }
    
    /**
     * Endpoint de prueba para ROP
     * 
     * @param demandaDiaria Demanda diaria promedio
     * @param leadTime Tiempo de entrega en días
     * @param stockSeguridad Stock de seguridad
     * @return ROP calculado
     */
    @GetMapping("/test/rop")
    public ResponseEntity<Double> testROP(
            @RequestParam Double demandaDiaria,
            @RequestParam Integer leadTime,
            @RequestParam Double stockSeguridad) {
        
        log.debug("Test ROP: d={}, L={}, SS={}", demandaDiaria, leadTime, stockSeguridad);
        
        Double rop = optimizacionService.calcularROP(demandaDiaria, leadTime, stockSeguridad);
        
        return ResponseEntity.ok(rop);
    }
    
    /**
     * Endpoint de prueba para Stock de Seguridad
     * 
     * @param nivelServicio Nivel de servicio (0.80-0.99)
     * @param desviacion Desviación estándar de la demanda
     * @param leadTime Tiempo de entrega en días
     * @return Stock de seguridad calculado
     */
    @GetMapping("/test/stock-seguridad")
    public ResponseEntity<Double> testStockSeguridad(
            @RequestParam Double nivelServicio,
            @RequestParam Double desviacion,
            @RequestParam Integer leadTime) {
        
        log.debug("Test Stock Seguridad: nivelServicio={}, σ={}, L={}", 
                  nivelServicio, desviacion, leadTime);
        
        Double factorZ = optimizacionService.obtenerFactorZ(nivelServicio);
        Double ss = optimizacionService.calcularStockSeguridad(factorZ, desviacion, leadTime);
        
        log.debug("Factor Z: {}, Stock Seguridad: {}", factorZ, ss);
        
        return ResponseEntity.ok(ss);
    }
}
