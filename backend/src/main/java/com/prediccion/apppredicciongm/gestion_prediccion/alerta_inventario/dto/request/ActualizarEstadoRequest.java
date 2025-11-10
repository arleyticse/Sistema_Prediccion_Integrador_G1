package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para actualizar el estado de una alerta.
 * 
 * Permite cambiar el estado de una alerta existente, por ejemplo,
 * marcarla como resuelta, ignorada o en proceso.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoRequest {

    /**
     * Nuevo estado para la alerta.
     * Campo obligatorio.
     */
    @NotNull(message = "El estado es obligatorio")
    private EstadoAlerta nuevoEstado;

    /**
     * Observaciones adicionales sobre el cambio de estado.
     * Campo opcional.
     */
    private String observaciones;

    /**
     * Accion tomada para resolver la alerta.
     * Requerido cuando el estado es RESUELTA.
     */
    private String accionTomada;

    /**
     * ID del usuario que realiza el cambio.
     * Campo opcional, se obtiene del contexto de seguridad si no se proporciona.
     */
    private Integer usuarioId;
}
