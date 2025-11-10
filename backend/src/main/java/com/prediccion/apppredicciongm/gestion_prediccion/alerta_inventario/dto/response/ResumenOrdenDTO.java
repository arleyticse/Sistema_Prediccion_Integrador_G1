package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para el resumen de una orden de compra generada.
 * 
 * Se utiliza para mostrar información resumida de las órdenes
 * generadas durante el procesamiento automático de alertas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenOrdenDTO {
    
    /**
     * ID único de la orden de compra.
     */
    private Long ordenId;
    
    /**
     * Número de orden generado automáticamente.
     * Formato: OC-YYYY-NNNN
     */
    private String numeroOrden;
    
    /**
     * Información básica del proveedor.
     */
    private ProveedorResumenDTO proveedor;
    
    /**
     * Fecha de generación de la orden.
     */
    private LocalDate fechaOrden;
    
    /**
     * Fecha estimada de entrega basada en el lead time del proveedor.
     */
    private LocalDate fechaEntregaEsperada;
    
    /**
     * Monto total de la orden (suma de subtotales).
     */
    private BigDecimal totalOrden;
    
    /**
     * Estado actual de la orden.
     * Valores: BORRADOR, PENDIENTE, APROBADA, ENVIADA, RECIBIDA_PARCIAL, RECIBIDA_COMPLETA, CANCELADA
     */
    private String estadoOrden;
    
    /**
     * Cantidad de productos diferentes en la orden.
     */
    private Integer cantidadProductos;
    
    /**
     * Lista resumida de productos incluidos en la orden.
     */
    private List<ProductoResumenDTO> productos;
    
    /**
     * Indica si la orden fue generada automáticamente por el sistema.
     */
    private Boolean generadaAutomaticamente;
    
    /**
     * Observaciones adicionales de la orden.
     */
    private String observaciones;
    
    /**
     * DTO interno para información del proveedor.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProveedorResumenDTO {
        private Integer proveedorId;
        private String nombreComercial;
        private String razonSocial;
        private String ruc;
        private Integer tiempoEntrega;
    }
    
    /**
     * DTO interno para información resumida de productos.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoResumenDTO {
        private Integer productoId;
        private String nombre;
        private String codigoSKU;
        private Integer cantidadSolicitada;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
