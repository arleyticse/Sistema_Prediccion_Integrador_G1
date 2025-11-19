package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para configurar predicciones inteligentes con Smile ML
 * Implementa RF006: Configuración de análisis predictivos múltiples
 * 
 * @author Sistema de Predicción Unificado
 * @version 2.0 - Integración con Smile ML
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionPrediccionSmartRequest {
    
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
    
    /**
     * Horizonte de predicción (días)
     * Si es null, se calcula automáticamente basado en lead time + buffer
     */
    @Min(value = 1, message = "El horizonte debe ser al menos 1 día")
    @Max(value = 365, message = "El horizonte no puede exceder 365 días")
    private Integer horizonteDias;
    
    /**
     * Tipo de algoritmo para usar
     */
    private AlgoritmoSmile algoritmo = AlgoritmoSmile.AUTO;
    
    /**
     * Nivel de confianza para intervalos de predicción (80%, 90%, 95%, 99%)
     */
    @Min(value = 80)
    @Max(value = 99)
    private Integer nivelConfianza = 95;
    
    /**
     * Incluir análisis estacional desde BD
     */
    private Boolean incluirEstacionalidad = true;
    
    /**
     * Considerar eventos especiales
     */
    private Boolean considerarEventos = true;
    
    /**
     * Factor de suavizado para exponential smoothing (10-90%)
     */
    @Min(value = 10)
    @Max(value = 90)
    private Integer factorSuavizado = 30;
    
    /**
     * Generar datos para visualización (RF007)
     */
    private Boolean generarVisualizacion = true;
    
    public enum AlgoritmoSmile {
        ARIMA,           // AutoRegressive Integrated Moving Average
        EXPONENTIAL,     // Exponential Smoothing
        LINEAR_TREND,    // Linear Regression with trend
        SEASONAL_DECOMP, // Seasonal decomposition
        AUTO            // Selección automática basada en datos
    }
}