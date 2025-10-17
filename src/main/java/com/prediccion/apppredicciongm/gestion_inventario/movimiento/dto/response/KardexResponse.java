package com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KardexResponse {
    
    private Long kardexId;
    private Integer productoId;
    private String nombreProducto;
    private String categoriaProducto;
    private LocalDateTime fechaMovimiento;
    private TipoMovimiento tipoMovimiento;
    private String tipoDocumento;
    private String numeroDocumento;
    private Integer cantidad;
    private Integer saldoCantidad;
    private BigDecimal costoUnitario;
    private BigDecimal valorTotal;
    private String lote;
    private LocalDateTime fechaVencimiento;
    private Integer proveedorId;
    private String nombreProveedor;
    private String motivo;
    private String referencia;
    private Integer usuarioId;
    private String nombreUsuario;
    private String observaciones;
    private String ubicacion;
    private LocalDateTime fechaRegistro;
}
