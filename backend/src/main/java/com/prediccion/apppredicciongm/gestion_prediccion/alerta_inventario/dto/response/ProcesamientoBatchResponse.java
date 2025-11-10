package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para procesamiento en lote.
 * 
 * Contiene el resumen del procesamiento batch de alertas,
 * incluyendo exitos, errores y resultados parciales.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesamientoBatchResponse {

    /**
     * Fecha y hora de inicio del procesamiento.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de finalizacion del procesamiento.
     */
    private LocalDateTime fechaFin;

    /**
     * Tiempo total de ejecucion en milisegundos.
     */
    private Long tiempoEjecucionMs;

    /**
     * Total de alertas procesadas.
     */
    private Integer totalProcesadas;

    /**
     * Numero de procesamiento exitosos.
     */
    private Integer exitosos;

    /**
     * Numero de procesamiento fallidos.
     */
    private Integer fallidos;

    /**
     * Lista de IDs de alertas procesadas exitosamente.
     */
    @Builder.Default
    private List<Long> alertasExitosas = new ArrayList<>();

    /**
     * Lista de IDs de alertas que fallaron.
     */
    @Builder.Default
    private List<Long> alertasFallidas = new ArrayList<>();

    /**
     * Mensajes de error por alerta fallida.
     */
    @Builder.Default
    private List<String> mensajesError = new ArrayList<>();

    /**
     * Informacion adicional del procesamiento.
     */
    private String observaciones;

    /**
     * Indica si el procesamiento fue completamente exitoso.
     */
    private Boolean exitoTotal;

    /**
     * IDs de las predicciones generadas.
     */
    @Builder.Default
    private List<Long> prediccionesGeneradas = new ArrayList<>();

    /**
     * IDs de las optimizaciones calculadas.
     */
    @Builder.Default
    private List<Long> optimizacionesGeneradas = new ArrayList<>();

    /**
     * IDs de las ordenes de compra generadas.
     */
    @Builder.Default
    private List<Long> ordenesGeneradas = new ArrayList<>();
}
