package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors;

/**
 * Excepcion lanzada cuando no se encuentra una alerta de inventario.
 * 
 * Se utiliza cuando se intenta acceder a una alerta que no existe
 * en la base de datos mediante su ID.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public class AlertaInventarioNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con ID de alerta.
     * 
     * @param alertaId ID de la alerta no encontrada
     */
    public AlertaInventarioNoEncontradaException(Long alertaId) {
        super("Alerta de inventario no encontrada con ID: " + alertaId);
    }

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param mensaje Mensaje descriptivo del error
     */
    public AlertaInventarioNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Mensaje descriptivo del error
     * @param causa Causa raiz de la excepcion
     */
    public AlertaInventarioNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
