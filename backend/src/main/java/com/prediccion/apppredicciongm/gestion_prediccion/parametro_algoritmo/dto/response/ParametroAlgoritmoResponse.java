package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de ParametroAlgoritmo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParametroAlgoritmoResponse {

    private Integer parametroId;

    private String nombreParametro;

    private BigDecimal valorParametro;

    private String tipoAlgoritmo;

    private String descripcion;

    private BigDecimal valorMinimo;

    private BigDecimal valorMaximo;

    private Boolean activo;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
}
