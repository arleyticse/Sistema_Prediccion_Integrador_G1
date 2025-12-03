package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar distribucion de productos por categoria.
 * Usado en graficos de barras/pie para analisis de surtido.
 */
public record DistribucionCategoriaDTO(
    Integer categoriaId,
    String nombre,
    Long cantidadProductos
) {}
