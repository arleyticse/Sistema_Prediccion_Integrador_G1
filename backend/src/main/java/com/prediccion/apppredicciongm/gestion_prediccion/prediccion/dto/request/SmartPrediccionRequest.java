package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para la configuración de predicción inteligente con ML usando Smile.
 * 
 * Permite configurar la predicción automática usando múltiples algoritmos de ML,
 * detección de estacionalidad y generación automática de órdenes de compra.
 */
@Data
@Schema(description = "Configuración avanzada para predicción inteligente con Machine Learning")
public class SmartPrediccionRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    @Min(value = 1, message = "El ID del producto debe ser positivo")
    @Schema(description = "ID del producto para el cual generar la predicción", example = "1")
    private Long idProducto;

    @Schema(description = "Horizonte de tiempo para la predicción en días. Si no se especifica, se detecta automáticamente basado en estacionalidad", 
            example = "30")
    private Integer horizonteTiempo;

    @Schema(description = "Indica si debe detectar automáticamente patrones estacionales", 
            example = "true", defaultValue = "true")
    private Boolean detectarEstacionalidad = true;

    @Schema(description = "Indica si debe generar orden de compra sugerida si hay riesgo de quiebre de stock", 
            example = "true", defaultValue = "true")
    private Boolean generarOrdenCompra = true;

    @Schema(description = "Forzar el uso de un algoritmo específico (opcional). Si no se especifica, se selecciona automáticamente el mejor",
            allowableValues = {"AUTO", "OLS", "LASSO", "RIDGE", "RANDOM_FOREST", "GBM", "REGRESSION_TREE"})
    private String algoritmoSeleccionado = "AUTO";

    @Schema(description = "Incluir análisis detallado de métricas de calidad (MAE, RMSE, MAPE)", 
            example = "true", defaultValue = "true")
    private Boolean incluirAnalisisDetallado = true;

    @Schema(description = "Número mínimo de registros históricos para entrenar el modelo",
            example = "30", defaultValue = "30")
    private Integer minimoRegistrosHistoricos = 30;

    @Schema(description = "Porcentaje de datos para validación (0.0 - 1.0). Se usa para seleccionar el mejor modelo",
            example = "0.2", defaultValue = "0.2")
    private Double porcentajeValidacion = 0.2;

    @Schema(description = "Generar análisis de estacionalidad y guardarlo en la base de datos", 
            example = "true", defaultValue = "true")
    private Boolean guardarAnalisisEstacionalidad = true;
}