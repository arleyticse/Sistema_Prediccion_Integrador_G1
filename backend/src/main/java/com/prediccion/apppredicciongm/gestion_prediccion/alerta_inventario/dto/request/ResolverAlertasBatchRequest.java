package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para resolver múltiples alertas en un solo request.
 * 
 * Utilizado después de generar órdenes de compra automáticas
 * o realizar acciones correctivas sobre las alertas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolverAlertasBatchRequest {

    /**
     * Lista de IDs de alertas a marcar como RESUELTA.
     */
    @NotEmpty(message = "Debe proporcionar al menos una alerta")
    private List<Long> alertaIds;

    /**
     * Descripción de la acción tomada para resolver las alertas.
     * Ejemplo: "Órdenes de compra generadas automáticamente"
     */
    private String accionTomada;

    /**
     * ID del usuario que resolvió las alertas (opcional).
     */
    private Integer usuarioId;
}
