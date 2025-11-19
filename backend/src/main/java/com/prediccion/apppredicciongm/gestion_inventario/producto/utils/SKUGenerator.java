package com.prediccion.apppredicciongm.gestion_inventario.producto.utils;

import com.prediccion.apppredicciongm.models.Inventario.Producto;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para generación dinámica de códigos SKU.
 * 
 * Los SKU se generan al vuelo usando la fórmula:
 * [INICIAL_CATEGORIA][0000][ID_PRODUCTO]
 * 
 * Ejemplos:
 * - Producto ID 1, Categoría "Alimentos" → A0001
 * - Producto ID 25, Categoría "Bebidas" → B0025
 * - Producto ID 123, Categoría "Limpieza" → L0123
 */
@Slf4j
public class SKUGenerator {

    private SKUGenerator() {
        // Utility class - no instances allowed
    }

    /**
     * Genera código SKU dinámicamente para un producto.
     * 
     * Formato: [INICIAL_CATEGORIA][PADDING][ID]
     * - Primera letra de la categoría en mayúscula
     * - Padding de 4 dígitos con ceros
     * - ID del producto
     * 
     * @param producto Producto del cual generar SKU
     * @return Código SKU generado (ej: "A0001")
     */
    public static String generarSKU(Producto producto) {
        if (producto == null || producto.getProductoId() == null) {
            log.warn("Producto null o sin ID, retornando SKU genérico");
            return "P0000";
        }

        String inicialCategoria = obtenerInicialCategoria(producto);
        String idFormateado = String.format("%04d", producto.getProductoId());
        
        return inicialCategoria + idFormateado;
    }

    /**
     * Genera código SKU usando ID y nombre de categoría directamente.
     * 
     * @param productoId ID del producto
     * @param nombreCategoria Nombre de la categoría
     * @return Código SKU generado
     */
    public static String generarSKU(Integer productoId, String nombreCategoria) {
        if (productoId == null) {
            return "P0000";
        }

        String inicial = obtenerInicialCategoria(nombreCategoria);
        String idFormateado = String.format("%04d", productoId);
        
        return inicial + idFormateado;
    }

    /**
     * Obtiene la inicial de la categoría del producto.
     * Si no tiene categoría, usa 'P' (Producto genérico).
     * 
     * @param producto Producto del cual obtener la inicial
     * @return Inicial de la categoría en mayúscula
     */
    private static String obtenerInicialCategoria(Producto producto) {
        if (producto.getCategoria() != null && 
            producto.getCategoria().getNombre() != null && 
            !producto.getCategoria().getNombre().trim().isEmpty()) {
            
            String nombreCategoria = producto.getCategoria().getNombre().trim();
            return nombreCategoria.substring(0, 1).toUpperCase();
        }

        return "P"; // Producto genérico si no tiene categoría
    }

    /**
     * Obtiene la inicial del nombre de categoría.
     * Si es null o vacío, usa 'P'.
     * 
     * @param nombreCategoria Nombre de la categoría
     * @return Inicial en mayúscula
     */
    private static String obtenerInicialCategoria(String nombreCategoria) {
        if (nombreCategoria != null && !nombreCategoria.trim().isEmpty()) {
            return nombreCategoria.trim().substring(0, 1).toUpperCase();
        }
        return "P";
    }

    /**
     * Valida si un SKU tiene el formato correcto.
     * 
     * Formato esperado: [LETRA][4 DÍGITOS]
     * Ejemplo: A0001, B0025, L0123
     * 
     * @param sku Código SKU a validar
     * @return true si el formato es válido
     */
    public static boolean validarFormatoSKU(String sku) {
        if (sku == null || sku.length() != 5) {
            return false;
        }

        // Primera letra debe ser alfabética
        if (!Character.isLetter(sku.charAt(0))) {
            return false;
        }

        // Los siguientes 4 caracteres deben ser dígitos
        String digitosParte = sku.substring(1);
        return digitosParte.matches("\\d{4}");
    }
}
