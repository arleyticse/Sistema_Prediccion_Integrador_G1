package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums;

/**
 * Enumeracion que define los tipos de alertas de inventario disponibles en el sistema.
 * Cada tipo representa una condicion especifica que requiere atencion del usuario.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public enum TipoAlerta {
    
    /**
     * Alerta cuando el stock esta por debajo del minimo configurado.
     */
    STOCK_BAJO,
    
    /**
     * Alerta cuando el stock alcanza el punto de reorden calculado.
     */
    PUNTO_REORDEN,
    
    /**
     * Alerta cuando el stock esta en nivel critico que requiere atencion inmediata.
     */
    STOCK_CRITICO,
    
    /**
     * Alerta cuando hay exceso de inventario respecto al stock maximo.
     */
    SOBRESTOCK,
    
    /**
     * Alerta cuando un producto no tiene movimiento por tiempo prolongado.
     */
    PRODUCTO_OBSOLETO,
    
    /**
     * Alerta cuando productos perecederos estan proximos a vencer.
     */
    VENCIMIENTO_PROXIMO,
    
    /**
     * Alerta cuando productos ya han vencido.
     */
    VENCIMIENTO_VENCIDO,
    
    /**
     * Alerta cuando se detectan patrones anomalos en la demanda.
     */
    DEMANDA_ANOMALA,
    
    /**
     * Alerta cuando el costo del producto se eleva significativamente.
     */
    COSTO_ELEVADO,
    
    /**
     * Alerta cuando hay alta merma o perdida del producto.
     */
    MERMA_ALTA,
    
    /**
     * Alerta cuando un proveedor tiene retrasos recurrentes en entregas.
     */
    PROVEEDOR_RETRASO
}
