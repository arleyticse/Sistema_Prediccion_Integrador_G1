package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitud de creación/actualización de CalculoObtimizacion
 * Contiene parámetros necesarios para calcular EOQ (cantidad óptima) y ROP (punto de reorden)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculoObtimizacionCreateRequest {

    @NotNull(message = "El producto ID no puede ser nulo")
    @Positive(message = "El producto ID debe ser positivo")
    private Integer productoId;

    @NotNull(message = "La demanda anual estimada no puede ser nula")
    @Positive(message = "La demanda anual estimada debe ser positiva")
    private Integer demandaAnualEstimada;

    @NotNull(message = "El costo de mantenimiento no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El costo de mantenimiento debe ser mayor a 0")
    private BigDecimal costoMantenimiento;

    @NotNull(message = "El costo de pedido no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El costo de pedido debe ser mayor a 0")
    private BigDecimal costoPedido;

    @NotNull(message = "Los días de lead time no pueden ser nulos")
    @Min(value = 1, message = "Los días de lead time deben ser al menos 1")
    private Integer diasLeadTime;

    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a 0")
    private BigDecimal costoUnitario;

    @Min(value = 0, message = "El stock de seguridad no puede ser negativo")
    private Integer stockSeguridad;

    private String observaciones;
}
