package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalcularOptimizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.OptimizacionResponse;

import java.util.List;

/**
 * Servicio para procesamiento en lote de optimizaciones EOQ/ROP.
 * 
 * Calcula optimizaciones de inventario para multiples productos
 * de forma paralela basandose en sus predicciones.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public interface IOptimizacionBatchService {

    /**
     * Ejecuta calculos de optimizacion EOQ/ROP para multiples predicciones.
     * 
     * Proceso:
     * 1. Valida predicciones
     * 2. Obtiene parametros de costos desde el producto
     * 3. Ejecuta calculos en paralelo
     * 4. Almacena resultados
     * 5. Retorna resumen del procesamiento
     * 
     * @param prediccionIds Lista de IDs de predicciones
     * @param nivelServicio Nivel de servicio deseado (0.90-0.99)
     * @return Resumen del procesamiento con exitos y errores
     */
    ProcesamientoBatchResponse ejecutarOptimizacionesBatch(
        List<Integer> prediccionIds, 
        Double nivelServicio
    );

    /**
     * Ejecuta una optimizacion para una prediccion especifica.
     * 
     * @param prediccionId ID de la prediccion
     * @param nivelServicio Nivel de servicio deseado
     * @return Resultado de la optimizacion
     */
    OptimizacionResponse ejecutarOptimizacionIndividual(
        Integer prediccionId, 
        Double nivelServicio
    );
}
