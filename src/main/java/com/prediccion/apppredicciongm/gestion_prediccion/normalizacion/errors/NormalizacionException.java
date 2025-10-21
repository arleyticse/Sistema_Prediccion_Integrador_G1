package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.errors;

/**
 * Excepción personalizada para errores en el proceso de normalización de demanda.
 * Se lanza cuando hay problemas al procesar datos de kardex o crear registros de demanda.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
public class NormalizacionException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje descripción del error
     */
    public NormalizacionException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje de error y causa.
     *
     * @param mensaje descripción del error
     * @param causa excepción que causó este error
     */
    public NormalizacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Constructor con causa.
     *
     * @param causa excepción que causó este error
     */
    public NormalizacionException(Throwable causa) {
        super(causa);
    }
}
