-- ============================================================================
-- SCRIPT SQL PARA BASE DE DATOS DE PREDICCIÓN Y OPTIMIZACIÓN DE INVENTARIO
-- Sistema completo con Inventario, Kardex/Cardex, Alertas y Órdenes de Compra
-- PostgreSQL
-- ============================================================================

-- Nota: Ejecutar después de tener las tablas básicas creadas por JPA
-- Este script complementa la estructura generada automáticamente

-- ============================================================================
-- 1. TABLA INVENTARIO
-- ============================================================================
CREATE TABLE IF NOT EXISTS inventario (
    id_inventario SERIAL PRIMARY KEY,
    id_producto INTEGER NOT NULL UNIQUE,
    stock_disponible INTEGER NOT NULL DEFAULT 0,
    stock_reservado INTEGER DEFAULT 0,
    stock_en_transito INTEGER DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    stock_maximo INTEGER,
    punto_reorden INTEGER,
    ubicacion_almacen VARCHAR(100),
    lote_actual VARCHAR(50),
    fecha_ultimo_movimiento TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP,
    valor_total_stock DECIMAL(12,2),
    dias_sin_venta INTEGER,
    rotacion_inventario DECIMAL(8,2),
    estado VARCHAR(20) DEFAULT 'NORMAL',
    observaciones VARCHAR(500),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) 
        REFERENCES productos(id_producto) ON DELETE CASCADE,
    CONSTRAINT chk_stock_positivo CHECK (stock_disponible >= 0),
    CONSTRAINT chk_estado_valido CHECK (estado IN 
        ('NORMAL', 'BAJO', 'CRITICO', 'EXCESO', 'OBSOLETO', 'BLOQUEADO'))
);

-- Índices para Inventario
CREATE INDEX idx_inventario_estado ON inventario(estado);
CREATE INDEX idx_inventario_producto ON inventario(id_producto);
CREATE INDEX idx_inventario_stock_bajo ON inventario(stock_disponible) 
    WHERE stock_disponible < punto_reorden;

-- ============================================================================
-- 2. TABLA KARDEX (CARDEX)
-- ============================================================================
CREATE TABLE IF NOT EXISTS kardex (
    id_kardex BIGSERIAL PRIMARY KEY,
    id_producto INTEGER NOT NULL,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(50) NOT NULL,
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(50),
    cantidad_entrada INTEGER DEFAULT 0,
    cantidad_salida INTEGER DEFAULT 0,
    saldo_cantidad INTEGER NOT NULL,
    costo_unitario DECIMAL(10,2),
    costo_total_entrada DECIMAL(12,2),
    costo_total_salida DECIMAL(12,2),
    saldo_valorizado DECIMAL(12,2),
    lote VARCHAR(50),
    fecha_vencimiento TIMESTAMP,
    proveedor VARCHAR(200),
    cliente VARCHAR(200),
    motivo VARCHAR(200),
    referencia VARCHAR(100),
    id_usuario INTEGER,
    observaciones VARCHAR(500),
    ubicacion VARCHAR(100),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kardex_producto FOREIGN KEY (id_producto) 
        REFERENCES productos(id_producto) ON DELETE CASCADE,
    CONSTRAINT fk_kardex_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_cantidad_valida CHECK (
        (cantidad_entrada > 0 AND cantidad_salida = 0) OR 
        (cantidad_salida > 0 AND cantidad_entrada = 0)
    )
);

-- Índices para Kardex
CREATE INDEX idx_kardex_producto ON kardex(id_producto);
CREATE INDEX idx_kardex_fecha_movimiento ON kardex(fecha_movimiento DESC);
CREATE INDEX idx_kardex_tipo_movimiento ON kardex(tipo_movimiento);
CREATE INDEX idx_kardex_lote ON kardex(lote);
CREATE INDEX idx_kardex_documento ON kardex(tipo_documento, numero_documento);

-- ============================================================================
-- 3. TABLA PROVEEDORES
-- ============================================================================
CREATE TABLE IF NOT EXISTS proveedores (
    id_proveedor SERIAL PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    ruc_nit VARCHAR(20) UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(300),
    ciudad VARCHAR(100),
    pais VARCHAR(100) DEFAULT 'Bolivia',
    persona_contacto VARCHAR(150),
    tiempo_entrega_dias INTEGER DEFAULT 7,
    dias_credito INTEGER DEFAULT 0,
    calificacion DECIMAL(3,2) DEFAULT 0.00,
    estado BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones VARCHAR(500),
    CONSTRAINT chk_calificacion_valida CHECK (calificacion >= 0 AND calificacion <= 5)
);

-- Índices para Proveedores
CREATE INDEX idx_proveedores_estado ON proveedores(estado);
CREATE INDEX idx_proveedores_calificacion ON proveedores(calificacion DESC);

-- ============================================================================
-- 4. TABLA ÓRDENES DE COMPRA
-- ============================================================================
CREATE TABLE IF NOT EXISTS ordenes_compra (
    id_orden_compra BIGSERIAL PRIMARY KEY,
    numero_orden VARCHAR(50) UNIQUE NOT NULL,
    id_proveedor INTEGER,
    fecha_orden DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_entrega_esperada DATE,
    fecha_entrega_real DATE,
    estado_orden VARCHAR(30) DEFAULT 'PENDIENTE',
    total_orden DECIMAL(12,2),
    generada_automaticamente BOOLEAN DEFAULT FALSE,
    id_usuario INTEGER,
    observaciones VARCHAR(500),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orden_proveedor FOREIGN KEY (id_proveedor) 
        REFERENCES proveedores(id_proveedor) ON DELETE SET NULL,
    CONSTRAINT fk_orden_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_estado_orden CHECK (estado_orden IN 
        ('BORRADOR', 'PENDIENTE', 'APROBADA', 'ENVIADA', 'EN_TRANSITO', 
         'RECIBIDA_PARCIAL', 'RECIBIDA_COMPLETA', 'CANCELADA', 'RECHAZADA'))
);

-- Índices para Órdenes de Compra
CREATE INDEX idx_orden_proveedor ON ordenes_compra(id_proveedor);
CREATE INDEX idx_orden_estado ON ordenes_compra(estado_orden);
CREATE INDEX idx_orden_fecha ON ordenes_compra(fecha_orden DESC);

-- ============================================================================
-- 5. TABLA DETALLE ÓRDENES DE COMPRA
-- ============================================================================
CREATE TABLE IF NOT EXISTS detalle_orden_compra (
    id_detalle BIGSERIAL PRIMARY KEY,
    id_orden_compra BIGINT NOT NULL,
    id_producto INTEGER NOT NULL,
    cantidad_solicitada INTEGER NOT NULL,
    cantidad_recibida INTEGER DEFAULT 0,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2),
    observaciones VARCHAR(300),
    CONSTRAINT fk_detalle_orden FOREIGN KEY (id_orden_compra) 
        REFERENCES ordenes_compra(id_orden_compra) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) 
        REFERENCES productos(id_producto) ON DELETE CASCADE,
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad_solicitada > 0),
    CONSTRAINT chk_precio_positivo CHECK (precio_unitario > 0)
);

-- Índices para Detalle Orden
CREATE INDEX idx_detalle_orden ON detalle_orden_compra(id_orden_compra);
CREATE INDEX idx_detalle_producto ON detalle_orden_compra(id_producto);

-- ============================================================================
-- 6. TABLA ALERTAS DE INVENTARIO
-- ============================================================================
CREATE TABLE IF NOT EXISTS alertas_inventario (
    id_alerta BIGSERIAL PRIMARY KEY,
    id_producto INTEGER,
    tipo_alerta VARCHAR(50) NOT NULL,
    nivel_criticidad VARCHAR(20) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    stock_actual INTEGER,
    stock_minimo INTEGER,
    cantidad_sugerida INTEGER,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    id_usuario_asignado INTEGER,
    accion_tomada VARCHAR(500),
    observaciones VARCHAR(500),
    CONSTRAINT fk_alerta_producto FOREIGN KEY (id_producto) 
        REFERENCES productos(id_producto) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (id_usuario_asignado) 
        REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_nivel_criticidad CHECK (nivel_criticidad IN 
        ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT chk_estado_alerta CHECK (estado IN 
        ('PENDIENTE', 'EN_PROCESO', 'RESUELTA', 'IGNORADA', 'ESCALADA'))
);

-- Índices para Alertas
CREATE INDEX idx_alerta_estado ON alertas_inventario(estado);
CREATE INDEX idx_alerta_criticidad ON alertas_inventario(nivel_criticidad);
CREATE INDEX idx_alerta_fecha ON alertas_inventario(fecha_generacion DESC);
CREATE INDEX idx_alerta_producto ON alertas_inventario(id_producto);
CREATE INDEX idx_alerta_tipo ON alertas_inventario(tipo_alerta);

-- ============================================================================
-- 7. TABLA ESTACIONALIDAD DE PRODUCTOS
-- ============================================================================
CREATE TABLE IF NOT EXISTS estacionalidad_producto (
    id_estacionalidad BIGSERIAL PRIMARY KEY,
    id_producto INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    factor_estacional DECIMAL(5,2) DEFAULT 1.00,
    demanda_promedio_historica INTEGER,
    demanda_maxima INTEGER,
    demanda_minima INTEGER,
    anio_referencia INTEGER,
    descripcion_temporada VARCHAR(100),
    observaciones VARCHAR(300),
    CONSTRAINT fk_estacionalidad_producto FOREIGN KEY (id_producto) 
        REFERENCES productos(id_producto) ON DELETE CASCADE,
    CONSTRAINT chk_mes_valido CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_factor_positivo CHECK (factor_estacional > 0),
    CONSTRAINT uk_producto_mes UNIQUE (id_producto, mes)
);

-- Índices para Estacionalidad
CREATE INDEX idx_estacionalidad_producto ON estacionalidad_producto(id_producto);
CREATE INDEX idx_estacionalidad_mes ON estacionalidad_producto(mes);

-- ============================================================================
-- 8. TABLA IMPORTACIONES DE DATOS
-- ============================================================================
CREATE TABLE IF NOT EXISTS importaciones_datos (
    id_importacion BIGSERIAL PRIMARY KEY,
    tipo_datos VARCHAR(50) NOT NULL,
    nombre_archivo VARCHAR(300),
    ruta_archivo VARCHAR(500),
    fecha_importacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registros_procesados INTEGER DEFAULT 0,
    registros_exitosos INTEGER DEFAULT 0,
    registros_fallidos INTEGER DEFAULT 0,
    estado_importacion VARCHAR(30) DEFAULT 'EN_PROCESO',
    tiempo_procesamiento_ms BIGINT,
    id_usuario INTEGER,
    errores TEXT,
    observaciones VARCHAR(500),
    CONSTRAINT fk_importacion_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_estado_importacion CHECK (estado_importacion IN 
        ('EN_PROCESO', 'COMPLETADA', 'COMPLETADA_CON_ERRORES', 'FALLIDA', 'CANCELADA'))
);

-- Índices para Importaciones
CREATE INDEX idx_importacion_fecha ON importaciones_datos(fecha_importacion DESC);
CREATE INDEX idx_importacion_tipo ON importaciones_datos(tipo_datos);
CREATE INDEX idx_importacion_estado ON importaciones_datos(estado_importacion);

-- ============================================================================
-- 9. VISTAS ÚTILES
-- ============================================================================

-- Vista: Resumen de Inventario con Alertas
CREATE OR REPLACE VIEW v_resumen_inventario AS
SELECT 
    p.id_producto,
    p.nombre AS producto,
    c.nombre AS categoria,
    i.stock_disponible,
    i.stock_reservado,
    i.stock_en_transito,
    i.stock_minimo,
    i.punto_reorden,
    i.estado,
    i.valor_total_stock,
    i.rotacion_inventario,
    i.dias_sin_venta,
    CASE 
        WHEN i.stock_disponible <= i.punto_reorden THEN 'REQUIERE REORDEN'
        WHEN i.stock_disponible < i.stock_minimo THEN 'CRÍTICO'
        ELSE 'NORMAL'
    END AS estado_recomendado,
    (i.stock_disponible + i.stock_reservado + i.stock_en_transito) AS stock_total
FROM inventario i
INNER JOIN productos p ON i.id_producto = p.id_producto
INNER JOIN categorias c ON p.id_categoria = c.id_categoria;

-- Vista: Kardex Resumido por Producto
CREATE OR REPLACE VIEW v_kardex_resumen AS
SELECT 
    p.id_producto,
    p.nombre AS producto,
    DATE_TRUNC('month', k.fecha_movimiento) AS mes,
    SUM(k.cantidad_entrada) AS total_entradas,
    SUM(k.cantidad_salida) AS total_salidas,
    SUM(k.costo_total_entrada) AS valor_entradas,
    SUM(k.costo_total_salida) AS valor_salidas,
    COUNT(*) AS num_movimientos
FROM kardex k
INNER JOIN productos p ON k.id_producto = p.id_producto
GROUP BY p.id_producto, p.nombre, DATE_TRUNC('month', k.fecha_movimiento);

-- Vista: Alertas Pendientes por Criticidad
CREATE OR REPLACE VIEW v_alertas_pendientes AS
SELECT 
    a.id_alerta,
    p.nombre AS producto,
    a.tipo_alerta,
    a.nivel_criticidad,
    a.mensaje,
    a.stock_actual,
    a.cantidad_sugerida,
    a.fecha_generacion,
    EXTRACT(DAY FROM (CURRENT_TIMESTAMP - a.fecha_generacion)) AS dias_pendiente,
    u.nombre AS usuario_asignado
FROM alertas_inventario a
INNER JOIN productos p ON a.id_producto = p.id_producto
LEFT JOIN usuarios u ON a.id_usuario_asignado = u.id_usuario
WHERE a.estado IN ('PENDIENTE', 'EN_PROCESO')
ORDER BY 
    CASE a.nivel_criticidad 
        WHEN 'CRITICA' THEN 1
        WHEN 'ALTA' THEN 2
        WHEN 'MEDIA' THEN 3
        WHEN 'BAJA' THEN 4
    END,
    a.fecha_generacion ASC;

-- Vista: Órdenes de Compra Pendientes
CREATE OR REPLACE VIEW v_ordenes_pendientes AS
SELECT 
    oc.id_orden_compra,
    oc.numero_orden,
    pr.razon_social AS proveedor,
    oc.fecha_orden,
    oc.fecha_entrega_esperada,
    oc.estado_orden,
    oc.total_orden,
    COUNT(doc.id_detalle) AS num_items,
    SUM(doc.cantidad_solicitada) AS total_unidades,
    CASE 
        WHEN oc.fecha_entrega_esperada < CURRENT_DATE THEN 'RETRASADA'
        WHEN oc.fecha_entrega_esperada = CURRENT_DATE THEN 'VENCE HOY'
        ELSE 'EN TIEMPO'
    END AS estado_entrega
FROM ordenes_compra oc
INNER JOIN proveedores pr ON oc.id_proveedor = pr.id_proveedor
LEFT JOIN detalle_orden_compra doc ON oc.id_orden_compra = doc.id_orden_compra
WHERE oc.estado_orden NOT IN ('RECIBIDA_COMPLETA', 'CANCELADA')
GROUP BY oc.id_orden_compra, oc.numero_orden, pr.razon_social, 
         oc.fecha_orden, oc.fecha_entrega_esperada, oc.estado_orden, oc.total_orden;

-- ============================================================================
-- 10. FUNCIONES ÚTILES
-- ============================================================================

-- Función: Calcular cantidad óptima de pedido (EOQ)
CREATE OR REPLACE FUNCTION calcular_eoq(
    demanda_anual INTEGER,
    costo_pedido DECIMAL,
    costo_mantenimiento_anual DECIMAL
) RETURNS INTEGER AS $$
BEGIN
    IF costo_mantenimiento_anual = 0 THEN
        RETURN 0;
    END IF;
    
    RETURN CEIL(SQRT((2.0 * demanda_anual * costo_pedido) / costo_mantenimiento_anual));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Función: Calcular punto de reorden (ROP)
CREATE OR REPLACE FUNCTION calcular_rop(
    demanda_diaria DECIMAL,
    dias_lead_time INTEGER,
    stock_seguridad INTEGER
) RETURNS INTEGER AS $$
BEGIN
    RETURN CEIL((demanda_diaria * dias_lead_time) + stock_seguridad);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- ============================================================================
-- 11. TRIGGERS PARA AUTOMATIZACIÓN
-- ============================================================================

-- Trigger: Actualizar fecha_ultima_actualizacion en Inventario
CREATE OR REPLACE FUNCTION actualizar_fecha_inventario()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_inventario
BEFORE UPDATE ON inventario
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_inventario();

-- Trigger: Calcular subtotal en Detalle Orden Compra
CREATE OR REPLACE FUNCTION calcular_subtotal_detalle()
RETURNS TRIGGER AS $$
BEGIN
    NEW.subtotal = NEW.cantidad_solicitada * NEW.precio_unitario;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calcular_subtotal
BEFORE INSERT OR UPDATE ON detalle_orden_compra
FOR EACH ROW
EXECUTE FUNCTION calcular_subtotal_detalle();

-- ============================================================================
-- 12. DATOS INICIALES (OPCIONAL)
-- ============================================================================

-- Insertar estados iniciales si no existen
-- Esto se puede hacer desde la aplicación Java también

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

-- NOTAS IMPORTANTES:
-- 1. Ajustar constraints según necesidades específicas
-- 2. Considerar particionamiento de tabla Kardex si crece mucho
-- 3. Implementar procedimientos almacenados para operaciones complejas
-- 4. Configurar jobs programados para generar alertas automáticas
-- 5. Implementar auditoría en tablas críticas
-- 6. Considerar replicación para alta disponibilidad
