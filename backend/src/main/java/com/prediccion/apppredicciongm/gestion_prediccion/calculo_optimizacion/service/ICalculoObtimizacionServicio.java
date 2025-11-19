package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalculoObtimizacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz de servicio para CalculoObtimizacion
 */
public interface ICalculoObtimizacionServicio {

    /**
     * Calcula optimización de inventario (EOQ y ROP) para un producto
     */
    CalculoOptimizacionResponse calcularObtimizacion(Integer productoId, CalculoObtimizacionCreateRequest request);

    /**
     * Obtiene un cálculo por ID
     */
    CalculoOptimizacionResponse obtenerCalculoPorId(Integer calculoId);

    /**
     * Obtiene el último cálculo de un producto
     */
    CalculoOptimizacionResponse obtenerUltimoCalculoPorProducto(Integer productoId);

    /**
     * Lista cálculos de un producto
     */
    Page<CalculoOptimizacionResponse> listarCalculosPorProducto(Integer productoId, Pageable pageable);

    /**
     * Lista todos los cálculos
     */
    Page<CalculoOptimizacionResponse> listarTodosLosCalculos(Pageable pageable);

    /**
     * Actualiza un cálculo existente
     */
    CalculoOptimizacionResponse actualizarCalculo(Integer calculoId, CalculoObtimizacionCreateRequest request);

    /**
     * Elimina un cálculo
     */
    void eliminarCalculo(Integer calculoId);

    /**
     * Recalcula para todas las predicciones
     */
    void recalcularParaTodasLasPredicciones();
}
