package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de solicitud para procesar multiples alertas en lote.
 * 
 * Utilizado para ejecutar predicciones, optimizaciones y generar ordenes
 * de compra para un conjunto de productos con alertas pendientes.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarAlertasRequest {

    /**
     * IDs de las alertas a procesar.
     * Debe contener al menos una alerta.
     */
    @NotEmpty(message = "Debe proporcionar al menos una alerta para procesar")
    private List<Long> alertaIds;

    /**
     * Horizonte de tiempo para la prediccion en meses.
     * 
     * Si no se proporciona, el sistema calcula automaticamente basado en:
     * - Tipo de producto (rotacion rapida: 3 meses, media: 6 meses, lenta: 12 meses)
     * - Lead time del proveedor
     * - Patron de demanda historico
     * 
     * Valor recomendado por defecto: 12 meses
     */
    private Integer horizonteTiempo;

    /**
     * Indica si se debe ejecutar la optimizacion EOQ/ROP.
     * Por defecto es true.
     */
    @Builder.Default
    private Boolean ejecutarOptimizacion = true;

    /**
     * Indica si se debe generar automaticamente la orden de compra.
     * Por defecto es false (requiere confirmacion del usuario).
     */
    @Builder.Default
    private Boolean generarOrdenAutomaticamente = false;

    /**
     * ID del usuario que solicita el procesamiento.
     * Se obtiene del contexto de seguridad si no se proporciona.
     */
    private Integer usuarioId;
}
