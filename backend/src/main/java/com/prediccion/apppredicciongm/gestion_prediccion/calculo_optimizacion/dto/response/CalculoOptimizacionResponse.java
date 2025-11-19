package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response para cálculos de optimización de inventario.
 * Mapea directamente con la entidad CalculoObtimizacion.
 * 
 * @author Sistema de Predicción Unificado
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoOptimizacionResponse {
    
    /**
     * ID del cálculo
     */
    private Integer calculoId;
    
    /**
     * ID del producto al que pertenece el cálculo
     */
    private Integer productoId;
    
    /**
     * Nombre del producto para mostrar en UI
     */
    private String productoNombre;
    
    /**
     * Código del producto
     */
    private String productoCodigo;
    
    /**
     * Fecha en que se realizó el cálculo
     */
    private LocalDateTime fechaCalculo;
    
    /**
     * Demanda anual estimada del producto
     */
    private Integer demandaAnualEstimada;
    
    /**
     * Cantidad Económica de Pedido (EOQ)
     * Es la cantidad óptima a ordenar que minimiza costos
     */
    private Integer eoqCantidadOptima;
    
    /**
     * Punto de Reorden (ROP)
     * Nivel de inventario al cual se debe hacer un nuevo pedido
     */
    private Integer ropPuntoReorden;
    
    /**
     * Stock de seguridad sugerido
     * Protección contra variabilidad en demanda/entrega
     */
    private Integer stockSeguridadSugerido;
    
    /**
     * Stock de seguridad aplicado
     */
    private Integer stockSeguridad;
    
    /**
     * Costo total de inventario anual
     */
    private BigDecimal costoTotalInventario;
    
    /**
     * Costo de mantenimiento anual por unidad
     */
    private BigDecimal costoMantenimiento;
    
    /**
     * Costo fijo por realizar un pedido
     */
    private BigDecimal costoPedido;
    
    /**
     * Tiempo de entrega del proveedor en días (Lead Time)
     */
    private Integer diasLeadTime;
    
    /**
     * Costo unitario del producto
     */
    private BigDecimal costoUnitario;
    
    /**
     * Número óptimo de órdenes al año
     */
    private Integer numeroOrdenesAnuales;
    
    /**
     * Días entre cada lote/pedido
     */
    private Integer diasEntreLotes;
    
    /**
     * Observaciones del cálculo
     */
    private String observaciones;
    
    /**
     * Fecha de última actualización
     */
    private LocalDateTime fechaActualizacion;
}
