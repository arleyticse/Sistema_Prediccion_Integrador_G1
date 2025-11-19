package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta para la predicción inteligente con ML.
 * 
 * Contiene el resultado completo de la predicción: algoritmo seleccionado,
 * métricas de calidad, predicciones detalladas y orden de compra sugerida.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resultado completo de la predicción inteligente con Machine Learning")
public class SmartPrediccionResponse {

    @Schema(description = "ID único de la predicción generada", example = "123")
    private Long idPrediccion;

    @Schema(description = "ID del producto analizado", example = "1")
    private Long idProducto;

    @Schema(description = "Nombre del producto analizado", example = "Producto ABC")
    private String nombreProducto;

    @Schema(description = "Fecha y hora de ejecución de la predicción")
    private LocalDateTime fechaEjecucion;

    @Schema(description = "Horizonte de tiempo utilizado (en días)", example = "30")
    private Integer horizonteTiempo;

    @Schema(description = "Algoritmo seleccionado automáticamente o por el usuario", example = "RANDOM_FOREST")
    private String algoritmoUtilizado;

    @Schema(description = "Razón por la cual se seleccionó este algoritmo", 
            example = "Mejor MAPE (8.5%) en validación cruzada")
    private String razonSeleccionAlgoritmo;

    @Schema(description = "Predicciones detalladas por período")
    private List<PrediccionDetalle> prediccionesDetalladas;

    @Schema(description = "Demanda total predicha para todo el horizonte", example = "450.5")
    private Double demandaTotalPredicha;

    @Schema(description = "Métricas de calidad del modelo")
    private MetricasCalidad metricas;

    @Schema(description = "Información de estacionalidad detectada")
    private EstacionalidadInfo estacionalidad;

    @Schema(description = "Análisis de riesgo de quiebre de stock")
    private RiesgoQuiebreInfo riesgoQuiebre;

    @Schema(description = "Orden de compra sugerida (si aplica)")
    private OrdenCompraSugerida ordenCompraSugerida;

    @Schema(description = "Recomendaciones y alertas del sistema")
    private List<String> recomendaciones;

    @Schema(description = "Información adicional sobre el proceso")
    private Map<String, Object> metadatos;

    /**
     * Detalle de predicción por período
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Predicción detallada para un período específico")
    public static class PrediccionDetalle {
        @Schema(description = "Período de la predicción (día)", example = "1")
        private Integer periodo;
        
        @Schema(description = "Fecha estimada", example = "2025-11-12")
        private String fecha;
        
        @Schema(description = "Demanda predicha", example = "15.2")
        private Double demandaPredicha;
        
        @Schema(description = "Intervalo de confianza inferior", example = "12.8")
        private Double intervaloCorrfianzaInferior;
        
        @Schema(description = "Intervalo de confianza superior", example = "17.6")
        private Double intervaloConfianzaSuperior;
    }

    /**
     * Métricas de calidad del modelo
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Métricas de calidad y precisión del modelo")
    public static class MetricasCalidad {
        @Schema(description = "Error Absoluto Medio (MAE)", example = "2.15")
        private Double mae;
        
        @Schema(description = "Error Cuadrático Medio Raíz (RMSE)", example = "3.42")
        private Double rmse;
        
        @Schema(description = "Error Absoluto Porcentual Medio (MAPE)", example = "8.5")
        private Double mape;
        
        @Schema(description = "Coeficiente de determinación R²", example = "0.85")
        private Double rSquared;
        
        @Schema(description = "Calificación de calidad", example = "EXCELENTE")
        private String calificacionCalidad;
        
        @Schema(description = "Nivel de confianza del modelo (0-100)", example = "85")
        private Integer nivelConfianza;
    }

    /**
     * Información de estacionalidad detectada
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Información sobre patrones estacionales detectados")
    public static class EstacionalidadInfo {
        @Schema(description = "Si se detectó estacionalidad", example = "true")
        private Boolean tieneEstacionalidad;
        
        @Schema(description = "Intensidad de la estacionalidad (0-1)", example = "0.65")
        private Double intensidadEstacional;
        
        @Schema(description = "Mes de mayor demanda", example = "12")
        private Integer mesMayorDemanda;
        
        @Schema(description = "Mes de menor demanda", example = "6")
        private Integer mesMenorDemanda;
        
        @Schema(description = "Patrón estacional detectado", example = "ALTO_DICIEMBRE_BAJO_JUNIO")
        private String patronDetectado;
        
        @Schema(description = "Horizonte sugerido basado en estacionalidad", example = "90")
        private Integer horizonteSugerido;
    }

    /**
     * Información del riesgo de quiebre de stock
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Análisis de riesgo de quiebre de stock")
    public static class RiesgoQuiebreInfo {
        @Schema(description = "Stock actual del producto", example = "150")
        private Integer stockActual;
        
        @Schema(description = "Stock mínimo configurado", example = "50")
        private Integer stockMinimo;
        
        @Schema(description = "Punto de reorden", example = "75")
        private Integer puntoReorden;
        
        @Schema(description = "Días estimados hasta quiebre si no se repone", example = "12")
        private Integer diasHastaQuiebre;
        
        @Schema(description = "Nivel de riesgo", example = "ALTO")
        private String nivelRiesgo;
        
        @Schema(description = "Requiere acción inmediata", example = "true")
        private Boolean requiereAccionInmediata;
    }

    /**
     * Orden de compra sugerida
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Orden de compra sugerida basada en la predicción")
    public static class OrdenCompraSugerida {
        @Schema(description = "Se sugiere generar orden de compra", example = "true")
        private Boolean sugerirOrden;
        
        @Schema(description = "Cantidad sugerida a ordenar", example = "200")
        private Integer cantidadSugerida;
        
        @Schema(description = "Justificación de la cantidad", example = "Basado en EOQ y demanda predicha")
        private String justificacionCantidad;
        
        @Schema(description = "Proveedor sugerido", example = "Proveedor ABC S.A.")
        private String proveedorSugerido;
        
        @Schema(description = "ID del proveedor sugerido", example = "3")
        private Long idProveedorSugerido;
        
        @Schema(description = "Costo estimado total", example = "1500.00")
        private Double costoEstimadoTotal;
        
        @Schema(description = "Fecha sugerida para realizar el pedido")
        private String fechaSugeridaPedido;
        
        @Schema(description = "Prioridad del pedido", example = "ALTA")
        private String prioridadPedido;
    }
}