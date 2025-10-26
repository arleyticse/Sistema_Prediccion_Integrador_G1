package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.request.ParametroAlgoritmoCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.response.ParametroAlgoritmoResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.service.IParametroAlgoritmoServicio;
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

import java.util.List;

/**
 * Controlador REST para gestión de ParametroAlgoritmo
 * Endpoints para configuración de parámetros de algoritmos de predicción
 */
@Slf4j
@RestController
@RequestMapping("/api/parametros-algoritmo")
@RequiredArgsConstructor
@Tag(name = "Parámetros de Algoritmo", description = "Gestión de parámetros configurables de algoritmos de predicción")
public class ParametroAlgoritmoControlador {

    private final IParametroAlgoritmoServicio parametroServicio;

    /**
     * Crea un nuevo parámetro de algoritmo
     */
    @PostMapping
    @Operation(summary = "Crear parámetro", description = "Crea un nuevo parámetro de algoritmo")
    public ResponseEntity<ParametroAlgoritmoResponse> crearParametro(
            @Valid @RequestBody ParametroAlgoritmoCreateRequest request) {
        
        log.info("POST /api/parametros-algoritmo - Parámetro: {}", request.getNombreParametro());
        
        ParametroAlgoritmoResponse respuesta = parametroServicio.crearParametro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Obtiene un parámetro por ID
     */
    @GetMapping("/{parametroId}")
    @Operation(summary = "Obtener parámetro", description = "Retorna los detalles de un parámetro de algoritmo")
    public ResponseEntity<ParametroAlgoritmoResponse> obtenerParametro(
            @Parameter(description = "ID del parámetro")
            @PathVariable Integer parametroId) {
        
        log.info("GET /api/parametros-algoritmo/{}", parametroId);
        
        ParametroAlgoritmoResponse respuesta = parametroServicio.obtenerParametroPorId(parametroId);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene parámetros por tipo de algoritmo
     */
    @GetMapping("/algoritmo/{tipoAlgoritmo}")
    @Operation(summary = "Listar por algoritmo", 
        description = "Lista todos los parámetros de un tipo de algoritmo específico")
    public ResponseEntity<List<ParametroAlgoritmoResponse>> obtenerPorAlgoritmo(
            @Parameter(description = "Tipo de algoritmo (ARIMA, EXPONENTIAL_SMOOTHING, MOVING_AVERAGE)")
            @PathVariable String tipoAlgoritmo) {
        
        log.info("GET /api/parametros-algoritmo/algoritmo/{}", tipoAlgoritmo);
        
        List<ParametroAlgoritmoResponse> respuesta = parametroServicio.obtenerParametrosPorAlgoritmo(tipoAlgoritmo);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene un parámetro específico
     */
    @GetMapping("/especifico")
    @Operation(summary = "Obtener parámetro específico", 
        description = "Obtiene un parámetro por nombre y tipo de algoritmo")
    public ResponseEntity<ParametroAlgoritmoResponse> obtenerEspecifico(
            @Parameter(description = "Nombre del parámetro (alpha, beta, gamma, periodo)")
            @RequestParam String nombreParametro,
            @Parameter(description = "Tipo de algoritmo")
            @RequestParam String tipoAlgoritmo) {
        
        log.info("GET /api/parametros-algoritmo/especifico?nombre={}&tipo={}", nombreParametro, tipoAlgoritmo);
        
        ParametroAlgoritmoResponse respuesta = parametroServicio.obtenerParametroEspecifico(nombreParametro, tipoAlgoritmo);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista todos los parámetros
     */
    @GetMapping
    @Operation(summary = "Listar todos", description = "Lista todos los parámetros de algoritmos")
    public ResponseEntity<Page<ParametroAlgoritmoResponse>> listarTodos(
            @ParameterObject Pageable pageable) {
        
        log.info("GET /api/parametros-algoritmo");
        
        Page<ParametroAlgoritmoResponse> respuesta = parametroServicio.listarTodosLosParametros(pageable);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Actualiza un parámetro
     */
    @PutMapping("/{parametroId}")
    @Operation(summary = "Actualizar parámetro", description = "Actualiza un parámetro existente")
    public ResponseEntity<ParametroAlgoritmoResponse> actualizarParametro(
            @Parameter(description = "ID del parámetro")
            @PathVariable Integer parametroId,
            @Valid @RequestBody ParametroAlgoritmoCreateRequest request) {
        
        log.info("PUT /api/parametros-algoritmo/{}", parametroId);
        
        ParametroAlgoritmoResponse respuesta = parametroServicio.actualizarParametro(parametroId, request);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Valida un parámetro
     */
    @PostMapping("/validar")
    @Operation(summary = "Validar parámetro", description = "Valida si un parámetro cumple con las restricciones")
    public ResponseEntity<Boolean> validarParametro(
            @Valid @RequestBody ParametroAlgoritmoResponse parametro) {
        
        log.info("POST /api/parametros-algoritmo/validar");
        
        Boolean valido = parametroServicio.validarParametro(parametro);
        return ResponseEntity.ok(valido);
    }

    /**
     * Elimina un parámetro
     */
    @DeleteMapping("/{parametroId}")
    @Operation(summary = "Eliminar parámetro", description = "Elimina un parámetro de algoritmo")
    public ResponseEntity<Void> eliminarParametro(
            @Parameter(description = "ID del parámetro")
            @PathVariable Integer parametroId) {
        
        log.info("DELETE /api/parametros-algoritmo/{}", parametroId);
        
        parametroServicio.eliminarParametro(parametroId);
        return ResponseEntity.noContent().build();
    }
}
