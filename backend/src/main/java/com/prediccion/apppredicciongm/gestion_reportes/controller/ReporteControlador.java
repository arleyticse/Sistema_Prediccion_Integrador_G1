package com.prediccion.apppredicciongm.gestion_reportes.controller;

import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReporteInventarioDTO;
import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReportePrediccionDTO;
import com.prediccion.apppredicciongm.gestion_reportes.service.IReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador REST para generación de reportes del sistema
 * 
 * Proporciona endpoints para obtener datos consolidados de predicciones
 * e inventario que pueden ser utilizados para generar PDFs en el frontend
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "API para generación de reportes de predicciones e inventario")
public class ReporteControlador {

    private final IReporteService reporteService;

    /**
     * Obtiene datos consolidados de predicciones para generar reporte
     * 
     * @param fechaInicio fecha inicial del periodo (opcional)
     * @param fechaFin fecha final del periodo (opcional)
     * @return datos del reporte de predicciones
     */
    @GetMapping("/predicciones")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @Operation(
        summary = "Obtener reporte de predicciones",
        description = "Genera datos consolidados de predicciones con estadísticas, métricas y análisis por producto"
    )
    public ResponseEntity<ReportePrediccionDTO> obtenerReportePredicciones(
            @Parameter(description = "Fecha inicio del periodo (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            
            @Parameter(description = "Fecha fin del periodo (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /api/reportes/predicciones - Parametros: fechaInicio={}, fechaFin={}", fechaInicio, fechaFin);

        try {
            ReportePrediccionDTO reporte = reporteService.generarReportePredicciones(fechaInicio, fechaFin);
            log.info("Reporte de predicciones generado: {} predicciones encontradas", 
                    reporte.getResumenGeneral().getTotalPredicciones());
            return ResponseEntity.ok(reporte);

        } catch (Exception e) {
            log.error("Error al generar reporte de predicciones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene datos consolidados de inventario para generar reporte
     * 
     * @param categoriaId filtrar por categoría específica (opcional)
     * @return datos del reporte de inventario
     */
    @GetMapping("/inventario")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'OPERARIO')")
    @Operation(
        summary = "Obtener reporte de inventario",
        description = "Genera datos consolidados de inventario con valoración, productos críticos y estadísticas"
    )
    public ResponseEntity<ReporteInventarioDTO> obtenerReporteInventario(
            @Parameter(description = "ID de categoría para filtrar (opcional)")
            @RequestParam(required = false) Integer categoriaId) {

        log.info("GET /api/reportes/inventario - Parametros: categoriaId={}", categoriaId);

        try {
            ReporteInventarioDTO reporte = reporteService.generarReporteInventario(categoriaId);
            log.info("Reporte de inventario generado: {} productos encontrados",
                    reporte.getResumenGeneral().getTotalProductos());
            return ResponseEntity.ok(reporte);

        } catch (Exception e) {
            log.error("Error al generar reporte de inventario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene datos de predicciones para un producto específico
     * 
     * @param productoId ID del producto
     * @param fechaInicio fecha inicial del periodo (opcional)
     * @param fechaFin fecha final del periodo (opcional)
     * @return datos del reporte de predicciones del producto
     */
    @GetMapping("/predicciones/producto/{productoId}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @Operation(
        summary = "Obtener reporte de predicciones por producto",
        description = "Genera datos de predicciones para un producto específico en un rango de fechas"
    )
    public ResponseEntity<ReportePrediccionDTO> obtenerReportePrediccionesPorProducto(
            @Parameter(description = "ID del producto")
            @PathVariable Integer productoId,
            
            @Parameter(description = "Fecha inicio del periodo (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            
            @Parameter(description = "Fecha fin del periodo (formato: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        log.info("GET /api/reportes/predicciones/producto/{} - Parametros: fechaInicio={}, fechaFin={}",
                productoId, fechaInicio, fechaFin);

        try {
            ReportePrediccionDTO reporte = reporteService.generarReportePrediccionPorProducto(
                    productoId, fechaInicio, fechaFin);
            
            log.info("Reporte de predicciones por producto generado: {} predicciones",
                    reporte.getResumenGeneral().getTotalPredicciones());
            return ResponseEntity.ok(reporte);

        } catch (Exception e) {
            log.error("Error al generar reporte de predicciones por producto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
