package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.mapper;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ImportacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ImportacionResponse;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DTOs y entidades de ImportacionDatos
 */
@Component
public class ImportacionMapper {

    /**
     * Convierte DTO Response a entidad
     */
    public ImportacionResponse toResponse(ImportacionDatos entity) {
        if (entity == null) {
            return null;
        }

        return ImportacionResponse.builder()
                .importacionId(entity.getImportacionId())
                .tipoDatos(mapTipoDatos(entity.getTipoDatos()))
                .nombreArchivo(entity.getNombreArchivo())
                .fechaImportacion(entity.getFechaImportacion())
                .registrosProcesados(entity.getRegistrosProcesados())
                .registrosExitosos(entity.getRegistrosExitosos())
                .registrosFallidos(entity.getRegistrosFallidos())
                .estadoImportacion(mapEstado(entity.getEstadoImportacion()))
                .tiempoProcesamiento(entity.getTiempoProcesamiento())
                .tasaExito(entity.getTasaExito())
                .errores(entity.getErrores())
                .observaciones(entity.getObservaciones())
                .build();
    }

    /**
     * Mapea TipoDatosImportacion enum al enum DTO
     */
    private ImportacionCreateRequest.TipoDatosImportacion mapTipoDatos(
            com.prediccion.apppredicciongm.enums.TipoDatosImportacion tipoDatos) {
        if (tipoDatos == null) {
            return null;
        }
        return ImportacionCreateRequest.TipoDatosImportacion.valueOf(tipoDatos.name());
    }

    /**
     * Mapea EstadoImportacion enum del modelo al enum DTO
     */
    private ImportacionResponse.EstadoImportacion mapEstado(
            com.prediccion.apppredicciongm.enums.EstadoImportacion estado) {
        if (estado == null) {
            return null;
        }
        return ImportacionResponse.EstadoImportacion.valueOf(estado.name());
    }
}
