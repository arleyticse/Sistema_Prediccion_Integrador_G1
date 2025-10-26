package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.errors;

/**
 * Excepción cuando un cálculo de optimización no es encontrado
 */
public class CalculoObtimizacionNoEncontradoException extends RuntimeException {
    public CalculoObtimizacionNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public CalculoObtimizacionNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
