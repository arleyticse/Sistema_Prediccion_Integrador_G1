package com.prediccion.apppredicciongm.gestion_inventario.producto.schemas;

/**
 * Ejemplos de payloads para la documentación Swagger de Productos
 */
public class ProductoExamples {
    
    // ==================== REQUEST EXAMPLES ====================
    
    public static final String CREAR_PRODUCTO_ALIMENTO = """
            {
              "nombre": "Arroz Premium 1kg",
              "categoriaId": 1,
              "unidadMedidaId": 1,
              "diasLeadTime": 5,
              "costoAdquisicion": 2.50,
              "costoMantenimiento": 0.10,
              "costoPedido": 15.00
            }
            """;
    
    public static final String CREAR_PRODUCTO_BEBIDA = """
            {
              "nombre": "Agua Mineral 500ml",
              "categoriaId": 2,
              "unidadMedidaId": 1,
              "diasLeadTime": 3,
              "costoAdquisicion": 0.80,
              "costoMantenimiento": 0.05,
              "costoPedido": 20.00
            }
            """;
    
    public static final String CREAR_PRODUCTO_LIMPIEZA = """
            {
              "nombre": "Detergente Líquido 1L",
              "categoriaId": 3,
              "unidadMedidaId": 2,
              "diasLeadTime": 7,
              "costoAdquisicion": 3.80,
              "costoMantenimiento": 0.15,
              "costoPedido": 25.00
            }
            """;
    
    public static final String CREAR_PRODUCTO_MEDICAMENTO = """
            {
              "nombre": "Paracetamol 500mg x 20 tabletas",
              "categoriaId": 4,
              "unidadMedidaId": 3,
              "diasLeadTime": 10,
              "costoAdquisicion": 5.50,
              "costoMantenimiento": 0.20,
              "costoPedido": 30.00
            }
            """;
    
    public static final String ACTUALIZAR_PRODUCTO = """
            {
              "nombre": "Arroz Premium 1kg (Actualizado)",
              "diasLeadTime": 4,
              "costoAdquisicion": 2.80,
              "costoMantenimiento": 0.12,
              "costoPedido": 18.00
            }
            """;
    
    // ==================== RESPONSE EXAMPLES ====================
    
    public static final String RESPONSE_PRODUCTO_CREADO = """
            {
              "productoId": 1,
              "nombre": "Arroz Premium 1kg",
              "categoriaNombre": "Alimentos",
              "unidadMedida": "Unidad",
              "diasLeadTime": 5,
              "costoAdquisicion": 2.50,
              "costoMantenimiento": 0.10,
              "costoMantenimientoAnual": 36.50,
              "costoPedido": 15.00,
              "fechaRegistro": "2025-10-14T10:30:00",
              "tieneInventario": false,
              "stockDisponible": 0
            }
            """;
    
    public static final String RESPONSE_PRODUCTO_CON_INVENTARIO = """
            {
              "productoId": 1,
              "nombre": "Arroz Premium 1kg",
              "categoriaNombre": "Alimentos",
              "unidadMedida": "Unidad",
              "diasLeadTime": 5,
              "costoAdquisicion": 2.50,
              "costoMantenimiento": 0.10,
              "costoMantenimientoAnual": 36.50,
              "costoPedido": 15.00,
              "fechaRegistro": "2025-10-14T10:30:00",
              "tieneInventario": true,
              "stockDisponible": 150,
              "stockMinimo": 50,
              "puntoReorden": 80,
              "estadoInventario": "NORMAL",
              "valorInventario": 375.00
            }
            """;
    
    public static final String RESPONSE_LISTA_PRODUCTOS = """
            {
              "content": [
                {
                  "productoId": 1,
                  "nombre": "Arroz Premium 1kg",
                  "categoriaNombre": "Alimentos",
                  "unidadMedida": "Unidad",
                  "costoAdquisicion": 2.50,
                  "stockDisponible": 150,
                  "estadoInventario": "NORMAL"
                },
                {
                  "productoId": 2,
                  "nombre": "Agua Mineral 500ml",
                  "categoriaNombre": "Bebidas",
                  "unidadMedida": "Unidad",
                  "costoAdquisicion": 0.80,
                  "stockDisponible": 25,
                  "estadoInventario": "BAJO"
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
    
    public static final String RESPONSE_PRODUCTO_ELIMINADO = """
            {
              "mensaje": "Producto eliminado exitosamente",
              "productoId": 1,
              "inventarioEliminado": true,
              "movimientosArchivados": 15
            }
            """;
    
    // ==================== ERROR EXAMPLES ====================
    
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 404,
              "error": "Not Found",
              "message": "Producto no encontrado con ID: 999",
              "path": "/api/productos/999"
            }
            """;
    
    public static final String ERROR_CATEGORIA_INVALIDA = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Categoría no encontrada con ID: 99",
              "path": "/api/productos"
            }
            """;
    
    public static final String ERROR_VALIDACION = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 400,
              "error": "Bad Request",
              "message": "Error de validación",
              "errors": [
                "El nombre del producto es obligatorio",
                "El costo de adquisición debe ser mayor a 0"
              ],
              "path": "/api/productos"
            }
            """;
    
    public static final String ERROR_PRODUCTO_CON_INVENTARIO = """
            {
              "timestamp": "2025-10-14T10:30:00",
              "status": 409,
              "error": "Conflict",
              "message": "No se puede eliminar el producto porque tiene inventario activo con stock disponible: 150 unidades",
              "path": "/api/productos/1"
            }
            """;
}
