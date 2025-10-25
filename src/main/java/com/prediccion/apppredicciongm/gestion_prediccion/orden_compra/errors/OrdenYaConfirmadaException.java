package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors;

/**
 * Excepción lanzada cuando se intenta cambiar el estado de una orden que ya ha sido confirmada.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public class OrdenYaConfirmadaException extends RuntimeException {
    
    public OrdenYaConfirmadaException(String mensaje) {
        super(mensaje);
    }

    public OrdenYaConfirmadaException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
