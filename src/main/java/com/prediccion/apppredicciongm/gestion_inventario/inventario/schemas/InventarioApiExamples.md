# Ejemplos de Uso - API de Inventario

Este documento contiene ejemplos detallados de cómo usar la API de Inventario.

## Tabla de Contenidos
- [Crear Inventario](#crear-inventario)
- [Actualizar Inventario](#actualizar-inventario)
- [Consultar Inventario](#consultar-inventario)
- [Alertas de Stock](#alertas-de-stock)
- [Ajuste de Stock](#ajuste-de-stock)
- [Resumen General](#resumen-general)

---

## Crear Inventario

### Endpoint
```
POST /api/inventario
```

### Request Body
```json
{
  "productoId": 1,
  "stockDisponible": 100,
  "stockReservado": 0,
  "stockEnTransito": 20,
  "stockMinimo": 10,
  "stockMaximo": 500,
  "puntoReorden": 25,
  "ubicacionAlmacen": "A-01-05",
  "observaciones": "Producto de alta rotación"
}
```

### Response (201 Created)
```json
{
  "inventarioId": 1,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "categoriaNombre": "Aceites y Grasas",
  "unidadMedida": "Litro",
  "stockDisponible": 100,
  "stockReservado": 0,
  "stockEnTransito": 20,
  "stockTotal": 120,
  "stockMinimo": 10,
  "stockMaximo": 500,
  "puntoReorden": 25,
  "ubicacionAlmacen": "A-01-05",
  "fechaUltimoMovimiento": null,
  "fechaUltimaActualizacion": "2025-10-14T10:30:00",
  "diasSinVenta": 0,
  "estado": "ACTIVO",
  "necesitaReorden": false,
  "bajoPuntoMinimo": false,
  "observaciones": "Producto de alta rotación"
}
```

---

## Actualizar Inventario

### Endpoint
```
PUT /api/inventario/{id}
```

### Request Body
```json
{
  "stockMinimo": 15,
  "stockMaximo": 600,
  "puntoReorden": 30,
  "ubicacionAlmacen": "A-02-10",
  "observaciones": "Cambio de ubicación por reorganización"
}
```

### Response (200 OK)
```json
{
  "inventarioId": 1,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "categoriaNombre": "Aceites y Grasas",
  "unidadMedida": "Litro",
  "stockDisponible": 100,
  "stockReservado": 0,
  "stockEnTransito": 20,
  "stockTotal": 120,
  "stockMinimo": 15,
  "stockMaximo": 600,
  "puntoReorden": 30,
  "ubicacionAlmacen": "A-02-10",
  "fechaUltimoMovimiento": null,
  "fechaUltimaActualizacion": "2025-10-14T11:00:00",
  "diasSinVenta": 0,
  "estado": "ACTIVO",
  "necesitaReorden": false,
  "bajoPuntoMinimo": false,
  "observaciones": "Cambio de ubicación por reorganización"
}
```

---

## Consultar Inventario

### 1. Obtener por ID
```
GET /api/inventario/1
```

### 2. Listar todos (paginado)
```
GET /api/inventario?pagina=0&tamano=10
```

### Response (200 OK)
```json
{
  "content": [
    {
      "inventarioId": 1,
      "nombreProducto": "Aceite Vegetal 1L",
      "stockDisponible": 100,
      "estado": "ACTIVO"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 3. Buscar por producto
```
GET /api/inventario/producto/1
```

### 4. Buscar por nombre
```
GET /api/inventario/buscar?nombre=aceite&pagina=0&tamano=10
```

### 5. Buscar por categoría
```
GET /api/inventario/categoria/3
```

### 6. Buscar por estado
```
GET /api/inventario/estado/ACTIVO?pagina=0&tamano=10
```

### 7. Buscar por rango de stock
```
GET /api/inventario/rango-stock?minStock=0&maxStock=50
```

---

## Alertas de Stock

### 1. Alertas de stock bajo (punto de reorden)
```
GET /api/inventario/alertas/stock-bajo
```

### Response (200 OK)
```json
[
  {
    "inventarioId": 5,
    "productoId": 5,
    "nombreProducto": "Detergente en Polvo 1kg",
    "categoriaNombre": "Limpieza",
    "stockDisponible": 22,
    "stockMinimo": 15,
    "puntoReorden": 30,
    "estado": "ACTIVO",
    "tipoAlerta": "REORDEN",
    "mensaje": "Stock alcanzó punto de reorden (30 unidades)",
    "diasSinVenta": 0
  }
]
```

### 2. Alertas críticas (bajo mínimo)
```
GET /api/inventario/alertas/criticas
```

### Response (200 OK)
```json
[
  {
    "inventarioId": 8,
    "productoId": 8,
    "nombreProducto": "Papel Higiénico 4 rollos",
    "categoriaNombre": "Cuidado Personal",
    "stockDisponible": 8,
    "stockMinimo": 20,
    "puntoReorden": 40,
    "estado": "BAJO_STOCK",
    "tipoAlerta": "BAJO_MINIMO",
    "mensaje": "Stock por debajo del mínimo (20 unidades)",
    "diasSinVenta": 2
  }
]
```

### 3. Productos agotados
```
GET /api/inventario/alertas/agotados
```

### Response (200 OK)
```json
[
  {
    "inventarioId": 12,
    "productoId": 12,
    "nombreProducto": "Leche Entera 1L",
    "categoriaNombre": "Lácteos",
    "stockDisponible": 0,
    "stockMinimo": 50,
    "puntoReorden": 100,
    "estado": "AGOTADO",
    "tipoAlerta": "CRITICO",
    "mensaje": "Producto agotado - Requiere acción inmediata",
    "diasSinVenta": 0
  }
]
```

### 4. Productos sin movimiento
```
GET /api/inventario/alertas/sin-movimiento?dias=30
```

### Response (200 OK)
```json
[
  {
    "inventarioId": 15,
    "productoId": 15,
    "nombreProducto": "Vinagre Blanco 500ml",
    "categoriaNombre": "Condimentos",
    "stockDisponible": 45,
    "stockMinimo": 10,
    "puntoReorden": 20,
    "estado": "ACTIVO",
    "tipoAlerta": "SIN_MOVIMIENTO",
    "mensaje": "Producto sin movimiento por 45 días",
    "diasSinVenta": 45
  }
]
```

### 5. Inventarios sobre stock máximo
```
GET /api/inventario/sobre-maximo
```

---

## Ajuste de Stock

### Endpoint
```
POST /api/inventario/ajustar-stock
```

### Ajuste Positivo (Ingreso)
```json
{
  "inventarioId": 1,
  "cantidad": 50,
  "motivo": "Ajuste por conteo físico",
  "observaciones": "Se encontraron 50 unidades adicionales en revisión de inventario"
}
```

### Ajuste Negativo (Salida)
```json
{
  "inventarioId": 1,
  "cantidad": -10,
  "motivo": "Ajuste por productos dañados",
  "observaciones": "Productos con envase deteriorado, no aptos para venta"
}
```

### Response (200 OK)
```json
{
  "inventarioId": 1,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "categoriaNombre": "Aceites y Grasas",
  "unidadMedida": "Litro",
  "stockDisponible": 90,
  "stockReservado": 0,
  "stockEnTransito": 20,
  "stockTotal": 110,
  "stockMinimo": 15,
  "stockMaximo": 600,
  "puntoReorden": 30,
  "ubicacionAlmacen": "A-02-10",
  "fechaUltimoMovimiento": "2025-10-14T14:30:00",
  "fechaUltimaActualizacion": "2025-10-14T14:30:00",
  "diasSinVenta": 0,
  "estado": "ACTIVO",
  "necesitaReorden": false,
  "bajoPuntoMinimo": false,
  "observaciones": "Cambio de ubicación por reorganización"
}
```

---

## Resumen General

### Endpoint
```
GET /api/inventario/resumen
```

### Response (200 OK)
```json
{
  "totalProductos": 150,
  "productosActivos": 142,
  "productosInactivos": 8,
  "productosConStockBajo": 12,
  "productosAgotados": 3,
  "productosSinMovimiento": 15,
  "valorTotalInventario": 125450.75,
  "stockTotalDisponible": 8550
}
```

---

## Verificaciones

### 1. Verificar si necesita reorden
```
GET /api/inventario/1/necesita-reorden
```

### Response (200 OK)
```json
false
```

### 2. Verificar si está bajo punto mínimo
```
GET /api/inventario/1/bajo-minimo
```

### Response (200 OK)
```json
false
```

---

## Códigos de Error

### 400 Bad Request
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El stock disponible es obligatorio",
  "path": "/api/inventario"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Inventario no encontrado con ID: 999",
  "path": "/api/inventario/999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un inventario para el producto con ID: 1",
  "path": "/api/inventario"
}
```

---

## Notas Importantes

1. **Paginación**: Los endpoints que retornan listas paginadas aceptan los parámetros `pagina` (inicia en 0) y `tamano`.

2. **Estados de Inventario**:
   - `ACTIVO`: Stock normal
   - `BAJO_STOCK`: Por debajo del punto de reorden
   - `AGOTADO`: Sin stock disponible
   - `INACTIVO`: Producto descontinuado o fuera de catálogo

3. **Stock Total**: Se calcula como: `stockDisponible + stockReservado + stockEnTransito`

4. **Alertas**: El sistema genera automáticamente alertas basándose en los umbrales configurados.

5. **Ajustes**: Los ajustes de stock registran la razón y actualización automáticamente el estado del inventario.
