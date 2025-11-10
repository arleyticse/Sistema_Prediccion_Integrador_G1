package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con informacion basica de un producto.
 * 
 * Utilizado en respuestas de alertas de inventario para proporcionar
 * informacion del producto sin cargar toda su estructura.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoBasicoDTO {

    /**
     * ID unico del producto.
     */
    private Integer productoId;

    /**
     * Nombre del producto.
     */
    private String nombre;

    /**
     * Categoria del producto.
     */
    private String categoria;

    /**
     * Unidad de medida del producto.
     */
    private String unidadMedida;

    /**
     * Informacion del proveedor principal.
     */
    private ProveedorBasicoDTO proveedor;
}
