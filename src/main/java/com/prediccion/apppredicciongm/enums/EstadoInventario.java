package com.prediccion.apppredicciongm.enums;

/**
 * Estados posibles del inventario
 */
public enum EstadoInventario {
    NORMAL("Normal - Stock adecuado"),
    BAJO("Bajo - Requiere reorden"),
    CRITICO("Crítico - Stock mínimo"),
    EXCESO("Exceso - Sobrestock"),
    OBSOLETO("Obsoleto - Sin movimiento"),
    BLOQUEADO("Bloqueado - No disponible");

    private final String descripcion;

    EstadoInventario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
