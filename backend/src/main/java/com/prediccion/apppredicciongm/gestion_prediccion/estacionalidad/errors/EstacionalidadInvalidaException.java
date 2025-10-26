package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors;

/**
 * Excepción lanzada cuando los parámetros de validación de estacionalidad son inválidos.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
public class EstacionalidadInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstacionalidadInvalidaException(String mensaje) {
        super(mensaje);
    }

    public EstacionalidadInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
