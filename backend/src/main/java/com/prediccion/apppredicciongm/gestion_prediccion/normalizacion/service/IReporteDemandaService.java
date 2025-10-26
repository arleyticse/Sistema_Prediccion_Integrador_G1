package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.dto.request.NormalizacionRequest;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

/**
 * Interfaz del servicio de normalización de demanda.
 * Define los métodos para procesar el historial de movimientos de kardex
 * y generar registros de demanda agregados por día.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
public interface IReporteDemandaService {

    /**
     * Normaliza la demanda para un producto específico extrayendo
     * movimientos de tipo SALIDA_VENTA del kardex y agregándolos por día.
     *
     * @param producto el producto a normalizar
     * @param diasProcesar número de días hacia atrás a procesar
     * @return número de registros de demanda creados o actualizados
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.errors.NormalizacionException
     *         si hay error en el proceso
     */
    int normalizarDemandaProducto(Producto producto, int diasProcesar);

    /**
     * Normaliza la demanda para todos los productos disponibles.
     * Ejecuta normalizarDemandaProducto para cada producto.
     *
     * @param diasProcesar número de días hacia atrás a procesar
     * @return número total de registros procesados
     */
    int normalizarDemandaTodos(int diasProcesar);

    /**
     * Procesa una solicitud de normalización manual.
     * Valida parámetros y ejecuta la normalización según lo especificado.
     *
     * @param request solicitud con parámetros de normalización
     * @return número de registros procesados
     */
    int procesarNormalizacionManual(NormalizacionRequest request);

    /**
     * Calcula la cantidad de registros de demanda históricos disponibles
     * para un producto. Útil para validar si hay datos suficientes.
     *
     * @param producto el producto a validar
     * @return número de registros disponibles
     */
    long obtenerCantidadDatosHistoricos(Producto producto);

    /**
     * Verifica si hay datos suficientes para realizar una predicción.
     * Se requiere un mínimo de 12 registros (1 año de datos mensuales).
     *
     * @param producto el producto a validar
     * @return true si hay datos suficientes, false en caso contrario
     */
    boolean hayDatosSuficientes(Producto producto);

    /**
     * Limpia todos los registros de demanda para un producto.
     * ADVERTENCIA: Esta operación es destructiva y no se puede deshacer.
     *
     * @param producto el producto a limpiar
     * @return número de registros eliminados
     */
    int limpiarDemandaProducto(Producto producto);
}
