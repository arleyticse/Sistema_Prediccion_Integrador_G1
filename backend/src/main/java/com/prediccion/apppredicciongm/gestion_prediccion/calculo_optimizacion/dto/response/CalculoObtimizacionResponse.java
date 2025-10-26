package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de CalculoObtimizacion
 * Retorna resultados de cálculos de EOQ, ROP y sugerencias de órdenes
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculoObtimizacionResponse {

    private Integer calculoId;

    private ProductoResponse producto;

    private LocalDateTime fechaCalculo;

    private Integer demandaAnualEstimada;

    private Integer eoqCantidadOptima;

    private Integer ropPuntoReorden;

    private Integer stockSeguridadSugerido;

    private BigDecimal costoTotalInventario;

    private BigDecimal costoMantenimiento;

    private BigDecimal costoPedido;

    private Integer diasLeadTime;

    private BigDecimal costoUnitario;

    private Integer numeroOrdenesAnuales;

    private Integer diasEntreLotes;

    private String observaciones;

    private LocalDateTime fechaActualizacion;
}
