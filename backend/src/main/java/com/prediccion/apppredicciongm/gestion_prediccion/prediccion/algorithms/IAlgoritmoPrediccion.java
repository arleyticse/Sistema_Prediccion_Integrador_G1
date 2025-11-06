package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;

import java.util.List;
import java.util.Map;

/**
 * Interfaz base para todos los algoritmos de predicción de demanda.
 * 
 * Define el contrato que deben cumplir todos los algoritmos de predicción
 * implementados en el sistema. Cada algoritmo debe ser capaz de procesar
 * datos históricos y generar predicciones con sus respectivas métricas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */
public interface IAlgoritmoPrediccion {
    
    /**
     * Ejecuta el algoritmo de predicción con los datos históricos proporcionados.
     * 
     * @param datosHistoricos Lista de valores históricos de demanda ordenados cronológicamente
     * @param horizonteTiempo Número de períodos futuros a predecir
     * @param parametros Mapa de parámetros específicos del algoritmo (alpha, beta, gamma, ventana, etc.)
     * @return ResultadoPrediccionDTO con las predicciones y métricas calculadas
     * @throws IllegalArgumentException si los datos históricos son insuficientes o parámetros inválidos
     */
    ResultadoPrediccionDTO predecir(List<Double> datosHistoricos, int horizonteTiempo, Map<String, Double> parametros);
    
    /**
     * Obtiene el código identificador único del algoritmo.
     * 
     * @return String con el código del algoritmo
     */
    String getCodigoAlgoritmo();
    
    /**
     * Obtiene el nombre descriptivo del algoritmo.
     * 
     * @return String con el nombre completo del algoritmo
     */
    String getNombreAlgoritmo();
    
    /**
     * Valida que los datos históricos y parámetros sean suficientes para ejecutar el algoritmo.
     * 
     * @param datosHistoricos Lista de valores históricos
     * @param parametros Parámetros del algoritmo
     * @throws IllegalArgumentException si la validación falla
     */
    void validarDatos(List<Double> datosHistoricos, Map<String, Double> parametros);
    
    /**
     * Obtiene la cantidad mínima de datos históricos requeridos por el algoritmo.
     * 
     * @return int cantidad mínima de observaciones históricas necesarias
     */
    int getMinimosDatosRequeridos();
}