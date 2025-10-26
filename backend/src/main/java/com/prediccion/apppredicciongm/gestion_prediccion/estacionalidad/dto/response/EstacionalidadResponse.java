package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.response;

import java.math.BigDecimal;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estacionalidad de producto.
 * Contiene información completa de un patrón estacional registrado.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstacionalidadResponse {

    private Long estacionalidadId;

    private ProductoResponse producto;

    private Integer mes;

    private BigDecimal factorEstacional;

    private Integer demandaPromedioHistorica;

    private Integer demandaMaxima;

    private Integer demandaMinima;

    private Integer anioReferencia;

    private String descripcionTemporada;

    private String observaciones;
}
