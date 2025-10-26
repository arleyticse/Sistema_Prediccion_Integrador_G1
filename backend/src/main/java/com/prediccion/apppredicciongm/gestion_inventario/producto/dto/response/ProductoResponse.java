package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;
import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {
    
    private Integer productoId;
    private String nombre;
    private Categoria categoria;
    private UnidadMedida unidadMedida;
    private Integer diasLeadTime;
    private BigDecimal costoAdquisicion;
    private BigDecimal costoMantenimiento;
    private BigDecimal costoMantenimientoAnual;
    private BigDecimal costoPedido;
    private LocalDateTime fechaRegistro;
    
    private Boolean tieneInventario;
    private Integer stockDisponible;
    private Integer stockMinimo;
    private Integer puntoReorden;
    private String estadoInventario;
    private BigDecimal valorInventario;
}
