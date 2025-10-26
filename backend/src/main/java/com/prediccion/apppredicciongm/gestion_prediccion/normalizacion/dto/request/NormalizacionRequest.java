package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar la normalización manual de demanda.
 * Se utiliza cuando se desea ejecutar el proceso de normalización bajo demanda
 * sin esperar al cron job programado.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NormalizacionRequest {

    /**
     * ID del producto para el cual normalizar demanda.
     * Si es null, se normalizan todos los productos.
     */
    private Long productoId;

    /**
     * Número de días hacia atrás a procesar.
     * Por defecto es 30 días. Mínimo 1 día.
     */
    @Min(value = 1, message = "diasProcesar debe ser al menos 1")
    private Integer diasProcesar = 30;

    /**
     * Indica si se deben recalcular todos los registros o solo los nuevos.
     * Por defecto es false (solo nuevos).
     */
    private boolean recalcularTodos = false;

    /**
     * Indica si se desean notificaciones de progreso.
     * Por defecto es true.
     */
    private boolean notificaciones = true;
}
