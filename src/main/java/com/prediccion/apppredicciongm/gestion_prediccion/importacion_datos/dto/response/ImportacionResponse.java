package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ImportacionCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de ImportacionDatos
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportacionResponse {

    private Long importacionId;

    private ImportacionCreateRequest.TipoDatosImportacion tipoDatos;

    private String nombreArchivo;

    private LocalDateTime fechaImportacion;

    private Integer registrosProcesados;

    private Integer registrosExitosos;

    private Integer registrosFallidos;

    private EstadoImportacion estadoImportacion;

    private Long tiempoProcesamiento;

    private Double tasaExito;

    private String errores;

    private String observaciones;

    private LocalDateTime fechaActualizacion;

    /**
     * Enum para estados de importación
     */
    public enum EstadoImportacion {
        EN_PROCESO,
        COMPLETADA,
        COMPLETADA_CON_ERRORES,
        FALLIDA,
        CANCELADA
    }
}
