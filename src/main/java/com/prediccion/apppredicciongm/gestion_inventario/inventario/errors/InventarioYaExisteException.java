package com.prediccion.apppredicciongm.gestion_inventario.inventario.errors;

import lombok.Getter;

@Getter
public class InventarioYaExisteException extends RuntimeException {
    private final Integer productoId;
    
    public InventarioYaExisteException(Integer productoId) {
        super("Ya existe un inventario para el producto con ID: " + productoId);
        this.productoId = productoId;
    }
}
