package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con informacion basica de un proveedor.
 * 
 * Utilizado para incluir datos del proveedor en respuestas de alertas
 * sin cargar toda la estructura del proveedor.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorBasicoDTO {

    /**
     * ID unico del proveedor.
     */
    private Integer proveedorId;

    /**
     * Nombre comercial del proveedor.
     */
    private String nombreComercial;

    /**
     * Tiempo de entrega en dias del proveedor.
     */
    private Integer tiempoEntregaDias;

    /**
     * Contacto principal del proveedor.
     */
    private String contacto;

    /**
     * Telefono del proveedor.
     */
    private String telefono;
}
