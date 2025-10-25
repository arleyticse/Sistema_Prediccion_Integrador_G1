package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * DTO de solicitud para generar predicción.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPrediccionRequest {

    @NotNull(message = "El producto ID es requerido")
    @JsonProperty("productoId")
    private Integer productoId;

    @JsonProperty("diasHistoricos")
    @Min(value = 30, message = "Mínimo 30 días históricos")
    private Integer diasHistoricos = 60;

    @JsonProperty("diasPronostico")
    @Min(value = 7, message = "Mínimo 7 días de pronóstico")
    private Integer diasPronostico = 30;

    @JsonProperty("limpiarAnterior")
    private Boolean limpiarAnterior = true; // Limpia predicción anterior

    @JsonProperty("incluirDetalles")
    private Boolean incluirDetalles = true; // Incluye predicción por día
}
