package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors;

/**
 * Excepción lanzada cuando no hay datos suficientes para generar predicción.
 * Se requiere mínimo 12 registros históricos.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class DatosInsuficientesException extends RuntimeException {

    public DatosInsuficientesException(String mensaje) {
        super(mensaje);
    }

    public DatosInsuficientesException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
