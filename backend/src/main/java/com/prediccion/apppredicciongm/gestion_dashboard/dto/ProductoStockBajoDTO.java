package com.prediccion.apppredicciongm.gestion_dashboard.dto;

/**
 * DTO para representar productos con stock bajo que requieren atencion.
 * Incluye informacion relevante para decision de reorden.
 */
public record ProductoStockBajoDTO(
    Integer productoId,
    String nombre,
    Integer stockActual,
    Integer stockMinimo,
    Integer puntoReorden,
    String estado,
    String categoriaNombre
) {}
