package com.prediccion.apppredicciongm.enums;

/**
 * Tipos de datos que se pueden importar
 */
public enum TipoDatosImportacion {
    PRODUCTOS("Productos"),
    INVENTARIO("Inventario"),
    KARDEX("Kardex/Cardex"),
    DEMANDA("Registro de Demanda"),
    PROVEEDORES("Proveedores"),
    VENTAS("Ventas"),
    COMPRAS("Compras"),
    CATEGORIAS("Categorías"),
    ESTACIONALIDAD("Estacionalidad");

    private final String descripcion;

    TipoDatosImportacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
