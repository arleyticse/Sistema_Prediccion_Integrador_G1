package com.prediccion.apppredicciongm.gestion_dashboard.dto;

import java.time.LocalDate;

/**
 * DTO para representar la tendencia de movimientos diarios (entradas y salidas).
 * Usado en graficos de lineas para mostrar flujo de inventario en el tiempo.
 */
public record TendenciaMovimientosDTO(
    LocalDate fecha,
    Long entradas,
    Long salidas
) {}
