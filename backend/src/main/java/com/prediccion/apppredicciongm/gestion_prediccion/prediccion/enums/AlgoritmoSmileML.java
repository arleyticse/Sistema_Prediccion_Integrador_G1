package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeración de algoritmos de Machine Learning disponibles en SMILE ML v3.1.1
 * 
 * Optimizado para negocios de consumo masivo y productos de primera necesidad.
 * Cada algoritmo está diseñado para diferentes patrones de demanda.
 */
@Getter
@RequiredArgsConstructor
public enum AlgoritmoSmileML {
    
    /**
     * Regresión Lineal con Mínimos Cuadrados Ordinarios (OLS)
     * Implementación: smile.regression.OLS
     */
    LINEAR_REGRESSION(
        "LINEAR_REGRESSION",
        "Regresión Lineal (OLS)",
        "Óptimo para productos con demanda estable y tendencia lineal clara. " +
        "Ideal para: arroz, azúcar, sal, aceite.",
        "smile.regression.OLS",
        TipoPatronDemanda.ESTABLE_LINEAL,
        10  // Mínimo registros requeridos
    ),
    
    /**
     * ARIMA (AutoRegressive Integrated Moving Average)
     * Implementación: smile.timeseries.ARIMA
     * Algoritmo especializado para series temporales
     */
    ARIMA(
        "ARIMA",
        "ARIMA Time Series",
        "Especializado para series temporales con tendencias y patrones estacionales. " +
        "Captura autocorrelación temporal. Ideal para productos con demanda regular y predecible.",
        "smile.timeseries.ARIMA",
        TipoPatronDemanda.TEMPORAL_AUTOCORRELADO,
        30  // Requiere datos suficientes para detectar patrones temporales
    ),
    
    /**
     * Random Forest para Regresión
     * Implementación: smile.regression.RandomForest
     */
    RANDOM_FOREST(
        "RANDOM_FOREST",
        "Random Forest",
        "Excelente para patrones complejos con estacionalidad y múltiples factores. " +
        "Ideal para: bebidas, snacks, productos de limpieza, artículos de higiene.",
        "smile.regression.RandomForest",
        TipoPatronDemanda.COMPLEJO_ESTACIONAL,
        30  // Requiere más datos para entrenar múltiples árboles
    ),
    
    /**
     * Gradient Boosting Machine
     * Implementación: smile.regression.GradientTreeBoost
     */
    GRADIENT_BOOSTING(
        "GRADIENT_BOOSTING",
        "Gradient Boosting Machine",
        "Máxima precisión para demanda errática y productos perecederos. " +
        "Ideal para: pan, lácteos, frutas, verduras, carnes.",
        "smile.regression.GradientTreeBoost",
        TipoPatronDemanda.ERRATICO_PERECEDERO,
        20  // Balance entre precisión y datos necesarios
    ),
    
    /**
     * Modo automático: selecciona el mejor algoritmo según características de los datos
     */
    AUTO(
        "AUTO",
        "Selección Automática",
        "El sistema analiza automáticamente los datos históricos y selecciona " +
        "el algoritmo más apropiado basándose en variabilidad, tendencia y estacionalidad.",
        "auto",
        TipoPatronDemanda.AUTOMATICO,
        10
    );
    
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final String claseSmile;
    private final TipoPatronDemanda patronOptimo;
    private final int minimoRegistrosRequeridos;
    
    /**
     * Obtiene el algoritmo por su código
     */
    public static AlgoritmoSmileML fromCodigo(String codigo) {
        if (codigo == null) {
            return AUTO;
        }
        
        for (AlgoritmoSmileML algoritmo : values()) {
            if (algoritmo.codigo.equalsIgnoreCase(codigo)) {
                return algoritmo;
            }
        }
        
        return AUTO;
    }
    
    /**
     * Verifica si este algoritmo requiere más datos que los disponibles
     */
    public boolean requiereMasDatos(int datosDisponibles) {
        return datosDisponibles < minimoRegistrosRequeridos;
    }
    
    /**
     * Tipos de patrones de demanda para clasificación automática
     */
    @Getter
    @RequiredArgsConstructor
    public enum TipoPatronDemanda {
        ESTABLE_LINEAL(
            "Demanda estable con tendencia clara",
            "Bajo coeficiente de variación (CV < 0.3)"
        ),
        TEMPORAL_AUTOCORRELADO(
            "Serie temporal con autocorrelación",
            "Demanda con dependencia temporal y patrones regulares"
        ),
        COMPLEJO_ESTACIONAL(
            "Patrones estacionales y múltiples factores",
            "Estacionalidad detectada o variabilidad moderada (0.3 ≤ CV ≤ 0.7)"
        ),
        ERRATICO_PERECEDERO(
            "Alta variabilidad y cambios frecuentes",
            "Alto coeficiente de variación (CV > 0.7)"
        ),
        AUTOMATICO(
            "Detección automática del patrón",
            "Análisis inteligente de características"
        );
        
        private final String descripcion;
        private final String criterio;
    }
}
