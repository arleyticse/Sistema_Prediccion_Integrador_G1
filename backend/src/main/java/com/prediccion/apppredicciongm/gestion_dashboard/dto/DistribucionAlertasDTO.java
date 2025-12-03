package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar la distribucion de alertas por tipo.
 * Usado en graficos para analisis de alertas del sistema.
 */
public record DistribucionAlertasDTO(
    String tipo,
    Long cantidad
) {}
