package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductoCreateRequest {
    private String nombre;
    private Integer categoriaId;
    private Integer unidadMedidaId;
    private Integer leadTimes;
    private BigDecimal costoAdquisicion;
    private BigDecimal costoMantenimiento;
    private BigDecimal costoPedido;
}
