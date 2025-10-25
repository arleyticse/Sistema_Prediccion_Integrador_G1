package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalculoObtimizacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoObtimizacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz de servicio para CalculoObtimizacion
 */
public interface ICalculoObtimizacionServicio {

    /**
     * Calcula optimización de inventario (EOQ y ROP) para un producto
     */
    CalculoObtimizacionResponse calcularObtimizacion(Integer productoId, CalculoObtimizacionCreateRequest request);

    /**
     * Obtiene un cálculo por ID
     */
    CalculoObtimizacionResponse obtenerCalculoPorId(Integer calculoId);

    /**
     * Obtiene el último cálculo de un producto
     */
    CalculoObtimizacionResponse obtenerUltimoCalculoPorProducto(Integer productoId);

    /**
     * Lista cálculos de un producto
     */
    Page<CalculoObtimizacionResponse> listarCalculosPorProducto(Integer productoId, Pageable pageable);

    /**
     * Lista todos los cálculos
     */
    Page<CalculoObtimizacionResponse> listarTodosLosCalculos(Pageable pageable);

    /**
     * Actualiza un cálculo existente
     */
    CalculoObtimizacionResponse actualizarCalculo(Integer calculoId, CalculoObtimizacionCreateRequest request);

    /**
     * Elimina un cálculo
     */
    void eliminarCalculo(Integer calculoId);

    /**
     * Recalcula para todas las predicciones
     */
    void recalcularParaTodasLasPredicciones();
}
