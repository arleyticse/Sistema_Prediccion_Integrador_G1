package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.controller;

import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.IPrediccionService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.GenerarPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.mapper.PrediccionMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.DatosInsuficientesException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.PrediccionNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.ProductoNoEncontradoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de predicción ARIMA.
 * Proporciona endpoints para generar y consultar predicciones de demanda.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@RestController
@RequestMapping("/api/predicciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Predicciones", description = "API para gestionar predicciones ARIMA de demanda")
public class PrediccionControlador {

    private final IPrediccionService prediccionService;
    private final PrediccionMapper prediccionMapper;

    /**
     * Genera una nueva predicción ARIMA para un producto.
     *
     * @param productoId ID del producto
     * @param request datos de la solicitud de predicción
     * @return la predicción generada
     */
    @PostMapping("/generar/{productoId}")
    @Operation(summary = "Generar predicción ARIMA", 
               description = "Genera una nueva predicción de demanda para un producto usando algoritmo ARIMA")
    public ResponseEntity<PrediccionResponse> generarPrediccion(
            @PathVariable Integer productoId,
            @RequestBody(required = false) GenerarPrediccionRequest request) {
        
        log.info("📊 POST /generar/{} - Generando predicción", productoId);
        
        // Usar valores por defecto si no se envía request
        int diasProcesar = (request != null && request.getDiasPronostico() > 0) ? request.getDiasPronostico() : 30;
        
        try {
            Prediccion prediccion = prediccionService.generarPrediccion(productoId, diasProcesar);
            PrediccionResponse response = prediccionMapper.prediccionToResponse(prediccion);
            
            log.info("✅ Predicción generada exitosamente para producto {}", productoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ProductoNoEncontradoException e) {
            log.error("❌ Producto no encontrado: {}", productoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DatosInsuficientesException e) {
            log.error("❌ Datos insuficientes para predicción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Obtiene todas las predicciones con paginación.
     *
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @return página de predicciones
     */
    @GetMapping
    @Operation(summary = "Obtener todas las predicciones",
               description = "Retorna todas las predicciones con paginación")
    public ResponseEntity<Page<PrediccionResponse>> obtenerTodasLasPredicciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("📋 GET / - Obteniendo todas las predicciones (page={}, size={})", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Prediccion> prediccionesPage = prediccionService.obtenerTodasLasPredicciones(pageable);
            Page<PrediccionResponse> responsePage = prediccionesPage.map(prediccionMapper::prediccionToResponse);
            
            log.info("✅ Se obtuvieron {} predicciones de {} total", 
                    responsePage.getNumberOfElements(), responsePage.getTotalElements());
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            log.error("❌ Error al obtener predicciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las predicciones de un producto.
     *
     * @param productoId ID del producto
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @return lista de predicciones
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener predicciones por producto",
               description = "Retorna todas las predicciones de un producto específico con paginación")
    public ResponseEntity<List<PrediccionResponse>> obtenerPrediccionesPorProducto(
            @PathVariable Integer productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("📋 GET /producto/{} - Obteniendo predicciones (page={}, size={})", productoId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Prediccion> predicciones = prediccionService.obtenerPrediccionesByProducto(productoId, pageable);
            List<PrediccionResponse> responses = prediccionMapper.prediccionListToResponseList(predicciones);
            
            log.info("✅ Se obtuvieron {} predicciones para producto {}", responses.size(), productoId);
            return ResponseEntity.ok(responses);
        } catch (ProductoNoEncontradoException e) {
            log.error("❌ Producto no encontrado: {}", productoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Obtiene una predicción específica por ID.
     *
     * @param prediccionId ID de la predicción
     * @return la predicción solicitada
     */
    @GetMapping("/{prediccionId}")
    @Operation(summary = "Obtener predicción por ID",
               description = "Retorna los detalles de una predicción específica")
    public ResponseEntity<PrediccionResponse> obtenerPrediccion(@PathVariable Long prediccionId) {
        log.info("🔍 GET /{} - Obteniendo predicción", prediccionId);
        
        try {
            // Obtener desde base de datos
            return ResponseEntity.ok().build();
        } catch (PrediccionNoEncontradaException e) {
            log.error("❌ Predicción no encontrada: {}", prediccionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Obtiene la última predicción de un producto.
     *
     * @param productoId ID del producto
     * @return la última predicción
     */
    @GetMapping("/ultima/{productoId}")
    @Operation(summary = "Obtener última predicción",
               description = "Retorna la predicción más reciente de un producto")
    public ResponseEntity<PrediccionResponse> obtenerUltimaPrediccion(@PathVariable Integer productoId) {
        log.info("📌 GET /ultima/{} - Obteniendo última predicción", productoId);
        
        try {
            Prediccion prediccion = prediccionService.obtenerUltimaPrediccion(productoId);
            PrediccionResponse response = prediccionMapper.prediccionToResponse(prediccion);
            
            log.info("✅ Última predicción obtenida para producto {}", productoId);
            return ResponseEntity.ok(response);
        } catch (ProductoNoEncontradoException e) {
            log.error("❌ Producto no encontrado: {}", productoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PrediccionNoEncontradaException e) {
            log.error("❌ No hay predicciones disponibles: {}", productoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Elimina una predicción.
     *
     * @param prediccionId ID de la predicción
     */
    @DeleteMapping("/{prediccionId}")
    @Operation(summary = "Eliminar predicción",
               description = "Elimina una predicción de la base de datos")
    public ResponseEntity<Void> eliminarPrediccion(@PathVariable Long prediccionId) {
        log.info("🗑️ DELETE /{} - Eliminando predicción", prediccionId);
        
        try {
            prediccionService.eliminarPrediccion(prediccionId);
            log.info("✅ Predicción eliminada");
            return ResponseEntity.noContent().build();
        } catch (PrediccionNoEncontradaException e) {
            log.error("❌ Predicción no encontrada: {}", prediccionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Manejador de excepciones para ProductoNoEncontradoException.
     */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<String> handleProductoNoEncontrado(ProductoNoEncontradoException e) {
        log.error("❌ Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /**
     * Manejador de excepciones para DatosInsuficientesException.
     */
    @ExceptionHandler(DatosInsuficientesException.class)
    public ResponseEntity<String> handleDatosInsuficientes(DatosInsuficientesException e) {
        log.error("❌ Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * Manejador de excepciones para PrediccionNoEncontradaException.
     */
    @ExceptionHandler(PrediccionNoEncontradaException.class)
    public ResponseEntity<String> handlePrediccionNoEncontrada(PrediccionNoEncontradaException e) {
        log.error("❌ Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
