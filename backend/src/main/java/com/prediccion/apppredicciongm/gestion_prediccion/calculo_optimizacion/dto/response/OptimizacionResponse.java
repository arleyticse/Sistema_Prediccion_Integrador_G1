package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO Response con los resultados de la optimización de inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizacionResponse {
    
    // ===== IDENTIFICACIÓN =====
    private Long id;
    private Long prediccionId;
    private Long productoId;
    private String productoNombre;
    private String codigoProducto;
    
    // ===== EOQ - ECONOMIC ORDER QUANTITY =====
    
    /**
     * Cantidad Económica de Pedido (EOQ)
     * Es la cantidad óptima a ordenar que minimiza los costos totales
     */
    private Double cantidadEconomicaPedido;
    
    /**
     * Número óptimo de pedidos al año
     * Calculado como: Demanda Anual / EOQ
     */
    private Double numeroOptimoPedidos;
    
    /**
     * Ciclo óptimo de pedido en días
     * Calculado como: 365 / Número de Pedidos
     */
    private Double cicloOptimoDias;
    
    // ===== ROP - REORDER POINT =====
    
    /**
     * Punto de Reorden (ROP)
     * Nivel de inventario al cual se debe hacer un nuevo pedido
     */
    private Double puntoReorden;
    
    /**
     * Stock de Seguridad
     * Inventario adicional para protegerse de variabilidad en demanda/entrega
     */
    private Double stockSeguridad;
    
    /**
     * Stock Máximo recomendado
     * EOQ + Stock de Seguridad
     */
    private Double stockMaximo;
    
    // ===== COSTOS =====
    
    /**
     * Costo Total Anual de inventario
     * Suma de: Costo de Ordenamiento + Costo de Almacenamiento
     */
    private Double costoTotalAnual;
    
    /**
     * Costo de Ordenamiento anual
     * (Demanda Anual / EOQ) × Costo por Pedido
     */
    private Double costoOrdenamiento;
    
    /**
     * Costo de Almacenamiento anual
     * (EOQ / 2) × Costo de Almacenamiento por Unidad
     */
    private Double costoAlmacenamientoAnual;
    
    /**
     * Costo del Stock de Seguridad anual
     */
    private Double costoStockSeguridad;
    
    // ===== ANÁLISIS DE DEMANDA =====
    
    /**
     * Demanda Anual proyectada
     */
    private Double demandaAnual;
    
    /**
     * Demanda Diaria promedio
     */
    private Double demandaDiaria;
    
    /**
     * Desviación estándar de la demanda
     */
    private Double desviacionEstandarDemanda;
    
    /**
     * Coeficiente de variación (CV)
     * CV = Desviación / Media
     * Indica volatilidad: <0.15 estable, 0.15-0.30 moderado, >0.30 volátil
     */
    private Double coeficienteVariacion;
    
    // ===== PARÁMETROS UTILIZADOS =====
    
    private Double costoPedido;
    private Double costoAlmacenamiento;
    private Double costoUnitario;
    private Integer tiempoEntregaDias;
    private Double nivelServicioDeseado;
    
    /**
     * Factor Z de la distribución normal para el nivel de servicio
     * 90% = 1.28, 95% = 1.65, 97.5% = 1.96, 99% = 2.33
     */
    private Double factorZ;
    
    // ===== RECOMENDACIONES =====
    
    /**
     * Recomendación textual basada en el análisis
     */
    private String recomendacion;
    
    /**
     * Alertas o advertencias sobre el análisis
     */
    private String advertencia;
    
    /**
     * Nivel de confianza del análisis: ALTO, MEDIO, BAJO
     */
    private String nivelConfianza;
    
    // ===== METADATOS =====
    
    private LocalDateTime fechaCalculo;
    private String calculadoPor;
}
