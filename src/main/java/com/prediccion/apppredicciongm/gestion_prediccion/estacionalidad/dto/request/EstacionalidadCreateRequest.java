package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una estacionalidad de producto.
 * Contiene los datos necesarios para registrar patrones estacionales de demanda.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstacionalidadCreateRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El mes es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer mes;

    @NotNull(message = "El factor estacional es obligatorio")
    @DecimalMin(value = "0.1", message = "El factor estacional debe ser mayor a 0.1")
    @DecimalMax(value = "10.0", message = "El factor estacional debe ser menor a 10.0")
    private BigDecimal factorEstacional;

    @NotNull(message = "La demanda promedio histórica es obligatoria")
    @Min(value = 0, message = "La demanda promedio no puede ser negativa")
    private Integer demandaPromedioHistorica;

    @NotNull(message = "La demanda máxima es obligatoria")
    @Min(value = 0, message = "La demanda máxima no puede ser negativa")
    private Integer demandaMaxima;

    @NotNull(message = "La demanda mínima es obligatoria")
    @Min(value = 0, message = "La demanda mínima no puede ser negativa")
    private Integer demandaMinima;

    @NotNull(message = "El año de referencia es obligatorio")
    @Min(value = 2000, message = "El año debe ser mayor a 2000")
    @Max(value = 2100, message = "El año debe ser menor a 2100")
    private Integer anioReferencia;

    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    private String descripcionTemporada;

    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    private String observaciones;
}
