package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Implementación del algoritmo de Suavizado Exponencial Simple (SES - Simple Exponential Smoothing).
 *
 * Este algoritmo asigna pesos exponencialmente decrecientes a las observaciones pasadas,
 * dando mayor importancia a los datos más recientes. Es más sofisticado que el promedio
 * móvil simple y más adecuado para datos con cambios graduales.
 *
 * Características:
 * - Un solo parámetro de control (alpha)
 * - Balance entre estabilidad y respuesta a cambios
 * - Memoria eficiente (solo necesita la última predicción)
 * - Suaviza fluctuaciones aleatorias
 * - No modela tendencias ni estacionalidad
 *
 * Uso recomendado:
 * - Productos de alta rotación (leche, pan, huevos)
 * - Demanda estable con variaciones aleatorias
 * - Cuando se requiere balance entre estabilidad y reactividad
 *
 * Parámetros requeridos:
 * - alpha: Factor de suavizado (0 < alpha < 1)
 *   * alpha cercano a 0: Más peso al histórico (más estable)
 *   * alpha cercano a 1: Más peso a datos recientes (más reactivo)
 *   * Valor típico: 0.3 para estabilidad, 0.7 para reactividad
 *
 * Fórmula:
 * Predicción(t+1) = alpha * Venta(t) + (1 - alpha) * Predicción(t)
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */

@Component
public class SimpleExponentialSmoothingAlgorithm extends AlgoritmoPrediccionBase {
    
    private static final String CODIGO = "SES";
    private static final String NOMBRE = "Suavizado Exponencial Simple";
    private static final int MINIMO_DATOS = 5;
    private static final String PARAMETRO_ALPHA = "alpha";
    private static final double ALPHA_DEFAULT = 0.3;
    private static final double ALPHA_MIN = 0.01;
    private static final double ALPHA_MAX = 0.99;
    
    @Override
    public ResultadoPrediccionDTO predecir(
            List<Double> datosHistoricos,
            int horizonteTiempo,
            Map<String, Double> parametros) {
        
        logger.info("Iniciando prediccion con {}: {} datos historicos, horizonte {} periodos",
            NOMBRE, datosHistoricos.size(), horizonteTiempo);
        
        // Validar datos
        validarDatos(datosHistoricos, parametros);
        
        // Obtener alpha
        double alpha = obtenerAlpha(parametros);
        logger.debug("Usando alpha = {} ({}% peso a datos recientes)",
            String.format("%.2f", alpha),
            String.format("%.0f", alpha * 100));
        
        // Inicializar con el promedio de los primeros valores
        int inicializacion = Math.min(3, datosHistoricos.size());
        double nivelActual = calcularPromedio(datosHistoricos.subList(0, inicializacion));
        logger.debug("Nivel inicial: {}", String.format("%.2f", nivelActual));
        
        // Aplicar suavizado exponencial a los datos históricos
        for (int i = inicializacion; i < datosHistoricos.size(); i++) {
            double observacion = datosHistoricos.get(i);
            nivelActual = alpha * observacion + (1 - alpha) * nivelActual;
        }
        
        // Generar predicciones
        List<Double> predicciones = new ArrayList<>();
        double prediccionActual = nivelActual;
        
        for (int i = 0; i < horizonteTiempo; i++) {
            predicciones.add(prediccionActual);
            // En SES simple, la predicción se mantiene constante para todos los períodos futuros
            // (no hay tendencia)
        }
        
        logger.debug("Generadas {} predicciones. Valor constante: {}",
            predicciones.size(),
            String.format("%.2f", prediccionActual));
        
        // Construir resultado
        ResultadoPrediccionDTO resultado = construirResultado(predicciones, datosHistoricos, parametros);
        
        // Agregar advertencias específicas
        List<String> advertencias = new ArrayList<>();
        
        if (datosHistoricos.size() < 15) {
            advertencias.add("Datos historicos limitados. Se recomienda al menos 15 observaciones para mejor ajuste del parametro alpha");
        }
        
        // Detectar tendencia
        if (detectarTendencia(datosHistoricos)) {
            advertencias.add("Se detecto una tendencia en los datos. Considere usar Holt (Suavizado Exponencial Doble) para mejor precision");
        }
        
        // Detectar alta variabilidad
        double desviacion = calcularDesviacionEstandar(datosHistoricos);
        double promedio = calcularPromedio(datosHistoricos);
        double coeficienteVariacion = (desviacion / promedio) * 100;
        
        if (coeficienteVariacion > 40) {
            advertencias.add(String.format(
                "Alta variabilidad en los datos (CV=%.1f%%). Un alpha mas alto (0.5-0.7) puede ser mas apropiado",
                coeficienteVariacion
            ));
        } else if (coeficienteVariacion < 15) {
            advertencias.add(String.format(
                "Baja variabilidad en los datos (CV=%.1f%%). Un alpha mas bajo (0.1-0.3) puede ser suficiente",
                coeficienteVariacion
            ));
        }
        
        resultado.setAdvertencias(advertencias);
        
        // Generar recomendaciones
        List<String> recomendaciones = generarRecomendacionesSES(datosHistoricos, predicciones, alpha);
        resultado.setRecomendaciones(recomendaciones);
        
        logger.info("Prediccion {} completada exitosamente con alpha={}", CODIGO, alpha);
        
        return resultado;
    }
    
    /**
     * Obtiene el valor de alpha de los parámetros o usa el valor por defecto.
     * 
     * @param parametros Mapa de parámetros
     * @return Valor de alpha validado
     */
    private double obtenerAlpha(Map<String, Double> parametros) {
        double alpha = ALPHA_DEFAULT;
        
        if (parametros != null && parametros.containsKey(PARAMETRO_ALPHA)) {
            alpha = parametros.get(PARAMETRO_ALPHA);
        }
        
        // Validar rango
        if (alpha < ALPHA_MIN) {
            logger.warn("Alpha ({}) menor que el minimo ({}). Ajustando a {}",
                alpha, ALPHA_MIN, ALPHA_MIN);
            alpha = ALPHA_MIN;
        } else if (alpha > ALPHA_MAX) {
            logger.warn("Alpha ({}) mayor que el maximo ({}). Ajustando a {}",
                alpha, ALPHA_MAX, ALPHA_MAX);
            alpha = ALPHA_MAX;
        }
        
        return alpha;
    }
    
    /**
     * Detecta si existe una tendencia significativa en los datos.
     * 
     * @param datos Lista de valores históricos
     * @return true si se detecta tendencia, false en caso contrario
     */
    private boolean detectarTendencia(List<Double> datos) {
        if (datos.size() < 10) {
            return false;
        }
        
        // Dividir datos en dos mitades y comparar promedios
        int mitad = datos.size() / 2;
        double promedioInicio = calcularPromedio(datos.subList(0, mitad));
        double promedioFin = calcularPromedio(datos.subList(mitad, datos.size()));
        
        // Si la diferencia es mayor al 15%, consideramos que hay tendencia
        double diferenciaPorcentual = Math.abs((promedioFin - promedioInicio) / promedioInicio) * 100;
        
        return diferenciaPorcentual > 15.0;
    }
    
    /**
     * Genera recomendaciones específicas para SES basadas en el análisis.
     * 
     * @param datosHistoricos Datos históricos
     * @param predicciones Predicciones generadas
     * @param alpha Valor de alpha utilizado
     * @return Lista de recomendaciones
     */
    private List<String> generarRecomendacionesSES(List<Double> datosHistoricos, List<Double> predicciones, double alpha) {
        List<String> recomendaciones = new ArrayList<>();
        
        double promedioHistorico = calcularPromedio(datosHistoricos);
        double promedioPrediccion = predicciones.get(0); // En SES es constante
        
        double cambioProcentual = ((promedioPrediccion - promedioHistorico) / promedioHistorico) * 100;
        
        if (Math.abs(cambioProcentual) < 5) {
            recomendaciones.add("Demanda muy estable. Mantener politicas actuales de inventario");
        } else if (cambioProcentual > 5) {
            recomendaciones.add(String.format(
                "Ligero incremento esperado (%.1f%%). Monitorear de cerca en los proximos periodos",
                cambioProcentual
            ));
        } else {
            recomendaciones.add(String.format(
                "Ligera disminucion esperada (%.1f%%). Revisar politicas de reorden",
                Math.abs(cambioProcentual)
            ));
        }
        
        // Recomendación sobre alpha
        if (alpha < 0.3) {
            recomendaciones.add("Alpha bajo: El modelo prioriza estabilidad sobre reactividad");
        } else if (alpha > 0.7) {
            recomendaciones.add("Alpha alto: El modelo es muy reactivo a cambios recientes");
        }
        
        return recomendaciones;
    }
    
    @Override
    public String getCodigoAlgoritmo() {
        return CODIGO;
    }
    
    @Override
    public String getNombreAlgoritmo() {
        return NOMBRE;
    }
    
    @Override
    public int getMinimosDatosRequeridos() {
        return MINIMO_DATOS;
    }
    
    /**
     * Valida los parámetros específicos del algoritmo SES.
     * 
     * @param datosHistoricos Lista de valores históricos
     * @param parametros Parámetros del algoritmo
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Override
    public void validarDatos(List<Double> datosHistoricos, Map<String, Double> parametros) {
        // Validación base
        super.validarDatos(datosHistoricos, parametros);
        
        // Validar parámetro alpha si está presente
        if (parametros != null && parametros.containsKey(PARAMETRO_ALPHA)) {
            double alpha = parametros.get(PARAMETRO_ALPHA);
            
            if (alpha <= 0 || alpha >= 1) {
                String mensaje = String.format(
                    "El parametro 'alpha' debe estar entre 0 y 1 (exclusivo). Valor recibido: %.2f",
                    alpha
                );
                logger.error(mensaje);
                throw new IllegalArgumentException(mensaje);
            }
        }
    }
}