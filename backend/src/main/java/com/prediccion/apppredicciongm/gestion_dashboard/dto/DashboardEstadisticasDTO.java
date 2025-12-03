package com.prediccion.apppredicciongm.gestion_dashboard.dto;

import java.math.BigDecimal;

/**
 * DTO con estadisticas generales del negocio para el dashboard del gerente.
 * Agrupa metricas clave para una vision rapida del estado del minimarket.
 */
public record DashboardEstadisticasDTO(
    Long totalProductos,
    Long productosActivos,
    Long productosStockBajo,
    Long productosStockCritico,
    Long productosExceso,
    Long alertasPendientes,
    Long alertasCriticas,
    Long proveedoresActivos,
    Long ordenesPendientes,
    Long ordenesBorrador,
    BigDecimal valorInventarioTotal,
    Long stockTotalUnidades
) {}
