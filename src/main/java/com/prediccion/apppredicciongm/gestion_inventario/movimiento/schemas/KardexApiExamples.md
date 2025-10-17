# Ejemplos de Uso - API de Movimientos de Inventario (Kardex)

Este documento contiene ejemplos detallados de cómo usar la API de Movimientos de Inventario.

## Tabla de Contenidos
- [Registrar Movimientos](#registrar-movimientos)
- [Consultar Movimientos](#consultar-movimientos)
- [Búsquedas Avanzadas](#búsquedas-avanzadas)
- [Análisis y Reportes](#análisis-y-reportes)

---

## Registrar Movimientos

### Endpoint
```
POST /api/movimientos
```

### 1. Registro de Compra (Entrada)

```json
{
  "productoId": 1,
  "fechaMovimiento": "2025-10-14T10:30:00",
  "tipoMovimiento": "COMPRA",
  "tipoDocumento": "FACTURA",
  "numeroDocumento": "F001-00012345",
  "cantidad": 200,
  "costoUnitario": 12.50,
  "lote": "LOT-2025-001",
  "fechaVencimiento": "2026-10-14T00:00:00",
  "proveedorId": 5,
  "motivo": "Compra regular de inventario",
  "referencia": "OC-2025-0045",
  "usuarioId": 1,
  "observaciones": "Producto en buen estado, revisado y aceptado",
  "ubicacion": "A-01-05"
}
```

### Response (201 Created)
```json
{
  "kardexId": 1,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "categoriaProducto": "Aceites y Grasas",
  "fechaMovimiento": "2025-10-14T10:30:00",
  "tipoMovimiento": "COMPRA",
  "tipoDocumento": "FACTURA",
  "numeroDocumento": "F001-00012345",
  "cantidad": 200,
  "saldoCantidad": 200,
  "costoUnitario": 12.50,
  "valorTotal": 2500.00,
  "lote": "LOT-2025-001",
  "fechaVencimiento": "2026-10-14T00:00:00",
  "proveedorId": 5,
  "nombreProveedor": "Distribuidora Alimentos S.A.",
  "motivo": "Compra regular de inventario",
  "referencia": "OC-2025-0045",
  "usuarioId": 1,
  "nombreUsuario": "Juan Pérez",
  "observaciones": "Producto en buen estado, revisado y aceptado",
  "ubicacion": "A-01-05",
  "fechaRegistro": "2025-10-14T10:30:15"
}
```

---

### 2. Registro de Venta (Salida)

```json
{
  "productoId": 1,
  "fechaMovimiento": "2025-10-14T15:45:00",
  "tipoMovimiento": "VENTA",
  "tipoDocumento": "BOLETA",
  "numeroDocumento": "B001-00098765",
  "cantidad": 50,
  "costoUnitario": 12.50,
  "motivo": "Venta al cliente",
  "referencia": "VENTA-2025-1234",
  "usuarioId": 2,
  "observaciones": "Cliente mayorista",
  "ubicacion": "A-01-05"
}
```

### Response (201 Created)
```json
{
  "kardexId": 2,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "categoriaProducto": "Aceites y Grasas",
  "fechaMovimiento": "2025-10-14T15:45:00",
  "tipoMovimiento": "VENTA",
  "tipoDocumento": "BOLETA",
  "numeroDocumento": "B001-00098765",
  "cantidad": 50,
  "saldoCantidad": 150,
  "costoUnitario": 12.50,
  "valorTotal": 625.00,
  "lote": null,
  "fechaVencimiento": null,
  "proveedorId": null,
  "nombreProveedor": null,
  "motivo": "Venta al cliente",
  "referencia": "VENTA-2025-1234",
  "usuarioId": 2,
  "nombreUsuario": "María González",
  "observaciones": "Cliente mayorista",
  "ubicacion": "A-01-05",
  "fechaRegistro": "2025-10-14T15:45:10"
}
```

---

### 3. Ajuste de Inventario (Entrada)

```json
{
  "productoId": 1,
  "tipoMovimiento": "AJUSTE_ENTRADA",
  "cantidad": 10,
  "motivo": "Ajuste por conteo físico",
  "usuarioId": 1,
  "observaciones": "Se encontraron 10 unidades adicionales en revisión"
}
```

---

### 4. Ajuste de Inventario (Salida)

```json
{
  "productoId": 1,
  "tipoMovimiento": "AJUSTE_SALIDA",
  "cantidad": 5,
  "motivo": "Producto dañado",
  "usuarioId": 1,
  "observaciones": "Envases deteriorados, no aptos para venta"
}
```

---

### 5. Devolución de Cliente

```json
{
  "productoId": 1,
  "tipoMovimiento": "DEVOLUCION_CLIENTE",
  "tipoDocumento": "NOTA_CREDITO",
  "numeroDocumento": "NC-001-00001",
  "cantidad": 5,
  "costoUnitario": 12.50,
  "motivo": "Producto en mal estado reportado por cliente",
  "referencia": "B001-00098765",
  "usuarioId": 2,
  "observaciones": "Cliente reportó envases abollados"
}
```

---

### 6. Devolución a Proveedor

```json
{
  "productoId": 1,
  "tipoMovimiento": "DEVOLUCION_PROVEEDOR",
  "tipoDocumento": "NOTA_DEBITO",
  "numeroDocumento": "ND-001-00001",
  "cantidad": 10,
  "costoUnitario": 12.50,
  "proveedorId": 5,
  "motivo": "Producto no cumple especificaciones",
  "referencia": "F001-00012345",
  "usuarioId": 1,
  "observaciones": "Fecha de vencimiento muy próxima"
}
```

---

## Consultar Movimientos

### 1. Listar todos los movimientos (paginado)
```
GET /api/movimientos?pagina=0&tamano=10
```

### Response (200 OK)
```json
{
  "content": [
    {
      "kardexId": 2,
      "nombreProducto": "Aceite Vegetal 1L",
      "tipoMovimiento": "VENTA",
      "cantidad": 50,
      "saldoCantidad": 150,
      "fechaMovimiento": "2025-10-14T15:45:00"
    },
    {
      "kardexId": 1,
      "nombreProducto": "Aceite Vegetal 1L",
      "tipoMovimiento": "COMPRA",
      "cantidad": 200,
      "saldoCantidad": 200,
      "fechaMovimiento": "2025-10-14T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 2,
  "totalPages": 1
}
```

---

### 2. Obtener movimiento por ID
```
GET /api/movimientos/1
```

---

### 3. Movimientos por producto
```
GET /api/movimientos/producto/1?pagina=0&tamano=10
```

---

### 4. Movimientos por producto y fecha
```
GET /api/movimientos/producto/1/fecha?fechaInicio=2025-10-01T00:00:00&fechaFin=2025-10-31T23:59:59&pagina=0&tamano=20
```

---

### 5. Último movimiento de un producto
```
GET /api/movimientos/producto/1/ultimo
```

### Response (200 OK)
```json
{
  "kardexId": 2,
  "productoId": 1,
  "nombreProducto": "Aceite Vegetal 1L",
  "tipoMovimiento": "VENTA",
  "cantidad": 50,
  "saldoCantidad": 150,
  "fechaMovimiento": "2025-10-14T15:45:00"
}
```

---

## Búsquedas Avanzadas

### 1. Movimientos por tipo
```
GET /api/movimientos/tipo/COMPRA?pagina=0&tamano=10
```

**Tipos disponibles:**
- `COMPRA`
- `VENTA`
- `AJUSTE_ENTRADA`
- `AJUSTE_SALIDA`
- `DEVOLUCION_CLIENTE`
- `DEVOLUCION_PROVEEDOR`
- `PRODUCCION`
- `CONSUMO`
- `MERMA`
- `TRANSFERENCIA_ENTRADA`
- `TRANSFERENCIA_SALIDA`

---

### 2. Movimientos por producto y tipo
```
GET /api/movimientos/producto/1/tipo/VENTA?pagina=0&tamano=10
```

---

### 3. Movimientos por rango de fechas
```
GET /api/movimientos/fecha?fechaInicio=2025-10-01T00:00:00&fechaFin=2025-10-31T23:59:59&pagina=0&tamano=20
```

---

### 4. Movimientos por proveedor
```
GET /api/movimientos/proveedor/5?pagina=0&tamano=10
```

---

### 5. Movimientos por usuario
```
GET /api/movimientos/usuario/1?pagina=0&tamano=10
```

---

### 6. Buscar por número de documento
```
GET /api/movimientos/documento/F001-00012345
```

### Response (200 OK)
```json
[
  {
    "kardexId": 1,
    "productoId": 1,
    "nombreProducto": "Aceite Vegetal 1L",
    "tipoMovimiento": "COMPRA",
    "numeroDocumento": "F001-00012345",
    "cantidad": 200,
    "fechaMovimiento": "2025-10-14T10:30:00"
  }
]
```

---

### 7. Buscar por lote
```
GET /api/movimientos/lote/LOT-2025-001
```

### Response (200 OK)
```json
[
  {
    "kardexId": 1,
    "productoId": 1,
    "nombreProducto": "Aceite Vegetal 1L",
    "lote": "LOT-2025-001",
    "fechaVencimiento": "2026-10-14T00:00:00",
    "cantidad": 200,
    "saldoCantidad": 200
  }
]
```

---

### 8. Productos próximos a vencer
```
GET /api/movimientos/vencimiento-proximo?fechaInicio=2025-10-01T00:00:00&fechaFin=2025-11-30T23:59:59
```

### Response (200 OK)
```json
[
  {
    "kardexId": 45,
    "productoId": 15,
    "nombreProducto": "Yogurt Natural 1L",
    "lote": "LOT-2025-089",
    "fechaVencimiento": "2025-10-25T00:00:00",
    "cantidad": 50,
    "saldoCantidad": 120,
    "diasParaVencer": 11
  }
]
```

---

### 9. Historial de precios de un producto
```
GET /api/movimientos/producto/1/historial-precios
```

### Response (200 OK)
```json
[
  {
    "kardexId": 1,
    "fechaMovimiento": "2025-10-14T10:30:00",
    "tipoMovimiento": "COMPRA",
    "costoUnitario": 12.50,
    "proveedorId": 5,
    "nombreProveedor": "Distribuidora Alimentos S.A."
  },
  {
    "kardexId": 120,
    "fechaMovimiento": "2025-09-15T14:20:00",
    "tipoMovimiento": "COMPRA",
    "costoUnitario": 11.80,
    "proveedorId": 5,
    "nombreProveedor": "Distribuidora Alimentos S.A."
  }
]
```

---

## Análisis y Reportes

### 1. Resumen de movimientos
```
GET /api/movimientos/resumen
```

### Response (200 OK)
```json
{
  "totalMovimientos": 1250,
  "totalEntradas": 450,
  "totalSalidas": 750,
  "totalAjustes": 50,
  "cantidadTotalEntrada": 25000,
  "cantidadTotalSalida": 22000,
  "fechaUltimoMovimiento": "2025-10-14T15:45:00",
  "productoMasMovido": "Aceite Vegetal 1L"
}
```

---

### 2. Calcular saldo actual de un producto
```
GET /api/movimientos/producto/1/saldo
```

### Response (200 OK)
```json
150
```

---

## Tipos de Movimiento Detallados

### Movimientos de Entrada (Incrementan Stock)
| Tipo | Descripción | Uso |
|------|-------------|-----|
| `COMPRA` | Compra a proveedor | Registro de facturas de compra |
| `DEVOLUCION_CLIENTE` | Cliente devuelve producto | Notas de crédito |
| `AJUSTE_ENTRADA` | Ajuste positivo | Diferencias por conteo físico |
| `PRODUCCION` | Fabricación interna | Para empresas que producen |
| `TRANSFERENCIA_ENTRADA` | Recepción de otra sucursal | Para multi-sucursal |

### Movimientos de Salida (Decrementan Stock)
| Tipo | Descripción | Uso |
|------|-------------|-----|
| `VENTA` | Venta a cliente | Boletas, facturas de venta |
| `DEVOLUCION_PROVEEDOR` | Devolución a proveedor | Notas de débito |
| `AJUSTE_SALIDA` | Ajuste negativo | Productos dañados, vencidos |
| `CONSUMO` | Uso interno | Muestras, degustaciones |
| `MERMA` | Pérdida natural | Deterioro, vencimiento |
| `TRANSFERENCIA_SALIDA` | Envío a otra sucursal | Para multi-sucursal |

---

## Códigos de Error

### 400 Bad Request - Stock Insuficiente
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente. Saldo actual: 10, Cantidad solicitada: 50",
  "path": "/api/movimientos"
}
```

### 404 Not Found - Producto no encontrado
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado con ID: 999",
  "path": "/api/movimientos"
}
```

---

## Notas Importantes

1. **Saldo Automático**: El sistema calcula automáticamente el saldo después de cada movimiento.

2. **Actualización de Inventario**: Cada movimiento actualiza automáticamente el inventario del producto.

3. **Fechas**: Si no se proporciona `fechaMovimiento`, se usa la fecha y hora actual.

4. **Lotes y Vencimientos**: Importante para productos con fecha de caducidad (alimentos, medicamentos).

5. **Trazabilidad**: Todos los movimientos registran el usuario que los realiza para auditoría.

6. **Documentos**: Los números de documento permiten vincular los movimientos con la documentación física/digital.

7. **Referencia Cruzada**: El campo `referencia` permite relacionar movimientos (ej: devolución con venta original).

8. **Eliminación**: Se recomienda NO eliminar movimientos sino anularlos con un movimiento contrario para mantener el historial.
