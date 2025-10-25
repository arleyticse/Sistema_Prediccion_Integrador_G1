package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors;

/**
 * Excepción lanzada cuando no se encuentra un producto.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public ProductoNoEncontradoException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
