package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors;

/**
 * Excepción lanzada cuando un producto no tiene proveedor asignado.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class ProductoSinProveedorException extends RuntimeException {
    
    public ProductoSinProveedorException(String mensaje) {
        super(mensaje);
    }

    public ProductoSinProveedorException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
