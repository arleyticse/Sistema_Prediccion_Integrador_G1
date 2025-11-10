package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para crear una alerta de inventario manualmente.
 * 
 * Permite a los usuarios crear alertas personalizadas ademas de las
 * generadas automaticamente por el sistema.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearAlertaRequest {

    /**
     * Tipo de alerta a crear.
     * Campo obligatorio.
     */
    @NotNull(message = "El tipo de alerta es obligatorio")
    private TipoAlerta tipoAlerta;

    /**
     * Nivel de criticidad de la alerta.
     * Campo obligatorio.
     */
    @NotNull(message = "El nivel de criticidad es obligatorio")
    private NivelCriticidad nivelCriticidad;

    /**
     * ID del producto asociado.
     * Campo obligatorio.
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    /**
     * Mensaje descriptivo de la alerta.
     * Campo obligatorio.
     */
    @NotNull(message = "El mensaje es obligatorio")
    private String mensaje;

    /**
     * Stock actual del producto.
     * Campo opcional.
     */
    private Integer stockActual;

    /**
     * Stock minimo del producto.
     * Campo opcional.
     */
    private Integer stockMinimo;

    /**
     * Cantidad sugerida para reorden.
     * Campo opcional.
     */
    private Integer cantidadSugerida;

    /**
     * ID del usuario a quien asignar la alerta.
     * Campo opcional.
     */
    private Integer usuarioAsignadoId;

    /**
     * Observaciones adicionales.
     * Campo opcional.
     */
    private String observaciones;
}
