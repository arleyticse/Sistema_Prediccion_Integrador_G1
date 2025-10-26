package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockResumenResponse {
    
    private Integer totalProductos;
    private Integer productosActivos;
    private Integer productosInactivos;
    private Integer productosConStockBajo;
    private Integer productosAgotados;
    private Integer productosSinMovimiento;
    private BigDecimal valorTotalInventario;
    private Integer stockTotalDisponible;
}
