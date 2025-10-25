package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors;

/**
 * Excepción lanzada cuando un patrón estacional no es encontrado.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
public class EstacionalidadNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstacionalidadNotFoundException(String mensaje) {
        super(mensaje);
    }

    public EstacionalidadNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
