package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO de solicitud para generar predicción de demanda.
 * Soporta múltiples algoritmos (SMA, SES, HOLT_WINTERS) con parámetros configurables.
 *
 * <p>Ejemplos de uso:
 * <ul>
 *   <li>SMA: algoritmo="SMA", parametros={"ventana": 14.0}</li>
 *   <li>SES: algoritmo="SES", parametros={"alpha": 0.3}</li>
 *   <li>Holt-Winters: algoritmo="HOLT_WINTERS", parametros={"alpha": 0.4, "beta": 0.2, "gamma": 0.3, "periodo": 7.0}</li>
 * </ul>
 *
 * @author Sistema de Predicción
 * @version 2.0
 * @since 2025-11-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPrediccionRequest {

    /**
     * ID del producto para el cual se generará la predicción.
     */
    @NotNull(message = "El ID del producto es requerido")
    @JsonProperty("productoId")
    private Integer productoId;

    /**
     * Código del algoritmo a utilizar.
     * Valores válidos: "SMA", "SES", "HOLT_WINTERS"
     */
    @NotBlank(message = "El algoritmo es requerido")
    @JsonProperty("algoritmo")
    private String algoritmo = "SMA";

    /**
     * Horizonte de tiempo (número de períodos a predecir).
     * Por ejemplo: 7 días, 14 días, 30 días.
     */
    @NotNull(message = "El horizonte de tiempo es requerido")
    @Min(value = 1, message = "El horizonte debe ser al menos 1 período")
    @Max(value = 365, message = "El horizonte no puede exceder 365 períodos")
    @JsonProperty("horizonteTiempo")
    private Integer horizonteTiempo = 30;

    /**
     * Parámetros específicos del algoritmo.
     * 
     * <p>Parámetros por algoritmo:
     * <ul>
     *   <li><b>SMA</b>: ventana (3-100, default 14)</li>
     *   <li><b>SES</b>: alpha (0.01-0.99, default 0.3)</li>
     *   <li><b>HOLT_WINTERS</b>: alpha (0.01-0.99, default 0.4), 
     *       beta (0.01-0.99, default 0.2), 
     *       gamma (0.01-0.99, default 0.3), 
     *       periodo (2-52, default 7)</li>
     * </ul>
     */
    @JsonProperty("parametros")
    private Map<String, Double> parametros = new HashMap<>();

    /**
     * Indicador para limpiar predicciones anteriores del mismo producto.
     */
    @JsonProperty("limpiarAnterior")
    private Boolean limpiarAnterior = false;

    /**
     * Indicador para incluir detalles extendidos en la respuesta.
     */
    @JsonProperty("incluirDetalles")
    private Boolean incluirDetalles = true;

    // Métodos de conveniencia para configurar parámetros

    /**
     * Configura parámetros para algoritmo SMA (Simple Moving Average).
     * 
     * @param ventana Tamaño de la ventana móvil (3-100)
     * @return Esta instancia para encadenamiento
     */
    public GenerarPrediccionRequest configurarSMA(int ventana) {
        this.algoritmo = "SMA";
        this.parametros.put("ventana", (double) ventana);
        return this;
    }

    /**
     * Configura parámetros para algoritmo SES (Simple Exponential Smoothing).
     * 
     * @param alpha Factor de suavizado (0.01-0.99)
     * @return Esta instancia para encadenamiento
     */
    public GenerarPrediccionRequest configurarSES(double alpha) {
        this.algoritmo = "SES";
        this.parametros.put("alpha", alpha);
        return this;
    }

    /**
     * Configura parámetros para algoritmo Holt-Winters.
     * 
     * @param alpha Factor de nivel (0.01-0.99)
     * @param beta Factor de tendencia (0.01-0.99)
     * @param gamma Factor de estacionalidad (0.01-0.99)
     * @param periodo Período estacional (2-52)
     * @return Esta instancia para encadenamiento
     */
    public GenerarPrediccionRequest configurarHoltWinters(double alpha, double beta, 
                                                         double gamma, int periodo) {
        this.algoritmo = "HOLT_WINTERS";
        this.parametros.put("alpha", alpha);
        this.parametros.put("beta", beta);
        this.parametros.put("gamma", gamma);
        this.parametros.put("periodo", (double) periodo);
        return this;
    }
}
