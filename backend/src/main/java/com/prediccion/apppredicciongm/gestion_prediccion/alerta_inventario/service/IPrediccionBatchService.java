package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ProcesarAlertasRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;

/**
 * Servicio para procesamiento en lote de predicciones.
 * 
 * Ejecuta predicciones automaticas para multiples productos
 * de forma paralela y eficiente.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public interface IPrediccionBatchService {

    /**
     * Ejecuta predicciones automaticas para una lista de productos.
     * 
     * Proceso:
     * 1. Valida alertas y productos
     * 2. Ejecuta predicciones en paralelo (modo automatico)
     * 3. Almacena resultados
     * 4. Retorna resumen del procesamiento
     * 
     * @param request Solicitud con lista de alertas y parametros
     * @return Resumen del procesamiento con exitos y errores
     */
    ProcesamientoBatchResponse ejecutarPrediccionesBatch(ProcesarAlertasRequest request);
}
