package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitud de creación/actualización de ParametroAlgoritmo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParametroAlgoritmoCreateRequest {

    @NotBlank(message = "El nombre del parámetro no puede estar vacío")
    private String nombreParametro;

    @NotNull(message = "El valor del parámetro no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El valor debe ser mayor o igual a 0")
    private BigDecimal valorParametro;

    @NotBlank(message = "El tipo de algoritmo no puede estar vacío")
    private String tipoAlgoritmo;

    private String descripcion;

    @DecimalMin(value = "0.0", message = "El valor mínimo debe ser mayor o igual a 0")
    private BigDecimal valorMinimo;

    @DecimalMax(value = "1.0", message = "El valor máximo debe ser menor o igual a 1")
    private BigDecimal valorMaximo;

    private Boolean activo;
}
