package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con el detalle de un producto en la orden de compra.
 * Incluye cantidades, precios unitarios y subtotales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleProductoOrdenDTO {
    
    private Long detalleId;
    
    private Long productoId;
    private String codigoProducto;
    private String nombreProducto;
    private String descripcion;
    private String unidadMedida;
    
    private Integer cantidadSolicitada;
    private Integer cantidadRecibida;
    
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;
    
    private String observaciones;
}
