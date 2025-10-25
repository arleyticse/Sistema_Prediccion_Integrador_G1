package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors;

/**
 * Excepción lanzada cuando no se encuentra una predicción.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class PrediccionNoEncontradaException extends RuntimeException {

    public PrediccionNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public PrediccionNoEncontradaException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
