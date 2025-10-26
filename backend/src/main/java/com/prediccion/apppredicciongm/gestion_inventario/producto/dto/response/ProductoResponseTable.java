package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductoResponseTable {
    private Integer productoId;
    private String nombre;
    private String unidadMedida;
    private BigDecimal costoAdquisicion;
    private LocalDateTime fechaRegistro;
}
