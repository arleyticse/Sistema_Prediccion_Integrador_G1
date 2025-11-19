package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalculoObtimizacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service.ICalculoObtimizacionServicio;
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

/**
 * Controlador REST para gestión de CalculoObtimizacion
 * Endpoints para cálculos de EOQ, ROP y optimización de inventario
 */
@Slf4j
@RestController
@RequestMapping("/api/calculo-optimizacion")
@RequiredArgsConstructor
@Tag(name = "Cálculo de Optimización", description = "Gestión de cálculos de EOQ y ROP para optimización de inventario")
public class CalculoObtimizacionControlador {

    private final ICalculoObtimizacionServicio calculoServicio;

    /**
     * Calcula optimización para un producto
     */
    @PostMapping("/calcular")
    @Operation(summary = "Calcular optimización de inventario", 
        description = "Calcula EOQ (cantidad óptima) y ROP (punto de reorden) para un producto")
    public ResponseEntity<CalculoOptimizacionResponse> calcularObtimizacion(
            @Parameter(description = "ID del producto")
            @RequestParam Integer productoId,
            @Valid @RequestBody CalculoObtimizacionCreateRequest request) {
        
        log.info("POST /api/calculo-optimizacion/calcular - Producto: {}", productoId);
        
        CalculoOptimizacionResponse respuesta = calculoServicio.calcularObtimizacion(productoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Obtiene un cálculo por ID
     */
    @GetMapping("/{calculoId}")
    @Operation(summary = "Obtener cálculo por ID", description = "Retorna los detalles de un cálculo de optimización")
    public ResponseEntity<CalculoOptimizacionResponse> obtenerCalculo(
            @Parameter(description = "ID del cálculo")
            @PathVariable Integer calculoId) {
        
        log.info("GET /api/calculo-optimizacion/{}", calculoId);
        
        CalculoOptimizacionResponse respuesta = calculoServicio.obtenerCalculoPorId(calculoId);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el último cálculo de un producto
     */
    @GetMapping("/producto/{productoId}/ultimo")
    @Operation(summary = "Obtener último cálculo de un producto", 
        description = "Retorna el cálculo más reciente de un producto específico")
    public ResponseEntity<CalculoOptimizacionResponse> obtenerUltimoCalculo(
            @Parameter(description = "ID del producto")
            @PathVariable Integer productoId) {
        
        log.info("GET /api/calculo-optimizacion/producto/{}/ultimo", productoId);
        
        CalculoOptimizacionResponse respuesta = calculoServicio.obtenerUltimoCalculoPorProducto(productoId);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista cálculos de un producto
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Listar cálculos de un producto", 
        description = "Retorna lista paginada de cálculos de un producto")
    public ResponseEntity<Page<CalculoOptimizacionResponse>> listarCalculosPorProducto(
            @Parameter(description = "ID del producto")
            @PathVariable Integer productoId,
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/calculo-optimizacion/producto/{}", productoId);
        
        Page<CalculoOptimizacionResponse> respuesta = calculoServicio.listarCalculosPorProducto(productoId, pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista todos los cálculos
     */
    @GetMapping
    @Operation(summary = "Listar todos los cálculos", 
        description = "Retorna lista paginada de todos los cálculos de optimización")
    public ResponseEntity<Page<CalculoOptimizacionResponse>> listarTodos(
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/calculo-optimizacion");
        
        Page<CalculoOptimizacionResponse> respuesta = calculoServicio.listarTodosLosCalculos(pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Actualiza un cálculo
     */
    @PutMapping("/{calculoId}")
    @Operation(summary = "Actualizar cálculo", description = "Actualiza un cálculo de optimización existente")
    public ResponseEntity<CalculoOptimizacionResponse> actualizarCalculo(
            @Parameter(description = "ID del cálculo")
            @PathVariable Integer calculoId,
            @Valid @RequestBody CalculoObtimizacionCreateRequest request) {
        
        log.info("PUT /api/calculo-optimizacion/{}", calculoId);
        
        CalculoOptimizacionResponse respuesta = calculoServicio.actualizarCalculo(calculoId, request);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Elimina un cálculo
     */
    @DeleteMapping("/{calculoId}")
    @Operation(summary = "Eliminar cálculo", description = "Elimina un cálculo de optimización")
    public ResponseEntity<Void> eliminarCalculo(
            @Parameter(description = "ID del cálculo")
            @PathVariable Integer calculoId) {
        
        log.info("DELETE /api/calculo-optimizacion/{}", calculoId);
        
        calculoServicio.eliminarCalculo(calculoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recalcula para todos los productos
     */
    @PostMapping("/recalcular-todos")
    @Operation(summary = "Recalcular todos", description = "Recalcula optimizaciones para todos los productos")
    public ResponseEntity<String> recalcularTodos() {
        
        log.info("POST /api/calculo-optimizacion/recalcular-todos");
        
        calculoServicio.recalcularParaTodasLasPredicciones();
        return ResponseEntity.ok("Recálculo iniciado correctamente");
    }
}
