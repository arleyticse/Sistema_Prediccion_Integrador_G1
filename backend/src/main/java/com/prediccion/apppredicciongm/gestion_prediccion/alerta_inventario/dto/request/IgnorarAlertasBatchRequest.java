package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para ignorar múltiples alertas en un solo request.
 * 
 * Utilizado cuando las alertas no requieren acción o son falsas alarmas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IgnorarAlertasBatchRequest {

    /**
     * Lista de IDs de alertas a marcar como IGNORADA.
     */
    @NotEmpty(message = "Debe proporcionar al menos una alerta")
    private List<Long> alertaIds;

    /**
     * Motivo por el cual se ignoran las alertas.
     * Ejemplo: "Stock suficiente en bodega secundaria"
     */
    private String motivo;

    /**
     * ID del usuario que ignora las alertas (opcional).
     */
    private Integer usuarioId;
}
