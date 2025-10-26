package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors;

/**
 * Excepción lanzada cuando existe un conflicto al crear un patrón estacional.
 * Por ejemplo, cuando ya existe un patrón para un producto y mes específico.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
public class EstacionalidadYaExisteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstacionalidadYaExisteException(String mensaje) {
        super(mensaje);
    }

    public EstacionalidadYaExisteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
