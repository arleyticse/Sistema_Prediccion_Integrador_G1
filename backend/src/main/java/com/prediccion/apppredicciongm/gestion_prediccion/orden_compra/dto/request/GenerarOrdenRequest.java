package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la solicitud de generación automática de orden de compra.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarOrdenRequest {
    
    /**
     * ID de la predicción que sirve de base para generar la orden.
     * Campo requerido.
     */
    private Integer prediccionId;
    
    /**
     * Buffer adicional a la cantidad calculada (opcional).
     * Si se proporciona, se suma a la cantidad calculada por la fórmula.
     */
    private Integer cantidadAdicional;
    
    /**
     * Notas o observaciones especiales para la orden (opcional).
     * Máximo 500 caracteres.
     */
    private String notasEspeciales;
    
    /**
     * Fecha de entrega deseada (opcional).
     * Si no se proporciona, se usa la fecha de entrega del proveedor.
     */
    private LocalDate fechaEntregaDeseada;
}
