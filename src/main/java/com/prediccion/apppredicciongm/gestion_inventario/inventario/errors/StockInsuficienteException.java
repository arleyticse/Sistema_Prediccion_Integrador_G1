package com.prediccion.apppredicciongm.gestion_inventario.inventario.errors;

import lombok.Getter;

@Getter
public class StockInsuficienteException extends RuntimeException {
    private final Integer productoId;
    private final Integer stockDisponible;
    private final Integer cantidadSolicitada;
    
    public StockInsuficienteException(Integer productoId, Integer stockDisponible, Integer cantidadSolicitada) {
        super(String.format("Stock insuficiente. Producto ID: %d, Stock disponible: %d, Cantidad solicitada: %d", 
                productoId, stockDisponible, cantidadSolicitada));
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }
    
    public StockInsuficienteException(String message) {
        super(message);
        this.productoId = null;
        this.stockDisponible = null;
        this.cantidadSolicitada = null;
    }
}
