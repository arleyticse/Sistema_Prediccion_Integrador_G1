package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response;

import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO completo con toda la información de una orden de compra.
 * Incluye datos empresariales, proveedor, detalles de productos y totales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenOrdenCompraDTO {
    
    private Long ordenCompraId;
    private String numeroOrden;
    private EstadoOrdenCompra estadoOrden;
    
    private LocalDate fechaOrden;
    private LocalDate fechaEntregaEsperada;
    private LocalDate fechaEntregaReal;
    
    private DatosEmpresaDTO empresa;
    private DatosProveedorDTO proveedor;
    
    private List<DetalleProductoOrdenDTO> detalles;
    
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal descuentos;
    private BigDecimal totalOrden;
    
    private Boolean generadaAutomaticamente;
    private String observaciones;
    
    private String usuarioCreador;
    private LocalDateTime fechaCreacion;
    private String usuarioAprobador;
    private LocalDateTime fechaAprobacion;
}
