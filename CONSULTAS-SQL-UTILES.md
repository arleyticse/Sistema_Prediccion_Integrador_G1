# 📊 Consultas SQL Útiles para el Sistema

## Consultas de Inventario

### 1. Productos que necesitan reorden
```sql
SELECT 
    p.id_producto,
    p.nombre,
    i.stock_disponible,
    i.punto_reorden,
    i.stock_minimo,
    (i.punto_reorden - i.stock_disponible) AS unidades_faltantes
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
WHERE i.stock_disponible <= i.punto_reorden
ORDER BY (i.punto_reorden - i.stock_disponible) DESC;
```

### 2. Productos en estado crítico
```sql
SELECT 
    p.id_producto,
    p.nombre,
    c.nombre AS categoria,
    i.stock_disponible,
    i.stock_minimo,
    i.valor_total_stock,
    i.estado
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
INNER JOIN categorias c ON p.id_categoria = c.id_categoria
WHERE i.estado IN ('CRITICO', 'BAJO')
ORDER BY i.stock_disponible ASC;
```

### 3. Top 10 productos con mayor valor en inventario
```sql
SELECT 
    p.nombre,
    i.stock_disponible,
    i.valor_total_stock,
    ROUND(i.rotacion_inventario, 2) AS rotacion
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
ORDER BY i.valor_total_stock DESC
LIMIT 10;
```

### 4. Productos obsoletos (sin movimiento)
```sql
SELECT 
    p.nombre,
    i.stock_disponible,
    i.dias_sin_venta,
    i.valor_total_stock,
    i.fecha_ultimo_movimiento
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
WHERE i.dias_sin_venta > 90
ORDER BY i.dias_sin_venta DESC;
```

---

## Consultas de Kardex

### 5. Reporte de movimientos por producto
```sql
SELECT 
    k.fecha_movimiento,
    k.tipo_movimiento,
    k.tipo_documento,
    k.numero_documento,
    k.cantidad_entrada,
    k.cantidad_salida,
    k.saldo_cantidad,
    k.costo_unitario,
    k.saldo_valorizado,
    k.proveedor,
    k.observaciones
FROM kardex k
WHERE k.id_producto = 1
ORDER BY k.fecha_movimiento DESC;
```

### 6. Movimientos por rango de fechas
```sql
SELECT 
    p.nombre AS producto,
    k.fecha_movimiento,
    k.tipo_movimiento,
    k.cantidad_entrada,
    k.cantidad_salida,
    k.proveedor,
    k.costo_total_entrada,
    k.costo_total_salida
FROM kardex k
INNER JOIN productos p ON k.id_producto = p.id_producto
WHERE k.fecha_movimiento BETWEEN '2024-01-01' AND '2024-12-31'
ORDER BY k.fecha_movimiento DESC;
```

### 7. Resumen de entradas y salidas por mes
```sql
SELECT 
    p.nombre AS producto,
    TO_CHAR(k.fecha_movimiento, 'YYYY-MM') AS mes,
    SUM(k.cantidad_entrada) AS total_entradas,
    SUM(k.cantidad_salida) AS total_salidas,
    SUM(k.cantidad_entrada) - SUM(k.cantidad_salida) AS movimiento_neto,
    SUM(k.costo_total_entrada) AS valor_entradas,
    SUM(k.costo_total_salida) AS valor_salidas
FROM kardex k
INNER JOIN productos p ON k.id_producto = p.id_producto
WHERE k.fecha_movimiento >= DATE_TRUNC('year', CURRENT_DATE)
GROUP BY p.nombre, TO_CHAR(k.fecha_movimiento, 'YYYY-MM')
ORDER BY mes DESC, p.nombre;
```

### 8. Productos más vendidos del mes
```sql
SELECT 
    p.nombre,
    SUM(k.cantidad_salida) AS unidades_vendidas,
    SUM(k.costo_total_salida) AS valor_total
FROM kardex k
INNER JOIN productos p ON k.id_producto = p.id_producto
WHERE k.tipo_movimiento = 'SALIDA_VENTA'
  AND k.fecha_movimiento >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY p.nombre
ORDER BY unidades_vendidas DESC
LIMIT 10;
```

### 9. Análisis de mermas
```sql
SELECT 
    p.nombre,
    SUM(k.cantidad_salida) AS unidades_perdidas,
    SUM(k.costo_total_salida) AS valor_perdido,
    k.motivo
FROM kardex k
INNER JOIN productos p ON k.id_producto = p.id_producto
WHERE k.tipo_movimiento IN ('SALIDA_MERMA', 'SALIDA_VENCIMIENTO')
  AND k.fecha_movimiento >= DATE_TRUNC('year', CURRENT_DATE)
GROUP BY p.nombre, k.motivo
ORDER BY valor_perdido DESC;
```

---

## Consultas de Alertas

### 10. Alertas pendientes ordenadas por criticidad
```sql
SELECT 
    a.id_alerta,
    p.nombre AS producto,
    a.tipo_alerta,
    a.nivel_criticidad,
    a.mensaje,
    a.stock_actual,
    a.cantidad_sugerida,
    a.fecha_generacion,
    EXTRACT(DAY FROM (CURRENT_TIMESTAMP - a.fecha_generacion)) AS dias_pendiente
FROM alertas_inventario a
INNER JOIN productos p ON a.id_producto = p.id_producto
WHERE a.estado IN ('PENDIENTE', 'EN_PROCESO')
ORDER BY 
    CASE a.nivel_criticidad 
        WHEN 'CRITICA' THEN 1
        WHEN 'ALTA' THEN 2
        WHEN 'MEDIA' THEN 3
        ELSE 4
    END,
    a.fecha_generacion ASC;
```

### 11. Resumen de alertas por tipo
```sql
SELECT 
    a.tipo_alerta,
    COUNT(*) AS total,
    COUNT(CASE WHEN a.estado = 'PENDIENTE' THEN 1 END) AS pendientes,
    COUNT(CASE WHEN a.estado = 'RESUELTA' THEN 1 END) AS resueltas,
    AVG(EXTRACT(DAY FROM (a.fecha_resolucion - a.fecha_generacion))) AS dias_promedio_resolucion
FROM alertas_inventario a
WHERE a.fecha_generacion >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY a.tipo_alerta
ORDER BY total DESC;
```

### 12. Productos con más alertas generadas
```sql
SELECT 
    p.nombre,
    COUNT(*) AS total_alertas,
    COUNT(CASE WHEN a.nivel_criticidad = 'CRITICA' THEN 1 END) AS alertas_criticas,
    MAX(a.fecha_generacion) AS ultima_alerta
FROM alertas_inventario a
INNER JOIN productos p ON a.id_producto = p.id_producto
WHERE a.fecha_generacion >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY p.nombre
ORDER BY total_alertas DESC
LIMIT 10;
```

---

## Consultas de Órdenes de Compra

### 13. Órdenes pendientes con retraso
```sql
SELECT 
    oc.numero_orden,
    pr.razon_social AS proveedor,
    oc.fecha_orden,
    oc.fecha_entrega_esperada,
    oc.estado_orden,
    oc.total_orden,
    (CURRENT_DATE - oc.fecha_entrega_esperada) AS dias_retraso
FROM ordenes_compra oc
INNER JOIN proveedores pr ON oc.id_proveedor = pr.id_proveedor
WHERE oc.estado_orden NOT IN ('RECIBIDA_COMPLETA', 'CANCELADA')
  AND oc.fecha_entrega_esperada < CURRENT_DATE
ORDER BY dias_retraso DESC;
```

### 14. Detalle de orden de compra
```sql
SELECT 
    oc.numero_orden,
    oc.fecha_orden,
    pr.razon_social AS proveedor,
    p.nombre AS producto,
    doc.cantidad_solicitada,
    doc.cantidad_recibida,
    doc.precio_unitario,
    doc.subtotal,
    (doc.cantidad_solicitada - doc.cantidad_recibida) AS pendiente_recibir
FROM ordenes_compra oc
INNER JOIN proveedores pr ON oc.id_proveedor = pr.id_proveedor
INNER JOIN detalle_orden_compra doc ON oc.id_orden_compra = doc.id_orden_compra
INNER JOIN productos p ON doc.id_producto = p.id_producto
WHERE oc.numero_orden = 'OC-2024-001';
```

### 15. Resumen de compras por proveedor
```sql
SELECT 
    pr.razon_social,
    COUNT(oc.id_orden_compra) AS total_ordenes,
    SUM(oc.total_orden) AS monto_total,
    AVG(oc.fecha_entrega_real - oc.fecha_entrega_esperada) AS promedio_retraso_dias,
    pr.calificacion
FROM ordenes_compra oc
INNER JOIN proveedores pr ON oc.id_proveedor = pr.id_proveedor
WHERE oc.fecha_orden >= DATE_TRUNC('year', CURRENT_DATE)
GROUP BY pr.razon_social, pr.calificacion
ORDER BY monto_total DESC;
```

---

## Consultas de Análisis y Reportes

### 16. Rotación de inventario por categoría
```sql
SELECT 
    c.nombre AS categoria,
    COUNT(DISTINCT p.id_producto) AS num_productos,
    SUM(i.stock_disponible) AS stock_total,
    AVG(i.rotacion_inventario) AS rotacion_promedio,
    SUM(i.valor_total_stock) AS valor_total_inventario
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
INNER JOIN categorias c ON p.id_categoria = c.id_categoria
GROUP BY c.nombre
ORDER BY valor_total_inventario DESC;
```

### 17. Predicción vs Real (últimos 3 meses)
```sql
SELECT 
    p.nombre,
    pred.fecha_prediccion,
    pred.cantidad_predicha,
    SUM(k.cantidad_salida) AS ventas_reales,
    pred.cantidad_predicha - SUM(k.cantidad_salida) AS diferencia,
    ROUND(
        (ABS(pred.cantidad_predicha - SUM(k.cantidad_salida))::DECIMAL / 
         NULLIF(SUM(k.cantidad_salida), 0)) * 100, 2
    ) AS error_porcentaje
FROM predicciones pred
INNER JOIN productos p ON pred.id_producto = p.id_producto
LEFT JOIN kardex k ON k.id_producto = pred.id_producto
    AND k.tipo_movimiento = 'SALIDA_VENTA'
    AND DATE_TRUNC('month', k.fecha_movimiento) = DATE_TRUNC('month', pred.fecha_prediccion)
WHERE pred.fecha_prediccion >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '3 months')
GROUP BY p.nombre, pred.fecha_prediccion, pred.cantidad_predicha
ORDER BY pred.fecha_prediccion DESC;
```

### 18. Análisis de estacionalidad
```sql
SELECT 
    p.nombre,
    ep.mes,
    ep.factor_estacional,
    ep.demanda_promedio_historica,
    ep.descripcion_temporada,
    CASE 
        WHEN ep.factor_estacional > 1.2 THEN 'Temporada Alta'
        WHEN ep.factor_estacional < 0.8 THEN 'Temporada Baja'
        ELSE 'Normal'
    END AS tipo_temporada
FROM estacionalidad_producto ep
INNER JOIN productos p ON ep.id_producto = p.id_producto
ORDER BY ep.mes, ep.factor_estacional DESC;
```

### 19. Eficiencia de importaciones
```sql
SELECT 
    imp.tipo_datos,
    COUNT(*) AS total_importaciones,
    SUM(imp.registros_procesados) AS total_registros,
    SUM(imp.registros_exitosos) AS total_exitosos,
    SUM(imp.registros_fallidos) AS total_fallidos,
    ROUND(
        (SUM(imp.registros_exitosos)::DECIMAL / 
         NULLIF(SUM(imp.registros_procesados), 0)) * 100, 2
    ) AS tasa_exito,
    AVG(imp.tiempo_procesamiento_ms) AS tiempo_promedio_ms
FROM importaciones_datos imp
WHERE imp.fecha_importacion >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY imp.tipo_datos
ORDER BY total_importaciones DESC;
```

### 20. Dashboard ejecutivo (resumen general)
```sql
SELECT 
    'Total Productos' AS metrica,
    COUNT(*)::TEXT AS valor
FROM productos
UNION ALL
SELECT 
    'Valor Total Inventario',
    TO_CHAR(SUM(valor_total_stock), 'FM$999,999,999.00')
FROM inventario
UNION ALL
SELECT 
    'Productos Críticos',
    COUNT(*)::TEXT
FROM inventario
WHERE estado = 'CRITICO'
UNION ALL
SELECT 
    'Alertas Pendientes',
    COUNT(*)::TEXT
FROM alertas_inventario
WHERE estado IN ('PENDIENTE', 'EN_PROCESO')
UNION ALL
SELECT 
    'Órdenes Activas',
    COUNT(*)::TEXT
FROM ordenes_compra
WHERE estado_orden NOT IN ('RECIBIDA_COMPLETA', 'CANCELADA')
UNION ALL
SELECT 
    'Ventas del Mes',
    TO_CHAR(SUM(costo_total_salida), 'FM$999,999,999.00')
FROM kardex
WHERE tipo_movimiento = 'SALIDA_VENTA'
  AND fecha_movimiento >= DATE_TRUNC('month', CURRENT_DATE);
```

---

## Consultas Avanzadas con Funciones Window

### 21. Ranking de productos por ventas con tendencia
```sql
WITH ventas_mensuales AS (
    SELECT 
        p.id_producto,
        p.nombre,
        DATE_TRUNC('month', k.fecha_movimiento) AS mes,
        SUM(k.cantidad_salida) AS unidades_vendidas,
        SUM(k.costo_total_salida) AS valor_vendido
    FROM kardex k
    INNER JOIN productos p ON k.id_producto = p.id_producto
    WHERE k.tipo_movimiento = 'SALIDA_VENTA'
      AND k.fecha_movimiento >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '6 months')
    GROUP BY p.id_producto, p.nombre, DATE_TRUNC('month', k.fecha_movimiento)
)
SELECT 
    nombre,
    mes,
    unidades_vendidas,
    valor_vendido,
    RANK() OVER (PARTITION BY mes ORDER BY unidades_vendidas DESC) AS ranking,
    LAG(unidades_vendidas) OVER (PARTITION BY id_producto ORDER BY mes) AS ventas_mes_anterior,
    ROUND(
        ((unidades_vendidas - LAG(unidades_vendidas) OVER (PARTITION BY id_producto ORDER BY mes))::DECIMAL /
         NULLIF(LAG(unidades_vendidas) OVER (PARTITION BY id_producto ORDER BY mes), 0)) * 100, 2
    ) AS variacion_porcentual
FROM ventas_mensuales
ORDER BY mes DESC, ranking;
```

### 22. Análisis ABC de inventario
```sql
WITH productos_valorizados AS (
    SELECT 
        p.id_producto,
        p.nombre,
        i.valor_total_stock,
        SUM(i.valor_total_stock) OVER () AS valor_total_inventario
    FROM inventario i
    INNER JOIN productos p ON i.id_producto = p.id_producto
),
productos_con_porcentaje AS (
    SELECT 
        id_producto,
        nombre,
        valor_total_stock,
        (valor_total_stock / valor_total_inventario * 100) AS porcentaje_valor,
        SUM(valor_total_stock / valor_total_inventario * 100) OVER (ORDER BY valor_total_stock DESC) AS porcentaje_acumulado
    FROM productos_valorizados
)
SELECT 
    nombre,
    valor_total_stock,
    ROUND(porcentaje_valor, 2) AS porcentaje_valor,
    ROUND(porcentaje_acumulado, 2) AS porcentaje_acumulado,
    CASE 
        WHEN porcentaje_acumulado <= 80 THEN 'A - Alta Rotación'
        WHEN porcentaje_acumulado <= 95 THEN 'B - Rotación Media'
        ELSE 'C - Baja Rotación'
    END AS clasificacion_abc
FROM productos_con_porcentaje
ORDER BY valor_total_stock DESC;
```

---

## Procedimientos Almacenados Útiles

### 23. Procedimiento para registrar venta completa
```sql
CREATE OR REPLACE PROCEDURE registrar_venta(
    p_producto_id INTEGER,
    p_cantidad INTEGER,
    p_costo_unitario DECIMAL,
    p_numero_documento VARCHAR,
    p_usuario_id INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_saldo_anterior INTEGER;
    v_inventario_id INTEGER;
BEGIN
    -- Obtener saldo anterior del Kardex
    SELECT COALESCE(k.saldo_cantidad, 0)
    INTO v_saldo_anterior
    FROM kardex k
    WHERE k.id_producto = p_producto_id
    ORDER BY k.fecha_movimiento DESC
    LIMIT 1;
    
    -- Insertar en Kardex
    INSERT INTO kardex (
        id_producto, fecha_movimiento, tipo_movimiento,
        cantidad_salida, saldo_cantidad, costo_unitario,
        costo_total_salida, numero_documento, id_usuario
    ) VALUES (
        p_producto_id, CURRENT_TIMESTAMP, 'SALIDA_VENTA',
        p_cantidad, v_saldo_anterior - p_cantidad, p_costo_unitario,
        p_cantidad * p_costo_unitario, p_numero_documento, p_usuario_id
    );
    
    -- Actualizar inventario
    UPDATE inventario
    SET stock_disponible = stock_disponible - p_cantidad,
        fecha_ultimo_movimiento = CURRENT_TIMESTAMP,
        dias_sin_venta = 0
    WHERE id_producto = p_producto_id;
    
    -- Verificar alertas
    SELECT id_inventario INTO v_inventario_id
    FROM inventario
    WHERE id_producto = p_producto_id;
    
    -- Generar alerta si es necesario
    IF (SELECT stock_disponible FROM inventario WHERE id_producto = p_producto_id) <= 
       (SELECT punto_reorden FROM inventario WHERE id_producto = p_producto_id) THEN
        INSERT INTO alertas_inventario (
            id_producto, tipo_alerta, nivel_criticidad, mensaje, stock_actual
        )
        SELECT 
            p_producto_id,
            'PUNTO_REORDEN',
            'MEDIA',
            'Stock alcanzó punto de reorden después de venta',
            stock_disponible
        FROM inventario
        WHERE id_producto = p_producto_id;
    END IF;
    
    COMMIT;
END;
$$;

-- Uso:
-- CALL registrar_venta(1, 50, 15.50, 'V001-001', 1);
```

---

## Consultas para Optimización

### 24. Identificar índices faltantes
```sql
SELECT 
    schemaname,
    tablename,
    attname AS column_name,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname = 'public'
  AND tablename IN ('kardex', 'inventario', 'alertas_inventario')
  AND n_distinct > 100
ORDER BY tablename, n_distinct DESC;
```

### 25. Análisis de performance de consultas
```sql
-- Activar estadísticas (ejecutar una sola vez)
-- CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    max_exec_time
FROM pg_stat_statements
WHERE query LIKE '%kardex%' OR query LIKE '%inventario%'
ORDER BY total_exec_time DESC
LIMIT 10;
```

---

**Estas consultas te ayudarán a:**
- 📊 Monitorear el estado del inventario
- 📈 Generar reportes ejecutivos
- 🔍 Analizar tendencias de ventas
- ⚠️ Gestionar alertas eficientemente
- 📦 Optimizar órdenes de compra
- 💰 Calcular costos y valorización
