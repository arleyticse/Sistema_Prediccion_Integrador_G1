package com.prediccion.apppredicciongm.enums;

/**
 * Estados de una importación de datos
 */
public enum EstadoImportacion {
    EN_PROCESO("En Proceso"),
    COMPLETADA("Completada"),
    COMPLETADA_CON_ERRORES("Completada con Errores"),
    FALLIDA("Fallida"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoImportacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
