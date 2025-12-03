package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar la distribucion del inventario por estado.
 * Usado en graficos de pie/doughnut para mostrar estado del stock.
 */
public record DistribucionInventarioDTO(
    String estado,
    Long cantidad
) {}
