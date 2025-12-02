package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalcularOptimizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.OptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para calcular optimización de inventario (EOQ/ROP)
 * basado en predicciones de demanda
 */
public interface IOptimizacionInventarioService {
    
    // ========== MÉTODO PRINCIPAL (INTEGRACIÓN ML → EOQ/ROP) ==========
    
    /**
     * Calcula EOQ y ROP directamente desde una predicción ML.
     * Este es el método principal que integra predicción con optimización.
     * Guarda el resultado en BD.
     * 
     * @param prediccion Resultado de predicción ML
     * @param productoId ID del producto
     * @return Cálculo de optimización guardado en BD
     */
    CalculoOptimizacionResponse calcularEOQROPDesdePrediccion(
            SmartPrediccionResponse prediccion,
            Long productoId);
    
    /**
     * Calcula EOQ y ROP desde una predicción ML con opción de no guardar.
     * Versión optimizada para procesamiento batch donde no se requiere persistir.
     * 
     * @param prediccion Resultado de predicción ML
     * @param productoId ID del producto
     * @param persistir Si true, guarda en BD; si false, solo retorna el cálculo
     * @return Cálculo de optimización (persistido o no según parámetro)
     */
    CalculoOptimizacionResponse calcularEOQROPDesdePrediccion(
            SmartPrediccionResponse prediccion,
            Long productoId,
            boolean persistir);
    
    /**
     * Obtiene el último cálculo de optimización para un producto
     * 
     * @param productoId ID del producto
     * @return Cálculo si existe
     */
    Optional<CalculoOptimizacionResponse> obtenerCalculoPorProducto(Long productoId);
    
    /**
     * Obtiene todos los cálculos de optimización
     * 
     * @return Lista de todos los cálculos
     */
    List<CalculoOptimizacionResponse> obtenerTodosLosCalculos();
    
    // ========== MÉTODOS LEGACY (COMPATIBILIDAD) ==========
    
    /**
     * Calcula la optimización completa (EOQ + ROP) basada en una predicción
     * 
     * @param request Parámetros de costos y tiempos
     * @return Resultados de optimización con EOQ, ROP, costos y recomendaciones
     */
    OptimizacionResponse calcularOptimizacion(CalcularOptimizacionRequest request);
    
    /**
     * Obtiene la última optimización calculada para una predicción
     * 
     * @param prediccionId ID de la predicción
     * @return Optimización guardada o null si no existe
     */
    OptimizacionResponse obtenerOptimizacionPorPrediccion(Long prediccionId);
    
    /**
     * Calcula solo EOQ (Economic Order Quantity)
     * Fórmula: EOQ = √((2 × D × S) / H)
     * 
     * @param demandaAnual Demanda anual del producto
     * @param costoPedido Costo de realizar un pedido
     * @param costoAlmacenamiento Costo de mantener inventario por unidad/año
     * @return Cantidad económica de pedido
     */
    Double calcularEOQ(Double demandaAnual, Double costoPedido, Double costoAlmacenamiento);
    
    /**
     * Calcula solo ROP (Reorder Point)
     * Fórmula: ROP = d × L + SS
     * 
     * @param demandaDiaria Demanda diaria promedio
     * @param tiempoEntregaDias Lead time en días
     * @param stockSeguridad Stock de seguridad calculado
     * @return Punto de reorden
     */
    Double calcularROP(Double demandaDiaria, Integer tiempoEntregaDias, Double stockSeguridad);
    
    /**
     * Calcula Stock de Seguridad
     * Fórmula: SS = Z × σ × √L
     * 
     * @param factorZ Factor de servicio (1.65 = 95%, 1.96 = 97.5%, 2.33 = 99%)
     * @param desviacionEstandar Desviación estándar de la demanda
     * @param tiempoEntregaDias Lead time en días
     * @return Stock de seguridad
     */
    Double calcularStockSeguridad(Double factorZ, Double desviacionEstandar, Integer tiempoEntregaDias);
    
    /**
     * Obtiene el factor Z de la distribución normal según nivel de servicio
     * 
     * @param nivelServicio Nivel de servicio deseado (0.90 = 90%, 0.95 = 95%, etc.)
     * @return Factor Z correspondiente
     */
    Double obtenerFactorZ(Double nivelServicio);
}
