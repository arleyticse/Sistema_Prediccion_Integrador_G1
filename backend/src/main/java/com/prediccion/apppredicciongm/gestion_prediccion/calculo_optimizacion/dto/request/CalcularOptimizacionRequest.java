package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request para calcular optimización de inventario (EOQ y ROP)
 * 
 * EOQ: Economic Order Quantity (Cantidad Económica de Pedido)
 * ROP: Reorder Point (Punto de Reorden)
 * 
 * NOTA: Los parámetros de costos y tiempos son OPCIONALES.
 * Si no se proporcionan, se obtendrán automáticamente de la base de datos
 * del producto asociado a la predicción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcularOptimizacionRequest {
    
    @NotNull(message = "El ID de la predicción es obligatorio")
    private Long prediccionId;
    
    // ===== COSTOS (OPCIONALES - Se obtienen del producto si no se proporcionan) =====
    
    /**
     * Costo de realizar un pedido (S/)
     * OPCIONAL: Si no se proporciona, se obtiene de producto.costoPedido
     * Incluye: costo administrativo, transporte, recepción, etc.
     * Valor típico: 50-200 soles por pedido
     */
    @Positive(message = "El costo por pedido debe ser positivo")
    private Double costoPedido;
    
    /**
     * Costo de mantener inventario por unidad al año (S/)
     * OPCIONAL: Si no se proporciona, se obtiene de producto.costoMantenimientoAnual
     * Incluye: almacenamiento, seguro, obsolescencia, capital inmovilizado
     * Típicamente: 20-30% del costo unitario del producto
     */
    @Positive(message = "El costo de almacenamiento debe ser positivo")
    private Double costoAlmacenamiento;
    
    /**
     * Costo unitario del producto (S/)
     * OPCIONAL: Si no se proporciona, se obtiene de producto.costoAdquisicion
     * Precio de compra al proveedor
     */
    @Positive(message = "El costo unitario debe ser positivo")
    private Double costoUnitario;
    
    // ===== TIEMPOS (OPCIONALES - Se obtienen del producto si no se proporcionan) =====
    
    /**
     * Tiempo de entrega del proveedor en días (Lead Time)
     * OPCIONAL: Si no se proporciona, se obtiene de producto.diasLeadTime
     * Desde que se hace el pedido hasta que se recibe
     * Valor típico: 3-30 días
     */
    @Positive(message = "El tiempo de entrega debe ser positivo")
    private Integer tiempoEntregaDias;
    
    // ===== NIVEL DE SERVICIO =====
    
    /**
     * Nivel de servicio deseado (0.90 = 90%, 0.95 = 95%, 0.99 = 99%)
     * Determina el stock de seguridad necesario
     * A mayor nivel, mayor stock de seguridad
     * DEFAULT: 0.95 (95% de nivel de servicio)
     */
    @DecimalMin(value = "0.80", message = "El nivel de servicio debe ser al menos 80%")
    @DecimalMax(value = "0.99", message = "El nivel de servicio no puede exceder 99%")
    private Double nivelServicioDeseado;
    
    // ===== OPCIONALES =====
    
    /**
     * Desviación estándar de la demanda (calculada automáticamente si no se provee)
     */
    private Double desviacionEstandarDemanda;
}
