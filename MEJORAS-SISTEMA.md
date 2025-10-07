# 📊 Sistema de Predicción de Demanda y Optimización de Inventario

## 🎯 Descripción del Proyecto
Sistema completo para predicción de demanda y optimización de recursos para tiendas de productos de consumo masivo, con funcionalidades avanzadas de control de inventario, Kardex/Cardex, y alertas automáticas.

---

## 🗂️ Estructura Mejorada de la Base de Datos

### **Entidades Principales**

#### 1. **Inventario** 
Control en tiempo real del stock de productos.

**Campos principales:**
- `stock_disponible`: Stock listo para venta
- `stock_reservado`: Stock comprometido pero no despachado
- `stock_en_transito`: Stock ordenado pero no recibido
- `stock_minimo`: Umbral mínimo de seguridad
- `punto_reorden`: Nivel que activa reabastecimiento automático
- `rotacion_inventario`: Indicador de rotación del producto
- `estado`: Estado del inventario (NORMAL, BAJO, CRITICO, EXCESO, etc.)

**Funcionalidades:**
- ✅ Alerta automática cuando stock < punto de reorden
- ✅ Cálculo automático de valor total del stock
- ✅ Control de días sin venta para detectar obsolescencia

---

#### 2. **Kardex (Cardex)**
Registro detallado de TODOS los movimientos de inventario.

**Campos principales:**
- `tipo_movimiento`: Entrada/Salida (Compra, Venta, Ajuste, Devolución, Merma, etc.)
- `cantidad_entrada/salida`: Cantidades del movimiento
- `saldo_cantidad`: Saldo después del movimiento
- `costo_unitario`: Costo del producto en ese momento
- `saldo_valorizado`: Valor total del inventario
- `lote`: Control de lotes
- `fecha_vencimiento`: Para productos perecederos
- `proveedor/cliente`: Origen/destino del movimiento

**Tipos de movimiento incluidos:**
- 📥 ENTRADA_COMPRA, ENTRADA_DEVOLUCION, ENTRADA_AJUSTE
- 📤 SALIDA_VENTA, SALIDA_DEVOLUCION, SALIDA_MERMA, SALIDA_VENCIMIENTO
- 🔄 AJUSTE_POSITIVO, AJUSTE_NEGATIVO

**Funcionalidades:**
- ✅ Trazabilidad completa de inventario
- ✅ Valorización de inventario (método promedio ponderado)
- ✅ Auditoría de movimientos
- ✅ Control de lotes y vencimientos

---

#### 3. **Proveedores**
Gestión completa de proveedores.

**Campos principales:**
- `razon_social`, `ruc_nit`: Datos legales
- `tiempo_entrega_dias`: Lead time del proveedor
- `dias_credito`: Condiciones de pago
- `calificacion`: Rating del proveedor (0-5)
- `estado`: Activo/Inactivo

---

#### 4. **Órdenes de Compra**
Órdenes generadas automáticamente o manualmente.

**Estados:**
- BORRADOR → PENDIENTE → APROBADA → ENVIADA → EN_TRANSITO → RECIBIDA

**Funcionalidades:**
- ✅ Generación automática basada en predicciones
- ✅ Cálculo automático de totales
- ✅ Tracking de estado de orden
- ✅ Comparación fecha esperada vs real

---

#### 5. **Alertas de Inventario**
Sistema de alertas automáticas inteligentes.

**Tipos de alertas:**
- 🔴 STOCK_CRITICO: Agotamiento inminente
- 🟡 PUNTO_REORDEN: Necesita reabastecimiento
- 🟠 VENCIMIENTO_PROXIMO: Productos próximos a vencer
- 🔵 SOBRESTOCK: Exceso de inventario
- ⚫ PRODUCTO_OBSOLETO: Sin movimiento prolongado
- 🟣 DEMANDA_ANOMALA: Picos inusuales de demanda

**Niveles de criticidad:**
- BAJA → MEDIA → ALTA → CRITICA

**Funcionalidades:**
- ✅ Generación automática de alertas
- ✅ Asignación a usuarios
- ✅ Seguimiento de acciones tomadas
- ✅ Estadísticas de resolución

---

#### 6. **Estacionalidad de Productos**
Captura patrones estacionales para mejorar predicciones.

**Campos:**
- `mes`: Mes del año (1-12)
- `factor_estacional`: Multiplicador (ej: 1.5 = 150% de demanda normal)
- `demanda_promedio_historica`: Promedio histórico
- `descripcion_temporada`: "Navidad", "Verano", "Regreso a clases"

**Funcionalidades:**
- ✅ Ajuste automático de predicciones según temporada
- ✅ Identificación de picos estacionales
- ✅ Optimización de stock para temporadas altas

---

#### 7. **Importación de Datos**
Registro y auditoría de importaciones masivas.

**Tipos de datos soportados:**
- Productos, Inventario, Kardex, Demanda, Proveedores, Ventas, Compras

**Campos:**
- `registros_procesados`, `registros_exitosos`, `registros_fallidos`
- `tiempo_procesamiento_ms`: Performance de la importación
- `errores`: Detalles de errores encontrados

**Funcionalidades:**
- ✅ Trazabilidad de importaciones
- ✅ Cálculo de tasa de éxito
- ✅ Registro de errores para corrección

---

## 🔄 Flujo del Sistema

### **1. Importación de Datos**
```
CSV/Excel → Sistema → Validación → Importación → Registro en ImportacionDatos
```

### **2. Control de Inventario**
```
Movimiento → Kardex → Actualiza Inventario → Verifica Umbrales → Genera Alertas
```

### **3. Predicción de Demanda**
```
RegistroDemanda → Análisis Estacional → Cálculo Predicción → CalculoOptimizacion
```

### **4. Generación de Órdenes**
```
Alerta Stock Bajo → Consulta EOQ/ROP → Genera OrdenCompra → Notifica Usuario
```

### **5. Recepción de Mercancía**
```
Orden Recibida → Entrada Kardex → Actualiza Inventario → Resuelve Alerta
```

---

## 🛠️ Mejoras Implementadas

### **Control de Inventario Avanzado**
- ✅ Stock disponible, reservado y en tránsito
- ✅ Control de ubicaciones en almacén
- ✅ Cálculo de rotación de inventario
- ✅ Detección de productos obsoletos
- ✅ Valorización automática

### **Kardex/Cardex Completo**
- ✅ Registro de TODOS los movimientos
- ✅ 15+ tipos de movimientos diferentes
- ✅ Método de valorización promedio ponderado
- ✅ Control de lotes y vencimientos
- ✅ Trazabilidad total

### **Sistema de Alertas Inteligente**
- ✅ 11 tipos de alertas diferentes
- ✅ 4 niveles de criticidad
- ✅ Asignación y seguimiento
- ✅ Resolución con histórico

### **Gestión de Proveedores**
- ✅ Base de datos de proveedores
- ✅ Lead times por proveedor
- ✅ Calificación de proveedores
- ✅ Términos de pago

### **Órdenes de Compra Automatizadas**
- ✅ Generación basada en predicciones
- ✅ 9 estados de seguimiento
- ✅ Comparación fechas esperadas vs reales
- ✅ Detalles por producto

### **Estacionalidad**
- ✅ Factores estacionales por mes
- ✅ Ajuste automático de predicciones
- ✅ Históricos por temporada

### **Importación Masiva**
- ✅ Soporte para múltiples tipos de datos
- ✅ Validación y registro de errores
- ✅ Métricas de rendimiento
- ✅ Auditoría completa

---

## 📋 Próximos Pasos para Implementación

### **1. Crear las tablas en PostgreSQL**
```sql
-- Ver archivo: database-schema.sql
```

### **2. Crear los Repositorios (DAO)**
```java
// Ejemplo:
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByProducto(Producto producto);
    List<Inventario> findByEstado(EstadoInventario estado);
    List<Inventario> findByStockDisponibleLessThanPuntoReorden();
}
```

### **3. Crear los Servicios**
- `InventarioService`: Gestión de inventario
- `KardexService`: Registro de movimientos
- `AlertaService`: Generación y gestión de alertas
- `OrdenCompraService`: Gestión de órdenes
- `ImportacionService`: Importación masiva de datos
- `PrediccionService`: Algoritmos de predicción

### **4. Implementar Funcionalidades**
- Importación desde CSV/Excel
- Dashboard de alertas
- Generación automática de órdenes
- Reportes de Kardex
- Análisis de estacionalidad
- Optimización EOQ/ROP con estacionalidad

---

## 🎓 Recomendaciones

### **Para Importación de Datos**
1. Usar Apache POI para leer Excel
2. Validar datos antes de insertar
3. Usar transacciones para rollback en caso de error
4. Registrar todo en `ImportacionDatos`

### **Para Kardex**
1. SIEMPRE registrar movimientos antes de actualizar inventario
2. Calcular saldo_valorizado usando promedio ponderado
3. No permitir eliminar registros de Kardex (auditoría)
4. Implementar triggers o listeners para sincronización

### **Para Alertas**
1. Ejecutar verificación periódica (cada hora o diaria)
2. Evitar duplicar alertas para el mismo producto
3. Escalar alertas no resueltas después de X días
4. Notificar por email/SMS alertas críticas

### **Para Predicción**
1. Considerar estacionalidad en algoritmos
2. Usar mínimo 12 meses de histórico
3. Aplicar suavizado exponencial o ARIMA
4. Re-entrenar modelos mensualmente

---

## 📊 Ejemplo de Uso del Kardex

```java
// Entrada por compra
Kardex entrada = new Kardex();
entrada.setProducto(producto);
entrada.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
entrada.setCantidadEntrada(100);
entrada.setCostoUnitario(new BigDecimal("10.50"));
entrada.setSaldoCantidad(stockAnterior + 100);
entrada.setProveedor("Proveedor ABC");
kardexService.registrarMovimiento(entrada);

// El servicio automáticamente:
// 1. Actualiza el inventario
// 2. Calcula saldo valorizado
// 3. Verifica si resuelve alertas
```

---

## 📈 Métricas Clave del Sistema

- **Tasa de cumplimiento de stock**: % de pedidos sin quiebres
- **Rotación de inventario**: Ventas / Stock promedio
- **Días de inventario**: Stock actual / Demanda diaria
- **Costo de mantenimiento**: % del valor de inventario
- **Precisión de predicción**: Error % entre predicho y real
- **Alertas resueltas**: % de alertas cerradas en tiempo
- **Calidad de proveedores**: Entregas a tiempo vs total

---

## 🚀 Tecnologías Utilizadas

- **Backend**: Spring Boot 3.5.6 + Java 21
- **ORM**: JPA/Hibernate
- **Base de Datos**: PostgreSQL
- **Lombok**: Reducción de código boilerplate
- **Maven**: Gestión de dependencias

---

## 👥 Soporte y Contribuciones

Para más información sobre la implementación o dudas específicas, consulta:
- Script SQL: `database-schema.sql`
- Documentación de APIs: (a crear)
- Ejemplos de uso: (a crear)

---

**Última actualización**: Octubre 2025
**Versión**: 2.0
