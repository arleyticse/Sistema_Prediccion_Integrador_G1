package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.GenerarPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.enums.AlgoritmoSmileML;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.mapper.PrediccionMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.ISmartPredictorService;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predicciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Predicciones", description = "API para gestión de predicciones con SMILE ML")
public class PrediccionController {

    private final IPrediccionRepositorio prediccionRepositorio;
    private final PrediccionMapper prediccionMapper;
    private final ISmartPredictorService smartPredictorService;
    private final IProductoRepositorio productoRepositorio;

    @GetMapping
    @Operation(summary = "Obtener predicciones (paginado)")
    public ResponseEntity<Page<PrediccionResponse>> obtenerPredicciones(Pageable pageable) {
        // Se retorna la lista paginada ordenada por fecha de ejecución descendente
        Pageable pageableWithSort = org.springframework.data.domain.PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.Direction.DESC,
            "fechaEjecucion"
        );
        Page<Prediccion> page = prediccionRepositorio.findAll(pageableWithSort);
        List<PrediccionResponse> content = prediccionMapper.prediccionListToResponseList(page.getContent());
        Page<PrediccionResponse> response = new PageImpl<>(content, pageableWithSort, page.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener predicciones por producto")
    public ResponseEntity<List<PrediccionResponse>> obtenerPorProducto(@PathVariable Integer productoId) {
        Optional<com.prediccion.apppredicciongm.models.Inventario.Producto> productoOpt = productoRepositorio.findById(productoId);
        if (productoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Producto producto = productoOpt.get();
        if (producto == null) return ResponseEntity.notFound().build();
        List<Prediccion> predicciones = prediccionRepositorio.findPrediccionesVigentesPorProducto(producto);
        List<PrediccionResponse> responses = prediccionMapper.prediccionListToResponseList(predicciones);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/ultima/{productoId}")
    @Operation(summary = "Obtener última predicción de un producto")
    public ResponseEntity<PrediccionResponse> obtenerUltima(@PathVariable Integer productoId) {
        Optional<com.prediccion.apppredicciongm.models.Inventario.Producto> productoOpt = productoRepositorio.findById(productoId);
        if (productoOpt.isEmpty()) return ResponseEntity.notFound().build();
        Producto producto = productoOpt.get();
        if (producto == null) return ResponseEntity.notFound().build();
        Optional<Prediccion> opt = prediccionRepositorio.findFirstByProductoOrderByFechaEjecucionDesc(producto);
        return opt.map(p -> ResponseEntity.ok(prediccionMapper.prediccionToResponse(p))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{prediccionId}")
    @Operation(summary = "Obtener una predicción por ID")
    public ResponseEntity<PrediccionResponse> obtenerPorId(@PathVariable Integer prediccionId) {
        Optional<Prediccion> opt = prediccionRepositorio.findById(prediccionId);
        return opt.map(p -> ResponseEntity.ok(prediccionMapper.prediccionToResponse(p))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{prediccionId}")
    @Operation(summary = "Eliminar una predicción")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> eliminarPrediccion(@PathVariable Integer prediccionId) {
        if (!prediccionRepositorio.existsById(prediccionId)) {
            return ResponseEntity.notFound().build();
        }
        prediccionRepositorio.deleteById(prediccionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar predicción (delegar a SMILE si aplica)")
    @PreAuthorize("hasAnyRole('GERENTE', 'OPERARIO')")
    public ResponseEntity<?> generarPrediccion(@RequestBody GenerarPrediccionRequest request) {
        // Only support generation via SMILE in this controller for now
        String algoritmo = request.getAlgoritmo();
        // If requested algoritmo maps to a SMILE algorithm, route to SmartPredictorService
        Set<String> smileAlgorithms = Arrays.stream(AlgoritmoSmileML.values()).map(AlgoritmoSmileML::getCodigo).collect(Collectors.toSet());
        try {
            if (algoritmo != null && smileAlgorithms.contains(algoritmo.toUpperCase())) {
                // map to Smart request
                com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.SmartPrediccionRequest sreq = new com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.SmartPrediccionRequest();
                sreq.setIdProducto(request.getProductoId().longValue());
                sreq.setDetectarEstacionalidad(true);
                sreq.setAlgoritmoSeleccionado(algoritmo);
                sreq.setHorizonteTiempo(request.getHorizonteTiempo());
                var resultado = smartPredictorService.generarPrediccionInteligente(sreq);
                // The smart predictor persists a Prediccion entity. Try to retrieve it and return DTO.
                Optional<com.prediccion.apppredicciongm.models.Inventario.Producto> productoOpt = productoRepositorio.findById(request.getProductoId());
                if (productoOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Producto no encontrado"));
                Producto producto = productoOpt.get();
                if (producto != null) {
                    Optional<Prediccion> opt = prediccionRepositorio.findByProductoAndAlgoritmoUsadoAndHorizonteTiempo(producto, algoritmo.toUpperCase(), request.getHorizonteTiempo());
                    if (opt.isPresent()) {
                        Prediccion saved = opt.get();
                        PrediccionResponse resp = prediccionMapper.prediccionToResponse(saved);
                        return ResponseEntity.ok(resp);
                    }
                }
                // Fallback: return SmartPrediccionResponse if entity not found
                return ResponseEntity.ok(resultado);
            } else {
                // Non-SMILE algorithms: respond with bad request explaining deprecation
                return ResponseEntity.badRequest().body(Map.of("error", "ALGORITHM_NOT_SUPPORTED", "message", "Generación con algoritmos legacy (SMA/SES/HOLT_WINTERS) está obsoleta. Use SMILE (AUTO, LINEAR_REGRESSION, ARIMA, RANDOM_FOREST, GRADIENT_BOOSTING)"));
            }
        } catch (Exception e) {
            log.error("Error generando predicción: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/algoritmos")
    @Operation(summary = "Obtener lista de algoritmos SMILE ML disponibles")
    public ResponseEntity<Map<String, String>> obtenerAlgoritmos() {
        Map<String, String> map = Arrays.stream(AlgoritmoSmileML.values())
                .collect(Collectors.toMap(AlgoritmoSmileML::getCodigo, AlgoritmoSmileML::getNombre));
        return ResponseEntity.ok(map);
    }

    @GetMapping("/algoritmos/info")
    @Operation(summary = "Obtener información detallada de algoritmos SMILE ML")
    public ResponseEntity<List<Map<String, Object>>> obtenerInfoAlgoritmos() {
        List<Map<String, Object>> info = Arrays.stream(AlgoritmoSmileML.values())
                .map(a -> Map.<String, Object>of(
                        "codigo", a.getCodigo(),
                        "nombre", a.getNombre(),
                        "descripcion", a.getDescripcion(),
                        "minimoRegistros", a.getMinimoRegistrosRequeridos()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(info);
    }
}
