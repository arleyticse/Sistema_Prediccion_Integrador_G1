package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para análisis automático de datos históricos y recomendación de algoritmos.
 * Utiliza técnicas estadísticas ligeras para detectar patrones sin dependencias ML.
 */
@Service
public class AnalisisAutomaticoService {

    // Constantes para umbrales de decisión
    private static final int MIN_DATOS_ESTACIONALIDAD = 14; // Mínimo para detectar estacionalidad semanal
    private static final double UMBRAL_ESTACIONALIDAD = 0.5; // ACF threshold para considerar estacionalidad
    private static final double UMBRAL_TENDENCIA_R2 = 0.3; // R² mínimo para considerar tendencia significativa
    private static final double CV_ESTABLE = 0.2; // CV < 0.2 indica demanda muy estable
    private static final double CV_MODERADO = 0.3; // CV < 0.3 indica demanda moderada

    /**
     * Analiza datos históricos y recomienda el algoritmo más apropiado.
     */
    public RecomendacionAlgoritmo analizarYRecomendar(List<Double> datosHistoricos) {
        if (datosHistoricos == null || datosHistoricos.isEmpty()) {
            throw new IllegalArgumentException("Se requieren datos históricos para el análisis");
        }

        if (datosHistoricos.size() < 7) {
            throw new IllegalArgumentException("Se necesitan al menos 7 registros para el análisis");
        }

        // Análisis de patrones
        boolean tieneEstacionalidad = detectarEstacionalidad(datosHistoricos);
        TendenciaInfo tendencia = detectarTendencia(datosHistoricos);
        double coeficienteVariacion = calcularCoeficienteVariacion(datosHistoricos);

        // Decisión de algoritmo basada en los patrones detectados
        if (tieneEstacionalidad && datosHistoricos.size() >= MIN_DATOS_ESTACIONALIDAD) {
            return recomendarHoltWinters(datosHistoricos, tendencia, coeficienteVariacion);
        } else if (tendencia.esSignificativa || coeficienteVariacion > CV_MODERADO) {
            return recomendarSES(tendencia, coeficienteVariacion);
        } else if (coeficienteVariacion < CV_ESTABLE) {
            return recomendarSMA(datosHistoricos, coeficienteVariacion);
        } else {
            // Default: SES para casos intermedios
            return recomendarSES(tendencia, coeficienteVariacion);
        }
    }

    /**
     * Detecta estacionalidad usando autocorrelación (ACF).
     */
    private boolean detectarEstacionalidad(List<Double> datos) {
        if (datos.size() < MIN_DATOS_ESTACIONALIDAD) {
            return false;
        }

        // Calcular ACF para lags comunes (7 días = semanal, 30 días = mensual)
        double acf7 = calcularAutocorrelacion(datos, 7);
        double acf30 = datos.size() >= 30 ? calcularAutocorrelacion(datos, 30) : 0.0;

        return acf7 > UMBRAL_ESTACIONALIDAD || acf30 > UMBRAL_ESTACIONALIDAD;
    }

    /**
     * Calcula la autocorrelación para un lag específico.
     */
    private double calcularAutocorrelacion(List<Double> datos, int lag) {
        if (datos.size() <= lag) {
            return 0.0;
        }

        double media = datos.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double numerador = 0.0;
        double denominador = 0.0;

        // Calcular numerador (covarianza con lag)
        for (int i = 0; i < datos.size() - lag; i++) {
            numerador += (datos.get(i) - media) * (datos.get(i + lag) - media);
        }

        // Calcular denominador (varianza)
        for (double dato : datos) {
            denominador += Math.pow(dato - media, 2);
        }

        return denominador > 0 ? numerador / denominador : 0.0;
    }

    /**
     * Detecta tendencia usando regresión lineal simple.
     */
    private TendenciaInfo detectarTendencia(List<Double> datos) {
        int n = datos.size();
        
        // Preparar datos para regresión: x = tiempo (0, 1, 2, ...), y = demanda
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = datos.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Calcular pendiente (beta) e intercepto (alpha)
        double pendiente = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercepto = (sumY - pendiente * sumX) / n;

        // Calcular R² (coeficiente de determinación)
        double mediaY = sumY / n;
        double ssTotal = 0, ssResidual = 0;
        
        for (int i = 0; i < n; i++) {
            double y = datos.get(i);
            double yPredicho = intercepto + pendiente * i;
            ssTotal += Math.pow(y - mediaY, 2);
            ssResidual += Math.pow(y - yPredicho, 2);
        }

        double r2 = ssTotal > 0 ? 1 - (ssResidual / ssTotal) : 0.0;
        boolean esSignificativa = r2 > UMBRAL_TENDENCIA_R2;

        return new TendenciaInfo(pendiente, r2, esSignificativa);
    }

    /**
     * Calcula el coeficiente de variación (CV = desviación estándar / media).
     */
    private double calcularCoeficienteVariacion(List<Double> datos) {
        double media = datos.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (media == 0) {
            return Double.MAX_VALUE; // CV indefinido cuando media = 0
        }

        double varianza = datos.stream()
            .mapToDouble(d -> Math.pow(d - media, 2))
            .average()
            .orElse(0.0);
        
        double desviacion = Math.sqrt(varianza);
        
        return desviacion / media;
    }

    /**
     * Recomienda Holt-Winters para datos con estacionalidad.
     */
    private RecomendacionAlgoritmo recomendarHoltWinters(List<Double> datos, TendenciaInfo tendencia, double cv) {
        Map<String, Double> parametros = new HashMap<>();
        parametros.put("alpha", 0.4);  // Nivel: moderado
        parametros.put("beta", tendencia.esSignificativa ? 0.3 : 0.1);  // Tendencia según análisis
        parametros.put("gamma", 0.3);  // Estacionalidad: moderado
        parametros.put("periodo", (double) detectarPeriodo(datos));

        String justificacion = String.format(
            "Patrón estacional detectado cada %d días con %s. " +
            "Holt-Winters captura nivel, tendencia y estacionalidad para predicciones precisas.",
            detectarPeriodo(datos),
            tendencia.esSignificativa ? "tendencia significativa" : "estacionalidad dominante"
        );

        return new RecomendacionAlgoritmo(
            "holtWintersAlgorithm",
            parametros,
            justificacion,
            0.9  // Alta confianza cuando hay estacionalidad clara
        );
    }

    /**
     * Recomienda SES para datos con tendencia o volatilidad moderada.
     */
    private RecomendacionAlgoritmo recomendarSES(TendenciaInfo tendencia, double cv) {
        Map<String, Double> parametros = new HashMap<>();
        
        // Alpha óptimo según volatilidad
        double alpha = calcularAlphaOptimo(cv);
        parametros.put("alpha", alpha);

        String razonPrincipal = tendencia.esSignificativa 
            ? "tendencia significativa" 
            : "volatilidad moderada";

        String justificacion = String.format(
            "Demanda con %s (R²=%.2f, CV=%.2f). " +
            "Suavizado Exponencial balancea datos recientes con historial para seguir cambios.",
            razonPrincipal,
            tendencia.r2,
            cv
        );

        double confianza = tendencia.esSignificativa ? 0.85 : 0.75;

        return new RecomendacionAlgoritmo(
            "simpleExponentialSmoothingAlgorithm",
            parametros,
            justificacion,
            confianza
        );
    }

    /**
     * Recomienda SMA para datos muy estables.
     */
    private RecomendacionAlgoritmo recomendarSMA(List<Double> datos, double cv) {
        Map<String, Double> parametros = new HashMap<>();
        
        // Ventana óptima: raíz cuadrada del tamaño de datos, capped entre 7-21
        int ventanaOptima = calcularVentanaOptima(datos.size());
        parametros.put("ventana", (double) ventanaOptima);

        String justificacion = String.format(
            "Demanda muy estable (CV=%.2f) sin tendencias marcadas. " +
            "Promedio Móvil Simple suaviza ruido aleatorio efectivamente.",
            cv
        );

        return new RecomendacionAlgoritmo(
            "simpleMovingAverageAlgorithm",
            parametros,
            justificacion,
            0.8  // Alta confianza para datos estables
        );
    }

    /**
     * Detecta el periodo de estacionalidad (7 o 30 días).
     */
    private int detectarPeriodo(List<Double> datos) {
        if (datos.size() < 30) {
            return 7; // Default: semanal
        }

        double acf7 = calcularAutocorrelacion(datos, 7);
        double acf30 = calcularAutocorrelacion(datos, 30);

        return acf30 > acf7 ? 30 : 7;
    }

    /**
     * Calcula alpha óptimo según coeficiente de variación.
     */
    private double calcularAlphaOptimo(double cv) {
        if (cv < 0.2) return 0.2;  // Baja volatilidad: suavizar más
        if (cv < 0.4) return 0.3;  // Volatilidad moderada
        return 0.5;                 // Alta volatilidad: reaccionar más rápido
    }

    /**
     * Calcula ventana óptima para SMA.
     */
    private int calcularVentanaOptima(int n) {
        int ventana = (int) Math.sqrt(n);
        return Math.max(7, Math.min(21, ventana)); // Entre 7 y 21 días
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Información sobre la tendencia detectada.
     */
    private static class TendenciaInfo {
        final double pendiente;
        final double r2;
        final boolean esSignificativa;

        TendenciaInfo(double pendiente, double r2, boolean esSignificativa) {
            this.pendiente = pendiente;
            this.r2 = r2;
            this.esSignificativa = esSignificativa;
        }
    }

    /**
     * DTO para la recomendación de algoritmo.
     */
    public static class RecomendacionAlgoritmo {
        private final String algoritmo;
        private final Map<String, Double> parametros;
        private final String justificacion;
        private final double confianza;

        public RecomendacionAlgoritmo(String algoritmo, Map<String, Double> parametros, 
                                     String justificacion, double confianza) {
            this.algoritmo = algoritmo;
            this.parametros = parametros;
            this.justificacion = justificacion;
            this.confianza = confianza;
        }

        public String getAlgoritmo() {
            return algoritmo;
        }

        public Map<String, Double> getParametros() {
            return parametros;
        }

        public String getJustificacion() {
            return justificacion;
        }

        public double getConfianza() {
            return confianza;
        }
    }
}
