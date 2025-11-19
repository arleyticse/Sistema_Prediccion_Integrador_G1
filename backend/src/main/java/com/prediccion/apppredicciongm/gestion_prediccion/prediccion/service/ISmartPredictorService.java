package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.SmartPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;

import java.util.List;

/**
 * Interfaz del servicio de predicción inteligente con Machine Learning.
 * 
 * Proporciona funcionalidades avanzadas de predicción usando múltiples algoritmos de ML,
 * selección automática del mejor modelo, detección de estacionalidad y generación
 * automática de órdenes de compra.
 */
public interface ISmartPredictorService {

    /**
     * Genera una predicción inteligente usando Machine Learning.
     * 
     * Este método:
     * 1. Obtiene datos históricos del producto desde registro_demanda
     * 2. Entrena múltiples modelos de ML (OLS, Random Forest, GBM, etc.)
     * 3. Selecciona automáticamente el mejor modelo basado en métricas de validación
     * 4. Genera predicciones para el horizonte especificado
     * 5. Detecta patrones de estacionalidad si está habilitado
     * 6. Analiza riesgo de quiebre de stock
     * 7. Sugiere orden de compra si es necesario
     * 
     * @param request Configuración de la predicción inteligente
     * @return Resultado completo con predicciones, métricas y recomendaciones
     * @throws IllegalArgumentException Si el producto no existe o no tiene datos históricos suficientes
     * @throws RuntimeException Si ocurre un error durante el entrenamiento de los modelos
     */
    SmartPrediccionResponse generarPrediccionInteligente(SmartPrediccionRequest request);

    /**
     * Obtiene predicciones inteligentes para todos los productos con alertas de inventario.
     * 
     * Este método procesa automáticamente todos los productos que tienen alertas
     * activas de inventario y genera predicciones para ayudar en la toma de decisiones.
     * 
     * @return Lista de predicciones para productos con alertas
     */
    List<SmartPrediccionResponse> procesarProductosConAlertas();

    /**
     * Valida si un producto tiene suficientes datos históricos para generar una predicción confiable.
     * 
     * @param idProducto ID del producto a validar
     * @param minimoRegistros Número mínimo de registros requeridos
     * @return true si tiene datos suficientes, false en caso contrario
     */
    boolean validarDatosHistoricosProducto(Long idProducto, int minimoRegistros);

    /**
     * Obtiene los algoritmos disponibles para predicción.
     * 
     * @return Lista de algoritmos disponibles con sus descripciones
     */
    List<String> obtenerAlgoritmosDisponibles();

    /**
     * Obtiene las métricas de calidad de una predicción específica.
     * 
     * @param idPrediccion ID de la predicción
     * @return Métricas detalladas de calidad del modelo utilizado
     */
    SmartPrediccionResponse.MetricasCalidad obtenerMetricasCalidad(Long idPrediccion);
}