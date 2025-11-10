package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO para el resultado de la ejecución de un job de generación de alertas.
 * 
 * Contiene métricas detalladas sobre:
 * - Tiempo de ejecución
 * - Alertas generadas por tipo
 * - Alertas generadas por criticidad
 * - Productos procesados
 * - Errores encontrados
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionResultDTO {

    /**
     * Nombre del job ejecutado.
     * Ejemplo: "Detección de Stock Bajo"
     */
    private String nombreJob;

    /**
     * Fecha y hora de inicio de la ejecución.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de finalización de la ejecución.
     */
    private LocalDateTime fechaFin;

    /**
     * Tiempo de ejecución en milisegundos.
     */
    private Long tiempoEjecucionMs;

    /**
     * Total de productos analizados.
     */
    private Integer productosAnalizados;

    /**
     * Total de alertas generadas en esta ejecución.
     */
    @Builder.Default
    private Integer totalAlertasGeneradas = 0;

    /**
     * Contador de alertas por tipo.
     * Key: Tipo de alerta (STOCK_BAJO, PREDICCION_VENCIDA, etc.)
     * Value: Cantidad de alertas generadas
     */
    @Builder.Default
    private Map<String, Integer> alertasPorTipo = new HashMap<>();

    /**
     * Contador de alertas por nivel de criticidad.
     * Key: Nivel de criticidad (CRITICA, ALTA, MEDIA, BAJA)
     * Value: Cantidad de alertas generadas
     */
    @Builder.Default
    private Map<String, Integer> alertasPorCriticidad = new HashMap<>();

    /**
     * IDs de las alertas generadas.
     */
    @Builder.Default
    private List<Long> alertasGeneradasIds = new ArrayList<>();

    /**
     * Número de alertas que fueron actualizadas (ya existían).
     */
    @Builder.Default
    private Integer alertasActualizadas = 0;

    /**
     * Número de alertas nuevas creadas.
     */
    @Builder.Default
    private Integer alertasNuevas = 0;

    /**
     * Número de errores encontrados durante la ejecución.
     */
    @Builder.Default
    private Integer erroresEncontrados = 0;

    /**
     * Lista de mensajes de error.
     */
    @Builder.Default
    private List<String> mensajesError = new ArrayList<>();

    /**
     * Indica si la ejecución fue exitosa (sin errores críticos).
     */
    private Boolean exitoso;

    /**
     * Observaciones adicionales sobre la ejecución.
     */
    private String observaciones;
}
