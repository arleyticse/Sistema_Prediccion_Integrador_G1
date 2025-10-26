package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import com.prediccion.apppredicciongm.models.Prediccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz del servicio de predicción ARIMA.
 * Define métodos para generar y consultar predicciones de demanda.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public interface IPrediccionService {

    /**
     * Genera una predicción ARIMA para un producto.
     *
     * @param productoId ID del producto
     * @param diasProcesar número de días a procesar
     * @return la predicción generada
     */
    Prediccion generarPrediccion(Integer productoId, int diasProcesar);

    /**
     * Obtiene la última predicción de un producto.
     *
     * @param productoId ID del producto
     * @return la última predicción
     */
    Prediccion obtenerUltimaPrediccion(Integer productoId);

    /**
     * Obtiene predicciones paginadas para un producto.
     *
     * @param productoId ID del producto
     * @param pageable información de paginación
     * @return lista de predicciones
     */
    List<Prediccion> obtenerPrediccionesByProducto(Integer productoId, Pageable pageable);

    /**
     * Obtiene todas las predicciones con paginación.
     *
     * @param pageable información de paginación
     * @return página de predicciones
     */
    Page<Prediccion> obtenerTodasLasPredicciones(Pageable pageable);

    /**
     * Actualiza la precisión de una predicción.
     *
     * @param prediccionId ID de la predicción
     * @param nuevaPrecision nueva precisión
     */
    void actualizarPrecision(Long prediccionId, double nuevaPrecision);

    /**
     * Elimina una predicción.
     *
     * @param prediccionId ID de la predicción
     */
    void eliminarPrediccion(Long prediccionId);
}
