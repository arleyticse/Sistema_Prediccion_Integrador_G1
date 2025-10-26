package com.prediccion.apppredicciongm.enums;

/**
 * Estados de una alerta
 */
public enum EstadoAlerta {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    RESUELTA("Resuelta"),
    IGNORADA("Ignorada"),
    ESCALADA("Escalada");

    private final String descripcion;

    EstadoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
