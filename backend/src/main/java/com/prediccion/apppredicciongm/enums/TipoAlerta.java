package com.prediccion.apppredicciongm.enums;

/**
 * Tipos de alertas del sistema
 */
public enum TipoAlerta {
    STOCK_BAJO("Stock por debajo del mínimo"),
    PUNTO_REORDEN("Producto alcanzó punto de reorden"),
    STOCK_CRITICO("Stock crítico - agotamiento inminente"),
    SOBRESTOCK("Exceso de inventario"),
    PRODUCTO_OBSOLETO("Producto sin movimiento prolongado"),
    VENCIMIENTO_PROXIMO("Producto próximo a vencer"),
    VENCIMIENTO_VENCIDO("Producto vencido"),
    DEMANDA_ANOMALA("Demanda anormalmente alta detectada"),
    COSTO_ELEVADO("Costo de mantenimiento elevado"),
    MERMA_ALTA("Nivel de merma superior al normal"),
    PROVEEDOR_RETRASO("Proveedor con retraso en entrega");

    private final String descripcion;

    TipoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
