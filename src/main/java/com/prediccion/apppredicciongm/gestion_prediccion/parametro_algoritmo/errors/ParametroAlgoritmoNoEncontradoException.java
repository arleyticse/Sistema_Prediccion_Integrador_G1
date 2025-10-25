package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.errors;

/**
 * Excepción cuando un parámetro de algoritmo no es encontrado
 */
public class ParametroAlgoritmoNoEncontradoException extends RuntimeException {
    public ParametroAlgoritmoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public ParametroAlgoritmoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
