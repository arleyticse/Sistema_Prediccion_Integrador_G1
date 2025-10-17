package com.prediccion.apppredicciongm.gestion_inventario.inventario.errors;

import lombok.Getter;

@Getter
public class InventarioNotFoundException extends RuntimeException {
    private final Integer inventarioId;
    
    public InventarioNotFoundException(Integer inventarioId) {
        super("Inventario no encontrado con ID: " + inventarioId);
        this.inventarioId = inventarioId;
    }
    
    public InventarioNotFoundException(String message) {
        super(message);
        this.inventarioId = null;
    }
}
