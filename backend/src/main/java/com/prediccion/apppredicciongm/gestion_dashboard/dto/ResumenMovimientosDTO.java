package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar resumen de movimientos del periodo.
 * Proporciona totales de entradas, salidas y mermas.
 */
public record ResumenMovimientosDTO(
    Long totalEntradas,
    Long totalSalidas,
    Long totalMermas,
    Long cantidadEntradas,
    Long cantidadSalidas,
    Long cantidadMermas
) {}
