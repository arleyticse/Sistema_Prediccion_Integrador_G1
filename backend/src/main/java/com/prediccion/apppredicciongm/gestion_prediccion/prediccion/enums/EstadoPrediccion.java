package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Enumeración que define los estados posibles de una predicción.
 * 
 * Estados:
 * - ACTIVA: La predicción es vigente y puede ser utilizada
 * - OBSOLETA: La predicción ha superado su horizonte de tiempo
 * - FALLIDA: La predicción no se completó correctamente
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-05
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum EstadoPrediccion {
    
    /**
     * ACTIVA: La predicción es vigente.
     * Condición: fecha_actual <= (fecha_ejecucion + horizonte_tiempo)
     */
    ACTIVA("Predicción vigente y disponible para usar", 1),
    
    /**
     * OBSOLETA: La predicción ha pasado su fecha de vigencia.
     * Condición: fecha_actual > (fecha_ejecucion + horizonte_tiempo)
     */
    OBSOLETA("Predicción expirada, generar una nueva", 2),
    
    /**
     * FALLIDA: El proceso de predicción no se completó.
     * Condición: excepción durante el cálculo o error en validación
     */
    FALLIDA("Error al generar la predicción, reintentar", 3);

    private final String descripcion;
    private final Integer codigo;

    EstadoPrediccion(String descripcion, Integer codigo) {
        this.descripcion = descripcion;
        this.codigo = codigo;
    }

    /**
     * Determina el estado de una predicción basado en sus fechas y estado de ejecución.
     *
     * @param fechaEjecucion Fecha en que se generó la predicción (no nula)
     * @param horizonteTiempo Número de días que es vigente la predicción (> 0)
     * @param esFallida Flag que indica si la predicción falló en su generación
     * @return EstadoPrediccion correspondiente
     */
    public static EstadoPrediccion determinar(LocalDateTime fechaEjecucion, Integer horizonteTiempo, boolean esFallida) {
        // Validar que los parámetros sean válidos
        if (fechaEjecucion == null || horizonteTiempo == null || horizonteTiempo <= 0) {
            return FALLIDA;
        }

        // Si la predicción fue marcada como fallida, retornar FALLIDA
        if (esFallida) {
            return FALLIDA;
        }

        // Calcular la fecha de vigencia
        LocalDateTime fechaVigencia = fechaEjecucion.plusDays(horizonteTiempo);
        LocalDateTime fechaActual = LocalDateTime.now();

        // Comparar si la predicción aún está vigente
        if (fechaActual.isAfter(fechaVigencia)) {
            return OBSOLETA;
        } else {
            return ACTIVA;
        }
    }

    /**
     * Sobrecarga que asume que la predicción es exitosa (esFallida = false).
     *
     * @param fechaEjecucion Fecha en que se generó la predicción
     * @param horizonteTiempo Número de días que es vigente la predicción
     * @return EstadoPrediccion correspondiente
     */
    public static EstadoPrediccion determinar(LocalDateTime fechaEjecucion, Integer horizonteTiempo) {
        return determinar(fechaEjecucion, horizonteTiempo, false);
    }
}
