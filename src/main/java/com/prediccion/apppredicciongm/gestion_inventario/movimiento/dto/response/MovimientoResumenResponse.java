package com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoResumenResponse {
    
    private Integer totalMovimientos;
    private Integer totalEntradas;
    private Integer totalSalidas;
    private Integer totalAjustes;
    private Integer cantidadTotalEntrada;
    private Integer cantidadTotalSalida;
    private LocalDateTime fechaUltimoMovimiento;
    private String productoMasMovido;
}
