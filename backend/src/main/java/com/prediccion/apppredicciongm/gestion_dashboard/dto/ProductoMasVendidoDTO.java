package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar productos mas vendidos.
 * Usado en graficos de barras para mostrar ranking de ventas.
 */
public record ProductoMasVendidoDTO(
    Integer productoId,
    String nombre,
    Long cantidadVendida
) {}
