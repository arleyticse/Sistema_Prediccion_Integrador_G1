package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para marcar múltiples alertas como en proceso en un solo request.
 * 
 * Utilizado cuando el usuario selecciona varias alertas desde el dashboard
 * para comenzar a atenderlas.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcarEnProcesoRequest {

    /**
     * Lista de IDs de alertas a marcar como EN_PROCESO.
     */
    @NotEmpty(message = "Debe proporcionar al menos una alerta")
    private List<Long> alertaIds;

    /**
     * ID del usuario que tomará responsabilidad de las alertas.
     */
    @NotNull(message = "El usuario asignado es obligatorio")
    private Integer usuarioId;

    /**
     * Observaciones sobre el inicio del proceso.
     */
    private String observaciones;
}
