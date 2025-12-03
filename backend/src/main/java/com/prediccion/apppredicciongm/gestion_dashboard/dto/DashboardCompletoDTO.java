package com.prediccion.apppredicciongm.gestion_dashboard.dto;

import java.util.List;

/**
 * DTO completo con todos los datos necesarios para el dashboard del gerente.
 * Agrupa todas las metricas, graficos y listas en una sola respuesta.
 */
public record DashboardCompletoDTO(
    DashboardEstadisticasDTO estadisticas,
    List<DistribucionInventarioDTO> distribucionInventario,
    List<TendenciaMovimientosDTO> tendenciaMovimientos,
    List<ProductoMasVendidoDTO> productosMasVendidos,
    List<ProductoStockBajoDTO> productosStockBajo,
    List<DistribucionCategoriaDTO> distribucionCategorias,
    List<DistribucionAlertasDTO> distribucionAlertas,
    ResumenMovimientosDTO resumenMovimientos
) {}
