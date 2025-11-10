package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors;

/**
 * Excepcion lanzada cuando ocurre un error durante el procesamiento en lote.
 * 
 * Se utiliza cuando falla el procesamiento de multiples alertas,
 * incluyendo predicciones, optimizaciones o generacion de ordenes.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public class ErrorProcesamientoLoteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje de error.
     * 
     * @param mensaje Descripcion del error ocurrido
     */
    public ErrorProcesamientoLoteException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Descripcion del error ocurrido
     * @param causa Causa raiz de la excepcion
     */
    public ErrorProcesamientoLoteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
