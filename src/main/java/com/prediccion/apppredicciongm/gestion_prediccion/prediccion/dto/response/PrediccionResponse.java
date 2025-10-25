package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para predicciones ARIMA.
 * Contiene información de la predicción generada.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionResponse {

    @JsonProperty("prediccionId")
    private Long prediccionId;

    @JsonProperty("productoId")
    private Integer productoId;

    @JsonProperty("productoNombre")
    private String productoNombre;

    @JsonProperty("demandaPredichaTotal")
    private Integer demandaPredichaTotal;

    @JsonProperty("algoritmo")
    private String algoritmo; // "ARIMA-Simple"

    @JsonProperty("precision")
    private Double precision; // MAE (Mean Absolute Error)

    @JsonProperty("fechaGeneracion")
    private LocalDateTime fechaGeneracion;

    @JsonProperty("vigenciaHasta")
    private LocalDateTime vigenciaHasta; // 30 días desde generación

    @JsonProperty("descripcion")
    private String descripcion; // Descripción amigable de la predicción

    @JsonProperty("detallePronostico")
    private String detallePronostico; // JSON con detalles por día

    /**
     * Constructor simplificado para casos básicos.
     */
    public PrediccionResponse(Long prediccionId, Integer productoId, String productoNombre,
                            Integer demandaPredichaTotal, String algoritmo, Double precision) {
        this.prediccionId = prediccionId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.demandaPredichaTotal = demandaPredichaTotal;
        this.algoritmo = algoritmo;
        this.precision = precision;
        this.fechaGeneracion = LocalDateTime.now();
        this.vigenciaHasta = LocalDateTime.now().plusDays(30);
    }
}
