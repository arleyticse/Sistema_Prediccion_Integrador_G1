package com.prediccion.apppredicciongm.gestion_dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio especializado para consultas del dashboard.
 * 
 * Proporciona consultas optimizadas que agregan datos de multiples tablas
 * para alimentar los graficos y metricas del dashboard del gerente.
 */
@Repository
public interface IDashboardRepositorio extends JpaRepository<Inventario, Integer> {

    /**
     * Obtiene la distribucion del inventario agrupado por estado.
     * Retorna: [estado, cantidad]
     */
    @Query(value = """
        SELECT estado, COUNT(*) as cantidad
        FROM inventario
        GROUP BY estado
        ORDER BY cantidad DESC
        """, nativeQuery = true)
    List<Object[]> findDistribucionInventarioPorEstado();

    /**
     * Obtiene los productos mas vendidos en los ultimos N dias.
     * Retorna: [producto_id, nombre, cantidad_vendida]
     */
    @Query(value = """
        SELECT p.id_producto, p.nombre, COALESCE(SUM(ABS(k.cantidad)), 0) as total_vendido
        FROM kardex k
        JOIN productos p ON k.id_producto = p.id_producto
        WHERE k.tipo_movimiento = 'SALIDA_VENTA'
            AND k.anulado = false
            AND k.fecha_movimiento >= :fechaDesde
        GROUP BY p.id_producto, p.nombre
        ORDER BY total_vendido DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findProductosMasVendidos(
        @Param("fechaDesde") LocalDateTime fechaDesde,
        @Param("limite") int limite
    );

    /**
     * Obtiene la tendencia de movimientos (entradas y salidas) agrupados por dia.
     * Retorna: [fecha, entradas, salidas]
     */
    @Query(value = """
        SELECT 
            DATE(fecha_movimiento) as fecha,
            SUM(CASE WHEN tipo_movimiento LIKE 'ENTRADA%' THEN ABS(cantidad) ELSE 0 END) as entradas,
            SUM(CASE WHEN tipo_movimiento LIKE 'SALIDA%' THEN ABS(cantidad) ELSE 0 END) as salidas
        FROM kardex
        WHERE fecha_movimiento >= :fechaDesde
            AND anulado = false
        GROUP BY DATE(fecha_movimiento)
        ORDER BY fecha
        """, nativeQuery = true)
    List<Object[]> findTendenciaMovimientos(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Obtiene productos con stock bajo (debajo del stock minimo).
     * Retorna: [producto_id, nombre, stock_disponible, stock_minimo, punto_reorden, estado, categoria_nombre]
     */
    @Query(value = """
        SELECT 
            p.id_producto,
            p.nombre,
            i.stock_disponible,
            i.stock_minimo,
            i.punto_reorden,
            i.estado,
            c.nombre as categoria_nombre
        FROM inventario i
        JOIN productos p ON i.id_producto = p.id_producto
        JOIN categorias c ON p.id_categoria = c.id_categoria
        WHERE i.estado IN ('BAJO', 'CRITICO')
            OR i.stock_disponible <= i.stock_minimo
        ORDER BY i.stock_disponible ASC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findProductosStockBajo(@Param("limite") int limite);

    /**
     * Obtiene la distribucion de productos por categoria.
     * Retorna: [categoria_id, nombre, cantidad_productos]
     */
    @Query(value = """
        SELECT c.id_categoria, c.nombre, COUNT(p.id_producto) as cantidad
        FROM productos p
        JOIN categorias c ON p.id_categoria = c.id_categoria
        GROUP BY c.id_categoria, c.nombre
        ORDER BY cantidad DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findDistribucionCategorias(@Param("limite") int limite);

    /**
     * Obtiene la distribucion de alertas pendientes por tipo.
     * Retorna: [tipo_alerta, cantidad]
     */
    @Query(value = """
        SELECT tipo_alerta, COUNT(*) as cantidad
        FROM alertas_inventario
        WHERE estado = 'PENDIENTE'
        GROUP BY tipo_alerta
        ORDER BY cantidad DESC
        """, nativeQuery = true)
    List<Object[]> findDistribucionAlertasPorTipo();

    /**
     * Obtiene resumen de movimientos del periodo.
     * Retorna: [total_entradas, total_salidas, total_mermas, cant_entradas, cant_salidas, cant_mermas]
     */
    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN tipo_movimiento LIKE 'ENTRADA%' THEN ABS(cantidad) ELSE 0 END), 0) as total_entradas,
            COALESCE(SUM(CASE WHEN tipo_movimiento LIKE 'SALIDA%' AND tipo_movimiento != 'SALIDA_MERMA' THEN ABS(cantidad) ELSE 0 END), 0) as total_salidas,
            COALESCE(SUM(CASE WHEN tipo_movimiento = 'SALIDA_MERMA' THEN ABS(cantidad) ELSE 0 END), 0) as total_mermas,
            COUNT(CASE WHEN tipo_movimiento LIKE 'ENTRADA%' THEN 1 END) as cant_entradas,
            COUNT(CASE WHEN tipo_movimiento LIKE 'SALIDA%' AND tipo_movimiento != 'SALIDA_MERMA' THEN 1 END) as cant_salidas,
            COUNT(CASE WHEN tipo_movimiento = 'SALIDA_MERMA' THEN 1 END) as cant_mermas
        FROM kardex
        WHERE fecha_movimiento >= :fechaDesde
            AND anulado = false
        """, nativeQuery = true)
    List<Object[]> findResumenMovimientos(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Cuenta productos por estado de inventario especifico.
     */
    @Query(value = "SELECT COUNT(*) FROM inventario WHERE estado = :estado", nativeQuery = true)
    Long countByEstadoInventario(@Param("estado") String estado);

    /**
     * Calcula el valor total del inventario.
     */
    @Query(value = """
        SELECT COALESCE(SUM(i.stock_disponible * p.costo_adquisicion), 0)
        FROM inventario i
        JOIN productos p ON i.id_producto = p.id_producto
        """, nativeQuery = true)
    Double calcularValorTotalInventario();

    /**
     * Suma el stock total disponible.
     */
    @Query(value = "SELECT COALESCE(SUM(stock_disponible), 0) FROM inventario", nativeQuery = true)
    Long sumStockTotalDisponible();

    /**
     * Cuenta proveedores activos.
     */
    @Query(value = "SELECT COUNT(*) FROM proveedores WHERE estado = true", nativeQuery = true)
    Long countProveedoresActivos();

    /**
     * Cuenta ordenes de compra por estado.
     */
    @Query(value = "SELECT COUNT(*) FROM ordenes_compra WHERE estado_orden = :estado", nativeQuery = true)
    Long countOrdenesPorEstado(@Param("estado") String estado);

    /**
     * Cuenta alertas pendientes.
     */
    @Query(value = "SELECT COUNT(*) FROM alertas_inventario WHERE estado = 'PENDIENTE'", nativeQuery = true)
    Long countAlertasPendientes();

    /**
     * Cuenta alertas criticas pendientes.
     */
    @Query(value = "SELECT COUNT(*) FROM alertas_inventario WHERE estado = 'PENDIENTE' AND nivel_criticidad = 'CRITICA'", nativeQuery = true)
    Long countAlertasCriticas();
}
