package com.prediccion.apppredicciongm.gestion_dashboard.controller;

import com.prediccion.apppredicciongm.gestion_dashboard.dto.*;
import com.prediccion.apppredicciongm.gestion_dashboard.service.DashboardService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para los endpoints del dashboard del gerente.
 * 
 * Expone metricas y datos agregados para visualizacion en el frontend.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Obtiene el dashboard completo con todas las metricas y graficos.
     * Este endpoint retorna toda la informacion necesaria en una sola llamada.
     */
    @GetMapping("/completo")
    public ResponseEntity<DashboardCompletoDTO> obtenerDashboardCompleto() {
        log.info("GET /api/dashboard/completo - Solicitando dashboard completo");
        DashboardCompletoDTO dashboard = dashboardService.obtenerDashboardCompleto();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene solo las estadisticas generales del negocio.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<DashboardEstadisticasDTO> obtenerEstadisticas() {
        log.debug("GET /api/dashboard/estadisticas");
        DashboardEstadisticasDTO estadisticas = dashboardService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene la distribucion del inventario por estado.
     */
    @GetMapping("/distribucion-inventario")
    public ResponseEntity<List<DistribucionInventarioDTO>> obtenerDistribucionInventario() {
        log.debug("GET /api/dashboard/distribucion-inventario");
        List<DistribucionInventarioDTO> distribucion = dashboardService.obtenerDistribucionInventario();
        return ResponseEntity.ok(distribucion);
    }

    /**
     * Obtiene la distribucion de productos por categoria.
     */
    @GetMapping("/distribucion-categorias")
    public ResponseEntity<List<DistribucionCategoriaDTO>> obtenerDistribucionCategorias() {
        log.debug("GET /api/dashboard/distribucion-categorias");
        List<DistribucionCategoriaDTO> distribucion = dashboardService.obtenerDistribucionCategorias();
        return ResponseEntity.ok(distribucion);
    }

    /**
     * Obtiene la distribucion de alertas por tipo.
     */
    @GetMapping("/distribucion-alertas")
    public ResponseEntity<List<DistribucionAlertasDTO>> obtenerDistribucionAlertas() {
        log.debug("GET /api/dashboard/distribucion-alertas");
        List<DistribucionAlertasDTO> distribucion = dashboardService.obtenerDistribucionAlertas();
        return ResponseEntity.ok(distribucion);
    }

    /**
     * Obtiene los productos con stock bajo.
     */
    @GetMapping("/productos-stock-bajo")
    public ResponseEntity<List<ProductoStockBajoDTO>> obtenerProductosStockBajo() {
        log.debug("GET /api/dashboard/productos-stock-bajo");
        List<ProductoStockBajoDTO> productos = dashboardService.obtenerProductosStockBajo();
        return ResponseEntity.ok(productos);
    }
}
