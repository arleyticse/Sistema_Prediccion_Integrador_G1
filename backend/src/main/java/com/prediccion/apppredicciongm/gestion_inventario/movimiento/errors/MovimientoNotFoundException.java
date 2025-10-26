package com.prediccion.apppredicciongm.gestion_inventario.movimiento.errors;

import lombok.Getter;

@Getter
public class MovimientoNotFoundException extends RuntimeException {
    private final Long kardexId;
    
    public MovimientoNotFoundException(Long kardexId) {
        super("Movimiento no encontrado con ID: " + kardexId);
        this.kardexId = kardexId;
    }
    
    public MovimientoNotFoundException(String message) {
        super(message);
        this.kardexId = null;
    }
}
