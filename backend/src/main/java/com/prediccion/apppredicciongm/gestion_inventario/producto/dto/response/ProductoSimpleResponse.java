package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ligero para listados de productos en dropdowns y selects.
 * Contiene solo los campos esenciales para optimizar rendimiento.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoSimpleResponse {
    
    private Integer productoId;
    private String nombre;
    private String nombreCategoria;
}
