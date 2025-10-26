package com.prediccion.apppredicciongm.enums;

public enum TipoMovimiento {
    ENTRADA_COMPRA("Entrada por Compra"),
    ENTRADA_DEVOLUCION("Entrada por Devolución"),
    ENTRADA_AJUSTE("Entrada por Ajuste de Inventario"),
    ENTRADA_TRANSFERENCIA("Entrada por Transferencia"),
    ENTRADA_PRODUCCION("Entrada por Producción"),
    ENTRADA_INICIAL("Entrada - Inventario Inicial"),
    
    SALIDA_VENTA("Salida por Venta"),
    SALIDA_DEVOLUCION("Salida por Devolución a Proveedor"),
    SALIDA_AJUSTE("Salida por Ajuste de Inventario"),
    SALIDA_TRANSFERENCIA("Salida por Transferencia"),
    SALIDA_MERMA("Salida por Merma"),
    SALIDA_VENCIMIENTO("Salida por Vencimiento"),
    SALIDA_CONSUMO("Salida por Consumo Interno"),
    
    AJUSTE_POSITIVO("Ajuste Positivo"),
    AJUSTE_NEGATIVO("Ajuste Negativo");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEntrada() {
        return this.name().startsWith("ENTRADA") || this == AJUSTE_POSITIVO;
    }

    public boolean esSalida() {
        return this.name().startsWith("SALIDA") || this == AJUSTE_NEGATIVO;
    }
}
