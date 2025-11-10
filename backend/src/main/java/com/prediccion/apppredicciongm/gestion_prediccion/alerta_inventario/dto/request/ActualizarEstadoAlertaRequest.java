package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el estado de una alerta de inventario.
 * 
 * Permite transiciones de estado como:
 * - PENDIENTE → EN_PROCESO
 * - EN_PROCESO → RESUELTA
 * - PENDIENTE → IGNORADA
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoAlertaRequest {

    /**
     * Nuevo estado de la alerta.
     * Valores permitidos: PENDIENTE, EN_PROCESO, RESUELTA, IGNORADA, ESCALADA
     */
    @NotNull(message = "El estado es obligatorio")
    private EstadoAlerta nuevoEstado;

    /**
     * Observaciones sobre el cambio de estado.
     * Ejemplo: "Orden de compra OC-2025-001 generada"
     */
    private String observaciones;

    /**
     * ID del usuario que realiza el cambio (opcional).
     * Si se proporciona, se registrará como responsable.
     */
    private Integer usuarioId;
}
