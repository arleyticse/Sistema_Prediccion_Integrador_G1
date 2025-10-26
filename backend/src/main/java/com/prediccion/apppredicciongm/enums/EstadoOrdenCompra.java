package com.prediccion.apppredicciongm.enums;

/**
 * Estados posibles de una orden de compra
 */
public enum EstadoOrdenCompra {
    BORRADOR("Borrador"),
    PENDIENTE("Pendiente de Aprobación"),
    APROBADA("Aprobada"),
    ENVIADA("Enviada al Proveedor"),
    EN_TRANSITO("En Tránsito"),
    RECIBIDA_PARCIAL("Recibida Parcialmente"),
    RECIBIDA_COMPLETA("Recibida Completamente"),
    CANCELADA("Cancelada"),
    RECHAZADA("Rechazada");

    private final String descripcion;

    EstadoOrdenCompra(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
