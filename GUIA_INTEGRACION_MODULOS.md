# 🔗 Guía de Integración: Producto → Inventario → Kardex

## 📋 Tabla de Contenidos
- [Arquitectura de Integración](#arquitectura-de-integración)
- [Flujo de Datos](#flujo-de-datos)
- [Endpoints Disponibles](#endpoints-disponibles)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Relaciones entre Módulos](#relaciones-entre-módulos)

---

## 🏗️ Arquitectura de Integración

### Estructura de Módulos

```
gestion_inventario/
├── producto/          → Catálogo de productos
│   ├── controller/    → ProductoControlador
│   ├── services/      → ProductoService (integra con Inventario y Kardex)
│   ├── dto/
│   ├── mapper/
│   ├── repository/
│   └── schemas/       → ProductoExamples (Swagger)
│
├── inventario/        → Control de stock
│   ├── controller/    → InventarioControlador
│   ├── services/      → InventarioServicio
│   ├── dto/
│   ├── mapper/
│   ├── repository/
│   └── schemas/       → InventarioExamples (Swagger)
│
└── movimiento/        → Registro de movimientos (Kardex)
    ├── controller/    → KardexControlador
    ├── services/      → KardexServicioImpl
    ├── dto/
    ├── mapper/
    ├── repository/
    └── schemas/       → KardexExamples (Swagger)
```

### Diagrama de Relaciones

```
┌─────────────┐
│  PRODUCTO   │ (1 Producto)
└──────┬──────┘
       │ 1:1
       ↓
┌─────────────┐
│ INVENTARIO  │ (1 Inventario por Producto)
└──────┬──────┘
       │ 1:N
       ↓
┌─────────────┐
│   KARDEX    │ (N Movimientos de Inventario)
└─────────────┘
```

---

## 🔄 Flujo de Datos

### Flujo Completo: Crear Producto → Inventario → Movimiento

#### 1️⃣ Crear Producto

**Endpoint**: `POST /api/productos`

```json
{
  "nombre": "Arroz Premium 1kg",
  "categoriaId": 1,
  "unidadMedidaId": 1,
  "diasLeadTime": 5,
  "costoAdquisicion": 2.50,
  "costoMantenimiento": 0.10,
  "costoPedido": 15.00
}
```

**Respuesta**:
```json
{
  "productoId": 1,
  "nombre": "Arroz Premium 1kg",
  "categoriaNombre": "Alimentos",
  "tieneInventario": false,  ← No tiene inventario aún
  "stockDisponible": 0
}
```

---

#### 2️⃣ Crear Inventario para el Producto

**Endpoint**: `POST /api/inventario`

```json
{
  "productoId": 1,              ← Relaciona con el producto creado
  "stockDisponible": 150,
  "stockReservado": 0,
  "stockEnTransito": 0,
  "stockMinimo": 50,
  "stockMaximo": 500,
  "puntoReorden": 80,
  "ubicacionAlmacen": "A-01-05"
}
```

**Respuesta**:
```json
{
  "inventarioId": 1,
  "productoId": 1,
  "nombreProducto": "Arroz Premium 1kg",
  "stockDisponible": 150,
  "stockTotal": 150,
  "estado": "NORMAL"
}
```

---

#### 3️⃣ Registrar Movimiento de Compra (Kardex)

**Endpoint**: `POST /api/movimientos`

```json
{
  "productoId": 1,              ← Relaciona con el producto
  "tipoMovimiento": "COMPRA",
  "cantidad": 100,
  "precioUnitario": 2.50,
  "numeroDocumento": "FC-2025-001234",
  "proveedorId": 1,
  "usuarioId": 1,
  "lote": "L-2025-10-001",
  "fechaVencimiento": "2026-10-14"
}
```

**Respuesta**:
```json
{
  "kardexId": 1,
  "productoId": 1,
  "nombreProducto": "Arroz Premium 1kg",
  "tipoMovimiento": "COMPRA",
  "cantidad": 100,
  "valorTotal": 250.00,
  "saldoAnterior": 150,
  "saldoActual": 250              ← Stock actualizado automáticamente
}
```

**✅ El servicio de Kardex actualiza automáticamente el inventario**

---

#### 4️⃣ Consultar Producto con Inventario Actualizado

**Endpoint**: `GET /api/productos/1`

```json
{
  "productoId": 1,
  "nombre": "Arroz Premium 1kg",
  "tieneInventario": true,      ← Ahora tiene inventario
  "stockDisponible": 250,       ← Stock actualizado
  "estadoInventario": "NORMAL",
  "valorInventario": 625.00     ← 250 unidades × $2.50
}
```

---

## 📡 Endpoints Disponibles

### Módulo Producto (9 Endpoints)

| Método | Endpoint | Descripción | Integración |
|--------|----------|-------------|-------------|
| `POST` | `/api/productos` | Crear producto | - |
| `PUT` | `/api/productos/{id}` | Actualizar producto | - |
| `DELETE` | `/api/productos/{id}` | Eliminar producto | ⚠️ Verifica inventario |
| `GET` | `/api/productos/{id}` | Obtener producto | ✅ Incluye datos de inventario |
| `GET` | `/api/productos` | Listar productos | ✅ Incluye datos de inventario |
| `GET` | `/api/productos/categoria/{id}` | Por categoría | ✅ Incluye datos de inventario |
| `GET` | `/api/productos/buscar?nombre=` | Buscar por nombre | ✅ Incluye datos de inventario |

### Módulo Inventario (20 Endpoints)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/inventario` | Crear inventario |
| `PUT` | `/api/inventario/{id}` | Actualizar inventario |
| `POST` | `/api/inventario/ajustar-stock` | Ajuste manual |
| `GET` | `/api/inventario/alertas/stock-bajo` | Alertas de reorden |
| ... | ... | Ver documentación completa |

### Módulo Kardex (25 Endpoints)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/movimientos` | Registrar movimiento |
| `GET` | `/api/movimientos/producto/{id}` | Historial del producto |
| `GET` | `/api/movimientos/tipo/{tipo}` | Por tipo de movimiento |
| ... | ... | Ver documentación completa |

---

## 💡 Ejemplos de Uso

### Caso 1: Flujo Completo de Compra

```bash
# 1. Crear producto (si no existe)
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Detergente Líquido 1L",
    "categoriaId": 3,
    "unidadMedidaId": 2,
    "diasLeadTime": 7,
    "costoAdquisicion": 3.80,
    "costoMantenimiento": 0.15,
    "costoPedido": 25.00
  }'

# 2. Crear inventario inicial
curl -X POST http://localhost:8080/api/inventario \
  -H "Content-Type: application/json" \
  -d '{
    "productoId": 3,
    "stockDisponible": 0,
    "stockMinimo": 20,
    "stockMaximo": 200,
    "puntoReorden": 30,
    "ubicacionAlmacen": "B-05-12"
  }'

# 3. Registrar compra (actualiza inventario automáticamente)
curl -X POST http://localhost:8080/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "productoId": 3,
    "tipoMovimiento": "COMPRA",
    "cantidad": 50,
    "precioUnitario": 3.80,
    "numeroDocumento": "FC-2025-456",
    "proveedorId": 2,
    "usuarioId": 1
  }'

# 4. Verificar producto con inventario actualizado
curl -X GET http://localhost:8080/api/productos/3
```

---

### Caso 2: Venta con Validación de Stock

```bash
# 1. Verificar stock disponible
curl -X GET http://localhost:8080/api/inventario/producto/3

# Respuesta:
# {
#   "stockDisponible": 50,
#   "estado": "NORMAL"
# }

# 2. Registrar venta (valida stock automáticamente)
curl -X POST http://localhost:8080/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "productoId": 3,
    "tipoMovimiento": "VENTA",
    "cantidad": 15,
    "precioUnitario": 5.50,
    "numeroDocumento": "FV-2025-789",
    "usuarioId": 2
  }'

# 3. Stock actualizado automáticamente:
# stockDisponible: 50 - 15 = 35
```

---

### Caso 3: Alertas de Inventario Bajo

```bash
# 1. Obtener productos con stock bajo
curl -X GET http://localhost:8080/api/inventario/alertas/stock-bajo

# 2. Ver detalles del producto con alerta
curl -X GET http://localhost:8080/api/productos/3

# Respuesta:
# {
#   "productoId": 3,
#   "nombre": "Detergente Líquido 1L",
#   "stockDisponible": 35,
#   "puntoReorden": 30,
#   "necesitaReorden": true,  ← Indicador de alerta
#   "estadoInventario": "BAJO"
# }

# 3. Generar pedido de reorden (use el endpoint de compra)
```

---

### Caso 4: Consultar Historial de Movimientos

```bash
# 1. Ver todos los movimientos de un producto
curl -X GET "http://localhost:8080/api/movimientos/producto/3?pagina=0&tamanioPagina=20"

# 2. Ver solo compras
curl -X GET "http://localhost:8080/api/movimientos/tipo/COMPRA"

# 3. Ver historial de precios
curl -X GET http://localhost:8080/api/movimientos/producto/3/historial-precios

# 4. Obtener resumen de movimientos
curl -X GET http://localhost:8080/api/movimientos/resumen
```

---

### Caso 5: Eliminación de Producto con Validaciones

```bash
# 1. Intentar eliminar producto con stock (FALLA)
curl -X DELETE http://localhost:8080/api/productos/3

# Respuesta:
# {
#   "status": 409,
#   "error": "Conflict",
#   "message": "No se puede eliminar el producto porque tiene inventario activo con stock disponible: 35 unidades"
# }

# 2. Eliminar producto sin stock (ÉXITO)
curl -X DELETE http://localhost:8080/api/productos/5

# Respuesta:
# {
#   "mensaje": "Producto eliminado exitosamente",
#   "productoId": 5,
#   "inventarioEliminado": true,
#   "movimientosArchivados": 8
# }
```

---

## 🔗 Relaciones entre Módulos

### 1. ProductoService → Inventario

```java
// ProductoService.java
private ProductoResponse enrichProductoResponse(Producto producto) {
    // Buscar inventario del producto
    Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(producto.getProductoId());
    
    if (inventarioOpt.isPresent()) {
        Inventario inventario = inventarioOpt.get();
        response.setTieneInventario(true);
        response.setStockDisponible(inventario.getStockDisponible());
        response.setEstadoInventario(inventario.getEstado().name());
        // Calcula valor de inventario
        response.setValorInventario(
            producto.getCostoAdquisicion()
                .multiply(BigDecimal.valueOf(inventario.getStockDisponible()))
        );
    }
    
    return response;
}
```

### 2. KardexService → Inventario (Actualización Automática)

```java
// KardexServicioImpl.java
@Transactional
public KardexResponse registrarMovimiento(KardexCreateRequest request) {
    // 1. Crear movimiento
    Kardex movimiento = ...;
    
    // 2. Actualizar inventario automáticamente
    boolean esEntrada = TipoMovimiento.esEntrada(request.getTipoMovimiento());
    inventarioServicio.actualizarStockDesdeMovimiento(
        request.getProductoId(),
        request.getCantidad(),
        esEntrada
    );
    
    // 3. Calcular saldo actual
    Integer saldoActual = calcularSaldoActualProducto(request.getProductoId());
    movimiento.setSaldoActual(saldoActual);
    
    return kardexMapper.toResponse(kardexRepositorio.save(movimiento));
}
```

### 3. InventarioService (Actualización desde Movimiento)

```java
// InventarioServicio.java
@Transactional
public void actualizarStockDesdeMovimiento(Integer productoId, Integer cantidad, boolean esEntrada) {
    Inventario inventario = inventarioRepositorio.findByProducto(productoId)
        .orElseThrow(...);
    
    Integer nuevoStock;
    if (esEntrada) {
        nuevoStock = inventario.getStockDisponible() + cantidad;
    } else {
        nuevoStock = inventario.getStockDisponible() - cantidad;
        if (nuevoStock < 0) {
            throw new StockInsuficienteException(...);
        }
    }
    
    inventario.setStockDisponible(nuevoStock);
    inventario.setFechaUltimoMovimiento(LocalDateTime.now());
    
    // Actualizar estado automáticamente
    if (nuevoStock == 0) {
        inventario.setEstado(EstadoInventario.CRITICO);
    } else if (inventario.bajoPuntoMinimo()) {
        inventario.setEstado(EstadoInventario.BAJO);
    } else {
        inventario.setEstado(EstadoInventario.NORMAL);
    }
    
    inventarioRepositorio.save(inventario);
}
```

---

## 🎯 Beneficios de la Integración

### ✅ Consistencia de Datos
- El stock se actualiza automáticamente con cada movimiento
- No hay desincronización entre inventario y kardex
- Transacciones garantizan integridad

### ✅ Trazabilidad Completa
- Cada producto tiene su historial de movimientos
- Se puede auditar cualquier cambio de stock
- Historial de precios disponible

### ✅ Validaciones Automáticas
- No se puede vender más de lo disponible
- No se puede eliminar productos con stock
- Alertas automáticas de reorden

### ✅ Información Enriquecida
- Los productos incluyen datos de inventario
- Cálculo automático de valor de inventario
- Estados actualizados en tiempo real

---

## 📚 Documentación Swagger

Cada módulo tiene sus ejemplos de Swagger definidos en clases dedicadas:

### ProductoExamples.java
- `CREAR_PRODUCTO_ALIMENTO`
- `CREAR_PRODUCTO_BEBIDA`
- `CREAR_PRODUCTO_LIMPIEZA`
- `CREAR_PRODUCTO_MEDICAMENTO`
- `RESPONSE_PRODUCTO_CREADO`
- `RESPONSE_PRODUCTO_CON_INVENTARIO`
- `ERROR_PRODUCTO_NO_ENCONTRADO`

### InventarioExamples.java
- `CREAR_INVENTARIO_BASICO`
- `AJUSTE_STOCK_ENTRADA`
- `RESPONSE_INVENTARIO_NORMAL`
- `RESPONSE_ALERTA_CRITICA`
- `ERROR_STOCK_INSUFICIENTE`

### KardexExamples.java
- `REGISTRAR_COMPRA`
- `REGISTRAR_VENTA`
- `REGISTRAR_AJUSTE_ENTRADA`
- `RESPONSE_MOVIMIENTO_COMPRA`
- `RESPONSE_HISTORIAL_MOVIMIENTOS`

---

## 🚀 Acceso a Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Todos los endpoints están documentados con ejemplos interactivos que puedes probar directamente desde el navegador.

---

**Fecha de Actualización**: 14 de Octubre, 2025  
**Versión**: 1.0.0  
**Sistema**: Gestión de Inventario - Predicción de Demanda
