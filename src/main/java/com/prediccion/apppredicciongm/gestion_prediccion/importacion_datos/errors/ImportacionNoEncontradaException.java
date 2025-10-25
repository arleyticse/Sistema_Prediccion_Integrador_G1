package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.errors;

/**
 * Excepción cuando una importación no es encontrada
 */
public class ImportacionNoEncontradaException extends RuntimeException {
    public ImportacionNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public ImportacionNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
