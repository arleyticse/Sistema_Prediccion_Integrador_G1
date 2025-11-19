package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta para predicciones inteligentes con Smile ML
 * Implementa RF006 (análisis múltiples) y RF007 (visualizaciones)
 * 
 * @author Sistema de Predicción Unificado
 * @version 2.0 - Integración con Smile ML
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoPrediccionSmartResponse {
    
    private Long productoId;
    private String nombreProducto;
    private String sku;
    private String codigoProducto;
    
    /**
     * Configuración utilizada para la predicción
     */
    private ConfiguracionUsada configuracion;
    
    /**
     * Predicciones detalladas por día
     */
    private List<PuntoPrediccion> predicciones;
    
    /**
     * Métricas de calidad del modelo Smile ML
     */
    private MetricasModeloSmile metricas;
    
    /**
     * Recomendaciones de inventario basadas en predicción
     */
    private RecomendacionesInventario recomendaciones;
    
    /**
     * Datos para visualización gráfica (RF007)
     */
    private DatosVisualizacion visualizacion;
    
    /**
     * Información sobre el cálculo automático de horizonte
     */
    private HorizonteAutomatico horizonteCalculado;
    
    private LocalDateTime fechaGeneracion = LocalDateTime.now();
    private String version = "2.0-SMILE";
    private String estado = "COMPLETADA";
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfiguracionUsada {
        private String algoritmo;
        private Integer horizonteDias;
        private Integer nivelConfianza;
        private Boolean estacionalidadAplicada;
        private Boolean eventosConsiderados;
        private Map<String, Object> parametrosModelo;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PuntoPrediccion {
        private LocalDateTime fecha;
        private Double demandaPredicta;
        private Double limiteSuperior;
        private Double limiteInferior;
        private Double probabilidad;
        private String tendencia; // "CRECIENTE", "DECRECIENTE", "ESTABLE"
        private String clasificacion; // "NORMAL", "ALTA", "BAJA"
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetricasModeloSmile {
        private Double accuracy;          // Precisión del modelo (0-100%)
        private Double mape;             // Mean Absolute Percentage Error
        private Double rmse;             // Root Mean Square Error
        private Double mae;              // Mean Absolute Error
        private Double r2Score;          // Coeficiente de determinación
        private String algoritmoUsado;   // Algoritmo Smile ML seleccionado
        private String razonSeleccion;   // Por qué se seleccionó este algoritmo
        private Integer periodosUsados;  // Cantidad de datos históricos
        private String frameworkVersion; // Smile ML v3.1.1
        private Map<String, Double> parametrosOptimos;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecomendacionesInventario {
        private Double stockMinimo;
        private Double stockOptimo;
        private Double stockMaximo;
        private Double puntoPedido;
        private Integer diasCobertura;
        private String nivelRiesgo; // "BAJO", "MEDIO", "ALTO", "CRITICO"
        private String alertaNivel; // "NORMAL", "ADVERTENCIA", "CRITICO"
        private String accionRecomendada;
        private List<String> observaciones;
        private Map<String, Double> metricsInventario;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DatosVisualizacion {
        // Datos históricos para gráficos de líneas
        private List<PuntoHistorico> datosHistoricos;
        
        // Componentes de la serie temporal (RF007)
        private ComponentesSerieTemporalr componentes;
        
        // Datos para gráfico de barras de predicción
        private Map<String, Double> prediccionPorPeriodo;
        
        // Bandas de confianza para visualización
        private List<BandaConfianza> bandasConfianza;
        
        // Configuración de gráficos recomendada
        private ConfiguracionGraficos configuracionGraficos;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PuntoHistorico {
        private LocalDateTime fecha;
        private Double demandaReal;
        private String tipo; // "HISTORICO", "PREDICCION"
        private String periodo; // "SEMANAL", "MENSUAL"
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComponentesSerieTemporalr {
        private List<Double> tendencia;
        private List<Double> estacionalidad;
        private List<Double> residuos;
        private List<String> etiquetasFecha;
        private String periodicidadDetectada;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BandaConfianza {
        private LocalDateTime fecha;
        private Double valorCentral;
        private Double limiteSuperior;
        private Double limiteInferior;
        private Integer nivelConfianza;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfiguracionGraficos {
        private String tipoGraficoPrincipal; // "LINE", "BAR", "AREA"
        private List<String> coloresRecomendados;
        private Map<String, String> etiquetasEjes;
        private Boolean mostrarIntervalosConfianza;
        private Boolean incluirComponentesEstacionales;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HorizonteAutomatico {
        private Integer horizonteCalculado;
        private String metodoCalculo;
        private String justificacion;
        private Map<String, Object> factoresConsiderados;
        private String rangoRecomendado;
        private Boolean esOptimo;
    }
}