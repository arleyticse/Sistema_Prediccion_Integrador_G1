package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO que encapsula el resultado completo de una predicción de demanda.
 * 
 * Contiene las predicciones generadas, métricas de precisión, datos históricos
 * utilizados y parámetros del algoritmo aplicado. Este DTO es utilizado
 * internamente por los algoritmos y puede ser convertido a PrediccionResponse
 * para la respuesta al cliente.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoPrediccionDTO {
    
    /**
     * Lista de valores predichos para cada período futuro.
     * El tamaño de la lista corresponde al horizonte de tiempo solicitado.
     */
    @JsonProperty("valoresPredichos")
    private List<Double> valoresPredichos;
    
    /**
     * Datos históricos utilizados para generar la predicción.
     * Útil para comparación y visualización.
     */
    @JsonProperty("datosHistoricos")
    private List<Double> datosHistoricos;
    
    /**
     * Suma total de la demanda predicha.
     * Útil para cálculos rápidos de inventario y órdenes de compra.
     */
    @JsonProperty("demandaTotalPredicha")
    private Double demandaTotalPredicha;
    
    /**
     * Error Absoluto Medio (MAE - Mean Absolute Error).
     * Promedio de las diferencias absolutas entre valores reales y predichos.
     * Cuanto menor, mejor la predicción.
     */
    @JsonProperty("mae")
    private Double mae;
    
    /**
     * Error Porcentual Absoluto Medio (MAPE - Mean Absolute Percentage Error).
     * Porcentaje de error promedio. Valores menores a 10% son excelentes,
     * 10-20% buenos, 20-50% aceptables, mayores a 50% pobres.
     */
    @JsonProperty("mape")
    private Double mape;
    
    /**
     * Raíz del Error Cuadrático Medio (RMSE - Root Mean Squared Error).
     * Penaliza más los errores grandes. Útil para detectar predicciones
     * con valores atípicos muy alejados.
     */
    @JsonProperty("rmse")
    private Double rmse;
    
    /**
     * Código del algoritmo utilizado (SMA, SES, HOLT, HOLT_WINTERS, etc.).
     */
    @JsonProperty("algoritmoUsado")
    private String algoritmoUsado;
    
    /**
     * Nombre descriptivo del algoritmo utilizado.
     */
    @JsonProperty("nombreAlgoritmo")
    private String nombreAlgoritmo;
    
    /**
     * Parámetros utilizados en el algoritmo.
     * Por ejemplo: {"alpha": 0.3, "ventana": 14}
     */
    @JsonProperty("parametros")
    private Map<String, Double> parametros;
    
    /**
     * Intervalo de confianza inferior (opcional).
     * Útil para análisis de riesgo y escenarios pessimistas.
     */
    @JsonProperty("intervaloConfianzaInferior")
    private List<Double> intervaloConfianzaInferior;
    
    /**
     * Intervalo de confianza superior (opcional).
     * Útil para análisis de riesgo y escenarios optimistas.
     */
    @JsonProperty("intervaloConfianzaSuperior")
    private List<Double> intervaloConfianzaSuperior;
    
    /**
     * Nivel de confianza usado para los intervalos (típicamente 95%).
     */
    @JsonProperty("nivelConfianza")
    private Double nivelConfianza;
    
    /**
     * Indica si el algoritmo detectó tendencia en los datos.
     */
    @JsonProperty("tieneTendencia")
    private Boolean tieneTendencia;
    
    /**
     * Indica si el algoritmo detectó estacionalidad en los datos.
     */
    @JsonProperty("tieneEstacionalidad")
    private Boolean tieneEstacionalidad;
    
    /**
     * Período estacional detectado (si aplica).
     * Por ejemplo: 7 (semanal), 30 (mensual), 365 (anual)
     */
    @JsonProperty("periodoEstacional")
    private Integer periodoEstacional;
    
    /**
     * Mensajes de advertencia o información adicional.
     * Por ejemplo: "Datos insuficientes para estacionalidad"
     */
    @JsonProperty("advertencias")
    private List<String> advertencias;
    
    /**
     * Recomendaciones basadas en el análisis.
     * Por ejemplo: "Se recomienda aumentar stock en 20%"
     */
    @JsonProperty("recomendaciones")
    private List<String> recomendaciones;
    
    /**
     * Calidad de la predicción basada en métricas.
     * EXCELENTE (MAPE < 10%), BUENA (10-20%), ACEPTABLE (20-50%), POBRE (> 50%)
     */
    @JsonProperty("calidadPrediccion")
    private String calidadPrediccion;
}