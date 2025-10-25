package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors;

/**
 * Excepción lanzada cuando una orden de compra no es encontrada.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class OrdenCompraNoEncontradaException extends RuntimeException {
    
    public OrdenCompraNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public OrdenCompraNoEncontradaException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
