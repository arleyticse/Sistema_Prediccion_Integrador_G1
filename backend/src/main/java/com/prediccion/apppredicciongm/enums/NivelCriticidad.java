package com.prediccion.apppredicciongm.enums;

/**
 * Niveles de criticidad para alertas
 */
public enum NivelCriticidad {
    BAJA("Baja - Informativo"),
    MEDIA("Media - Requiere atención"),
    ALTA("Alta - Acción inmediata recomendada"),
    CRITICA("Crítica - Acción urgente requerida");

    private final String descripcion;

    NivelCriticidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
