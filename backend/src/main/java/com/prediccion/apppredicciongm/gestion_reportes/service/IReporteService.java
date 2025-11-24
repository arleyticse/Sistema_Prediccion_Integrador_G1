package com.prediccion.apppredicciongm.gestion_reportes.service;

import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReporteInventarioDTO;
import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReportePrediccionDTO;

import java.time.LocalDate;

/**
 * Interfaz para servicios de generación de reportes
 */
public interface IReporteService {

    /**
     * Genera reporte completo de predicciones con estadísticas y métricas
     * 
     * @param fechaInicio fecha inicial del periodo (opcional)
     * @param fechaFin fecha final del periodo (opcional)
     * @return DTO con datos del reporte de predicciones
     */
    ReportePrediccionDTO generarReportePredicciones(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Genera reporte completo de inventario con valoración y productos críticos
     * 
     * @param categoriaId filtrar por categoría específica (opcional)
     * @return DTO con datos del reporte de inventario
     */
    ReporteInventarioDTO generarReporteInventario(Integer categoriaId);

    /**
     * Genera reporte de predicciones para un producto específico
     * 
     * @param productoId ID del producto
     * @param fechaInicio fecha inicial del periodo (opcional)
     * @param fechaFin fecha final del periodo (opcional)
     * @return DTO con datos del reporte
     */
    ReportePrediccionDTO generarReportePrediccionPorProducto(Integer productoId, LocalDate fechaInicio, LocalDate fechaFin);
}
