package com.prediccion.apppredicciongm.gestion_inventario.movimiento.schemas;

/**
 * Ejemplos de payloads para la documentación Swagger de Movimientos (Kardex)
 */
public class KardexExamples {
    
    // ==================== REQUEST EXAMPLES ====================
    
    public static final String REGISTRAR_COMPRA = """
            {
              "productoId": 1,
              "tipoMovimiento": "COMPRA",
              "cantidad": 100,
              "precioUnitario": 2.50,
              "numeroDocumento": "FC-2025-001234",
              "proveedorId": 1,
              "usuarioId": 1,
              "lote": "L-2025-10-001",
              "fechaVencimiento": "2026-10-14",
              "observaciones": "Compra mensual de arroz"
            }
            """;
    
    public static final String REGISTRAR_VENTA = """
            {
              "productoId": 1,
              "tipoMovimiento": "VENTA",
              "cantidad": 25,
              "precioUnitario": 3.50,
              "numeroDocumento": "FV-2025-005678",
              "usuarioId": 2,
              "observaciones": "Venta al por menor"
            }
            """;
    
    public static final String REGISTRAR_AJUSTE_ENTRADA = """
            {
              "productoId": 2,
              "tipoMovimiento": "AJUSTE_ENTRADA",
              "cantidad": 15,
              "precioUnitario": 0.80,
              "numeroDocumento": "AJ-ENTRADA-001",
              "usuarioId": 1,
              "observaciones": "Ajuste por inventario físico - encontrado stock adicional en almacén secundario"
            }
            """;
    
    public static final String REGISTRAR_AJUSTE_SALIDA = """
            {
              "productoId": 2,
              "tipoMovimiento": "AJUSTE_SALIDA",
              "cantidad": 10,
              "precioUnitario": 0.80,
              "numeroDocumento": "AJ-SALIDA-002",
              "usuarioId": 1,
              "observaciones": "Ajuste por productos dañados durante transporte interno"
            }
            """;
    
    public static final String REGISTRAR_DEVOLUCION_PROVEEDOR = """
            {
              "productoId": 3,
              "tipoMovimiento": "DEVOLUCION_PROVEEDOR",
              "cantidad": 5,
              "precioUnitario": 3.80,
              "numeroDocumento": "DEV-PROV-001",
              "proveedorId": 2,
              "usuarioId": 1,
              "lote": "L-2025-09-045",
              "observaciones": "Devolución por productos defectuosos"
            }
            """;
    
    public static final String REGISTRAR_DEVOLUCION_CLIENTE = """
            {
              "productoId": 1,
              "tipoMovimiento": "DEVOLUCION_CLIENTE",
              "cantidad": 3,
              "precioUnitario": 3.50,
              "numeroDocumento": "DEV-CLI-025",
              "usuarioId": 2,
              "observaciones": "Devolución de cliente por error en pedido"
            }
            """;
    
    public static final String REGISTRAR_TRASLADO_ENTRADA = """
            {
              "productoId": 4,
              "tipoMovimiento": "TRASLADO_ENTRADA",
              "cantidad": 50,
              "precioUnitario": 5.50,
              "numeroDocumento": "TRA-ENT-010",
              "usuarioId": 1,
              "observaciones": "Traslado desde sucursal Centro"
            }
            """;
    
    public static final String REGISTRAR_TRASLADO_SALIDA = """
            {
              "productoId": 4,
              "tipoMovimiento": "TRASLADO_SALIDA",
              "cantidad": 30,
              "precioUnitario": 5.50,
              "numeroDocumento": "TRA-SAL-011",
              "usuarioId": 1,
              "observaciones": "Traslado hacia sucursal Norte"
            }
            """;
    
    public static final String REGISTRAR_MERMA = """
            {
              "productoId": 2,
              "tipoMovimiento": "MERMA",
              "cantidad": 8,
              "precioUnitario": 0.80,
              "numeroDocumento": "MERMA-015",
              "usuarioId": 1,
              "observaciones": "Merma por fecha de vencimiento próxima"
            }
            """;
    
    public static final String REGISTRAR_PRODUCCION = """
            {
              "productoId": 5,
              "tipoMovimiento": "PRODUCCION",
              "cantidad": 200,
              "precioUnitario": 1.20,
              "numeroDocumento": "PROD-2025-010",
              "usuarioId": 3,
              "lote": "L-PROD-2025-10-005",
              "fechaVencimiento": "2026-04-14",
              "observaciones": "Producción interna de producto terminado"
            }
            """;
    
    public static final String REGISTRAR_CONSUMO_INTERNO = """
            {
              "productoId": 3,
              "tipoMovimiento": "CONSUMO_INTERNO",
              "cantidad": 5,
              "precioUnitario": 3.80,
              "numeroDocumento": "CONS-INT-008",
              "usuarioId": 1,
              "observaciones": "Uso interno para limpieza de instalaciones"
            }
            """;
    
    // ==================== RESPONSE EXAMPLES ====================
    
    public static final String RESPONSE_MOVIMIENTO_COMPRA = """
            {
              "kardexId": 1,
              "productoId": 1,
              "nombreProducto": "Arroz Premium 1kg",
              "tipoMovimiento": "COMPRA",
              "cantidad": 100,
              "precioUnitario": 2.50,
              "valorTotal": 250.00,
              "numeroDocumento": "FC-2025-001234",
              "proveedorId": 1,
              "nombreProveedor": "Distribuidora San José S.A.",
              "usuarioId": 1,
              "nombreUsuario": "Juan Pérez",
              "lote": "L-2025-10-001",
              "fechaVencimiento": "2026-10-14",
              "saldoAnterior": 150,
              "saldoActual": 250,
              "fechaMovimiento": "2025-10-14T10:30:00",
              "observaciones": "Compra mensual de arroz"
            }
            """;
    
    public static final String RESPONSE_MOVIMIENTO_VENTA = """
            {
              "kardexId": 2,
              "productoId": 1,
              "nombreProducto": "Arroz Premium 1kg",
              "tipoMovimiento": "VENTA",
              "cantidad": 25,
              "precioUnitario": 3.50,
              "valorTotal": 87.50,
              "numeroDocumento": "FV-2025-005678",
              "usuarioId": 2,
              "nombreUsuario": "María González",
              "saldoAnterior": 250,
              "saldoActual": 225,
              "fechaMovimiento": "2025-10-14T11:45:00",
              "observaciones": "Venta al por menor"
            }
            """;
    
    public static final String RESPONSE_HISTORIAL_MOVIMIENTOS = """
            {
              "content": [
                {
                  "kardexId": 1,
                  "tipoMovimiento": "COMPRA",
                  "cantidad": 100,
                  "valorTotal": 250.00,
                  "saldoActual": 250,
                  "fechaMovimiento": "2025-10-14T10:30:00"
                },
                {
                  "kardexId": 2,
                  "tipoMovimiento": "VENTA",
                  "cantidad": 25,
                  "valorTotal": 87.50,
                  "saldoActual": 225,
                  "fechaMovimiento": "2025-10-14T11:45:00"
                }
              ],
              "pageable": {
                "pageNumber": 0,
                "pageSize": 20
              },
              "totalElements": 2,
              "totalPages": 1
            }
            """;
    
    public static final String RESPONSE_RESUMEN_MOVIMIENTOS = """
            {
              "totalMovimientos": 156,
              "totalEntradas": 95,
              "totalSalidas": 61,
              "cantidadTotalEntradas": 4850,
              "cantidadTotalSalidas": 3275,
              "valorTotalEntradas": 12450.50,
              "valorTotalSalidas": 11890.25,
              "saldoActual": 1575,
              "valorInventarioActual": 3938.75,
              "fechaUltimoMovimiento": "2025-10-14T11:45:00"
            }
            """;
    
    public static final String RESPONSE_HISTORIAL_PRECIOS = """
            {
              "productoId": 1,
              "nombreProducto": "Arroz Premium 1kg",
              "historialPrecios": [
                {
                  "fecha": "2025-09-14",
                  "precioCompra": 2.30,
                  "precioVenta": 3.20,
                  "cantidad": 100
                },
                {
                  "fecha": "2025-10-14",
                  "precioCompra": 2.50,
                  "precioVenta": 3.50,
                  "cantidad": 100
                }
              ],
              "precioPromedio": 2.40,
              "ultimoPrecio": 2.50
            }
            """;
    
    public static final String RESPONSE_PRODUCTOS_VENCIMIENTO_PROXIMO = """
            {
              "alertas": [
                {
                  "kardexId": 15,
                  "productoId": 4,
                  "nombreProducto": "Paracetamol 500mg",
                  "lote": "L-2025-03-015",
                  "cantidad": 50,
                  "fechaVencimiento": "2025-11-14",
                  "diasRestantes": 31,
                  "tipoAlerta": "PROXIMO_A_VENCER"
                },
                {
                  "kardexId": 22,
                  "productoId": 2,
                  "nombreProducto": "Agua Mineral 500ml",
                  "lote": "L-2025-08-089",
                  "cantidad": 120,
                  "fechaVencimiento": "2025-10-30",
                  "diasRestantes": 16,
                  "tipoAlerta": "VENCE_PRONTO"
                }
              ]
            }
            """;
    
    // ==================== ERROR EXAMPLES ====================
    
    public static final String ERROR_MOVIMIENTO_NO_ENCONTRADO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Movimiento no encontrado con ID: 999",
              "path": "/api/movimientos/999"
            }
            """;
    
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Producto no encontrado con ID: 99",
              "path": "/api/movimientos"
            }
            """;
    
    public static final String ERROR_STOCK_INSUFICIENTE_MOVIMIENTO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Stock insuficiente para realizar el movimiento. Stock actual: 15, Cantidad solicitada: 25",
              "productoId": 2,
              "stockDisponible": 15,
              "cantidadSolicitada": 25,
              "path": "/api/movimientos"
            }
            """;
    
    public static final String ERROR_TIPO_MOVIMIENTO_INVALIDO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Tipo de movimiento inválido: TIPO_INVALIDO",
              "tiposPermitidos": ["COMPRA", "VENTA", "AJUSTE_ENTRADA", "AJUSTE_SALIDA", "DEVOLUCION_PROVEEDOR", "DEVOLUCION_CLIENTE", "TRASLADO_ENTRADA", "TRASLADO_SALIDA", "MERMA", "PRODUCCION", "CONSUMO_INTERNO"],
              "path": "/api/movimientos"
            }
            """;
}
