package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors;

/**
 * Excepcion lanzada cuando se intenta modificar una alerta ya resuelta.
 * 
 * Se utiliza para prevenir cambios en alertas que ya han sido
 * procesadas y marcadas como resueltas.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public class AlertaYaResueltaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con ID de alerta.
     * 
     * @param alertaId ID de la alerta ya resuelta
     */
    public AlertaYaResueltaException(Long alertaId) {
        super("La alerta con ID " + alertaId + " ya ha sido resuelta y no puede modificarse");
    }

    /**
     * Constructor con mensaje personalizado.
     * 
     * @param mensaje Mensaje descriptivo del error
     */
    public AlertaYaResueltaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Mensaje descriptivo del error
     * @param causa Causa raiz de la excepcion
     */
    public AlertaYaResueltaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
