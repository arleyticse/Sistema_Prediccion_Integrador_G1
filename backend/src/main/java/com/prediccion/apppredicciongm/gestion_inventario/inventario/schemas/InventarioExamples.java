package com.prediccion.apppredicciongm.gestion_inventario.inventario.schemas;

/**
 * Ejemplos de payloads para la documentación Swagger de Inventario
 */
public class InventarioExamples {
    
    // ==================== REQUEST EXAMPLES ====================
    
    public static final String CREAR_INVENTARIO_BASICO = """
            {
              "productoId": 1,
              "stockDisponible": 150,
              "stockReservado": 0,
              "stockEnTransito": 0,
              "stockMinimo": 50,
              "stockMaximo": 500,
              "puntoReorden": 80,
              "ubicacionAlmacen": "A-01-05",
              "observaciones": "Inventario inicial"
            }
            """;
    
    public static final String CREAR_INVENTARIO_CON_STOCK = """
            {
              "productoId": 2,
              "stockDisponible": 200,
              "stockReservado": 25,
              "stockEnTransito": 50,
              "stockMinimo": 100,
              "stockMaximo": 800,
              "puntoReorden": 150,
              "ubicacionAlmacen": "B-02-10",
              "observaciones": "Producto de alta rotación"
            }
            """;
    
    public static final String ACTUALIZAR_INVENTARIO = """
            {
              "stockMinimo": 60,
              "stockMaximo": 600,
              "puntoReorden": 100,
              "ubicacionAlmacen": "A-01-08",
              "observaciones": "Ubicación actualizada por reorganización"
            }
            """;
    
    public static final String AJUSTE_STOCK_ENTRADA = """
            {
              "inventarioId": 1,
              "cantidad": 50,
              "motivo": "Ajuste por inventario físico - encontrado stock adicional",
              "usuarioId": 1
            }
            """;
    
    public static final String AJUSTE_STOCK_SALIDA = """
            {
              "inventarioId": 1,
              "cantidad": -10,
              "motivo": "Ajuste por producto dañado",
              "usuarioId": 1
            }
            """;
    
    // ==================== RESPONSE EXAMPLES ====================
    
    public static final String RESPONSE_INVENTARIO_NORMAL = """
            {
              "inventarioId": 1,
              "productoId": 1,
              "nombreProducto": "Arroz Premium 1kg",
              "categoriaNombre": "Alimentos",
              "unidadMedida": "Unidad",
              "stockDisponible": 150,
              "stockReservado": 0,
              "stockEnTransito": 0,
              "stockTotal": 150,
              "stockMinimo": 50,
              "stockMaximo": 500,
              "puntoReorden": 80,
              "necesitaReorden": false,
              "bajoPuntoMinimo": false,
              "ubicacionAlmacen": "A-01-05",
              "fechaUltimoMovimiento": "2025-10-14T09:30:00",
              "fechaUltimaActualizacion": "2025-10-14T10:15:00",
              "diasSinVenta": 2,
              "estado": "NORMAL",
              "observaciones": "Inventario inicial"
            }
            """;
    
    public static final String RESPONSE_INVENTARIO_BAJO_STOCK = """
            {
              "inventarioId": 2,
              "productoId": 2,
              "nombreProducto": "Agua Mineral 500ml",
              "categoriaNombre": "Bebidas",
              "unidadMedida": "Unidad",
              "stockDisponible": 30,
              "stockReservado": 5,
              "stockEnTransito": 100,
              "stockTotal": 135,
              "stockMinimo": 50,
              "stockMaximo": 300,
              "puntoReorden": 80,
              "necesitaReorden": true,
              "bajoPuntoMinimo": true,
              "ubicacionAlmacen": "C-03-12",
              "fechaUltimoMovimiento": "2025-10-13T16:45:00",
              "fechaUltimaActualizacion": "2025-10-14T08:20:00",
              "diasSinVenta": 0,
              "estado": "BAJO",
              "observaciones": "Requiere pedido urgente"
            }
            """;
    
    public static final String RESPONSE_ALERTA_CRITICA = """
            {
              "inventarioId": 3,
              "productoId": 3,
              "nombreProducto": "Detergente Líquido 1L",
              "categoriaNombre": "Limpieza",
              "stockDisponible": 5,
              "stockMinimo": 20,
              "puntoReorden": 30,
              "tipoAlerta": "CRITICO",
              "mensaje": "Stock por debajo del mínimo (20 unidades)",
              "fechaUltimoMovimiento": "2025-10-12T14:30:00",
              "diasSinMovimiento": 0
            }
            """;
    
    public static final String RESPONSE_RESUMEN_STOCK = """
            {
              "totalProductos": 45,
              "productosActivos": 38,
              "productosInactivos": 7,
              "productosConStockBajo": 12,
              "productosAgotados": 3,
              "productosSinMovimiento": 5,
              "valorTotalInventario": 125750.50,
              "stockTotalDisponible": 8450
            }
            """;
    
    // ==================== ERROR EXAMPLES ====================
    
    public static final String ERROR_INVENTARIO_NO_ENCONTRADO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Inventario no encontrado con ID: 999",
              "path": "/api/inventario/999"
            }
            """;
    
    public static final String ERROR_INVENTARIO_YA_EXISTE = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "Ya existe un inventario para el producto con ID: 1",
              "productoId": 1,
              "path": "/api/inventario"
            }
            """;
    
    public static final String ERROR_STOCK_INSUFICIENTE = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Stock insuficiente. Stock actual: 10, Cantidad solicitada: 25",
              "productoId": 1,
              "stockDisponible": 10,
              "cantidadSolicitada": 25,
              "path": "/api/inventario/ajustar-stock"
            }
            """;
}
