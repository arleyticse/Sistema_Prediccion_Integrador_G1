package com.prediccion.apppredicciongm.gestion_dashboard.service;

import com.prediccion.apppredicciongm.gestion_dashboard.dto.*;
import com.prediccion.apppredicciongm.gestion_dashboard.repository.IDashboardRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para obtener datos del dashboard del gerente.
 * 
 * Implementa logica de negocio para agregar y transformar datos
 * de multiples fuentes en metricas utiles para la toma de decisiones.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int DIAS_TENDENCIA = 30;
    private static final int LIMITE_TOP_PRODUCTOS = 10;
    private static final int LIMITE_PRODUCTOS_STOCK_BAJO = 15;
    private static final int LIMITE_CATEGORIAS = 10;

    private final IDashboardRepositorio dashboardRepositorio;
    private final IProductoRepositorio productoRepositorio;

    public DashboardService(IDashboardRepositorio dashboardRepositorio, 
                           IProductoRepositorio productoRepositorio) {
        this.dashboardRepositorio = dashboardRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    /**
     * Obtiene el dashboard completo con todas las metricas y graficos.
     */
    public DashboardCompletoDTO obtenerDashboardCompleto() {
        log.info("Generando dashboard completo para el gerente");
        
        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(DIAS_TENDENCIA);
        
        return new DashboardCompletoDTO(
            obtenerEstadisticas(),
            obtenerDistribucionInventario(),
            obtenerTendenciaMovimientos(fechaDesde),
            obtenerProductosMasVendidos(fechaDesde),
            obtenerProductosStockBajo(),
            obtenerDistribucionCategorias(),
            obtenerDistribucionAlertas(),
            obtenerResumenMovimientos(fechaDesde)
        );
    }

    /**
     * Obtiene estadisticas generales del negocio.
     */
    public DashboardEstadisticasDTO obtenerEstadisticas() {
        log.debug("Calculando estadisticas generales");
        
        Long totalProductos = productoRepositorio.count();
        Long productosActivos = totalProductos;
        Long productosStockBajo = dashboardRepositorio.countByEstadoInventario("BAJO");
        Long productosStockCritico = dashboardRepositorio.countByEstadoInventario("CRITICO");
        Long productosExceso = dashboardRepositorio.countByEstadoInventario("EXCESO");
        Long alertasPendientes = dashboardRepositorio.countAlertasPendientes();
        Long alertasCriticas = dashboardRepositorio.countAlertasCriticas();
        Long proveedoresActivos = dashboardRepositorio.countProveedoresActivos();
        Long ordenesPendientes = dashboardRepositorio.countOrdenesPorEstado("PENDIENTE");
        Long ordenesBorrador = dashboardRepositorio.countOrdenesPorEstado("BORRADOR");
        
        Double valorInventario = dashboardRepositorio.calcularValorTotalInventario();
        BigDecimal valorInventarioDecimal = valorInventario != null 
            ? BigDecimal.valueOf(valorInventario).setScale(2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
        
        Long stockTotalUnidades = dashboardRepositorio.sumStockTotalDisponible();

        return new DashboardEstadisticasDTO(
            totalProductos,
            productosActivos,
            productosStockBajo != null ? productosStockBajo : 0L,
            productosStockCritico != null ? productosStockCritico : 0L,
            productosExceso != null ? productosExceso : 0L,
            alertasPendientes != null ? alertasPendientes : 0L,
            alertasCriticas != null ? alertasCriticas : 0L,
            proveedoresActivos != null ? proveedoresActivos : 0L,
            ordenesPendientes != null ? ordenesPendientes : 0L,
            ordenesBorrador != null ? ordenesBorrador : 0L,
            valorInventarioDecimal,
            stockTotalUnidades != null ? stockTotalUnidades : 0L
        );
    }

    /**
     * Obtiene la distribucion del inventario por estado.
     */
    public List<DistribucionInventarioDTO> obtenerDistribucionInventario() {
        log.debug("Obteniendo distribucion de inventario por estado");
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findDistribucionInventarioPorEstado();
            return resultados.stream()
                .map(row -> new DistribucionInventarioDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener distribucion de inventario", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene la tendencia de movimientos de los ultimos dias.
     */
    public List<TendenciaMovimientosDTO> obtenerTendenciaMovimientos(LocalDateTime fechaDesde) {
        log.debug("Obteniendo tendencia de movimientos desde {}", fechaDesde);
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findTendenciaMovimientos(fechaDesde);
            return resultados.stream()
                .map(row -> new TendenciaMovimientosDTO(
                    convertirALocalDate(row[0]),
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener tendencia de movimientos", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene los productos mas vendidos del periodo.
     */
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(LocalDateTime fechaDesde) {
        log.debug("Obteniendo top {} productos mas vendidos", LIMITE_TOP_PRODUCTOS);
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findProductosMasVendidos(
                fechaDesde, LIMITE_TOP_PRODUCTOS);
            return resultados.stream()
                .map(row -> new ProductoMasVendidoDTO(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener productos mas vendidos", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene productos con stock bajo.
     */
    public List<ProductoStockBajoDTO> obtenerProductosStockBajo() {
        log.debug("Obteniendo productos con stock bajo");
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findProductosStockBajo(LIMITE_PRODUCTOS_STOCK_BAJO);
            return resultados.stream()
                .map(row -> new ProductoStockBajoDTO(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    row[2] != null ? ((Number) row[2]).intValue() : 0,
                    row[3] != null ? ((Number) row[3]).intValue() : 0,
                    row[4] != null ? ((Number) row[4]).intValue() : 0,
                    (String) row[5],
                    (String) row[6]
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener productos con stock bajo", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene la distribucion de productos por categoria.
     */
    public List<DistribucionCategoriaDTO> obtenerDistribucionCategorias() {
        log.debug("Obteniendo distribucion por categorias");
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findDistribucionCategorias(LIMITE_CATEGORIAS);
            return resultados.stream()
                .map(row -> new DistribucionCategoriaDTO(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener distribucion por categorias", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene la distribucion de alertas pendientes por tipo.
     */
    public List<DistribucionAlertasDTO> obtenerDistribucionAlertas() {
        log.debug("Obteniendo distribucion de alertas por tipo");
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findDistribucionAlertasPorTipo();
            return resultados.stream()
                .map(row -> new DistribucionAlertasDTO(
                    (String) row[0],
                    ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener distribucion de alertas", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene resumen de movimientos del periodo.
     */
    public ResumenMovimientosDTO obtenerResumenMovimientos(LocalDateTime fechaDesde) {
        log.debug("Obteniendo resumen de movimientos desde {}", fechaDesde);
        
        try {
            List<Object[]> resultados = dashboardRepositorio.findResumenMovimientos(fechaDesde);
            if (!resultados.isEmpty()) {
                Object[] row = resultados.get(0);
                return new ResumenMovimientosDTO(
                    ((Number) row[0]).longValue(),
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue(),
                    ((Number) row[5]).longValue()
                );
            }
            return new ResumenMovimientosDTO(0L, 0L, 0L, 0L, 0L, 0L);
        } catch (Exception e) {
            log.error("Error al obtener resumen de movimientos", e);
            return new ResumenMovimientosDTO(0L, 0L, 0L, 0L, 0L, 0L);
        }
    }

    /**
     * Convierte un objeto de fecha de la base de datos a LocalDate.
     */
    private LocalDate convertirALocalDate(Object fechaObj) {
        if (fechaObj instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (fechaObj instanceof java.time.LocalDate ld) {
            return ld;
        }
        if (fechaObj instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        return LocalDate.now();
    }
}
