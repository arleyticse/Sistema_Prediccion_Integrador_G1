package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.mapper;

import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.GenerarPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper de MapStruct para convertir entre entidades Prediccion y DTOs.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PrediccionMapper {
    /**
     * Convierte una entidad Prediccion a PrediccionResponse DTO.
     *
     * @param prediccion la entidad predicción
     * @return el DTO de respuesta
     */
    @Mapping(target = "prediccionId", source = "prediccionId")
    @Mapping(target = "productoId", source = "producto.productoId")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "demandaPredichaTotal", source = "demandaPredichaTotal")
    @Mapping(target = "precision", source = "metricasError")
    @Mapping(target = "algoritmo", source = "algoritmoUsado")
    @Mapping(target = "fechaGeneracion", source = "fechaEjecucion")
    @Mapping(target = "descripcion", ignore = true)
    @Mapping(target = "vigenciaHasta", ignore = true)
    @Mapping(target = "detallePronostico", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "descripcionEstado", ignore = true)
    PrediccionResponse prediccionToResponse(Prediccion prediccion);

    /**
     * Convierte una lista de entidades Prediccion a DTOs PrediccionResponse.
     *
     * @param predicciones lista de entidades predicción
     * @return lista de DTOs de respuesta
     */
    List<PrediccionResponse> prediccionListToResponseList(List<Prediccion> predicciones);

    /**
     * Convierte un GenerarPrediccionRequest a una entidad Prediccion (mapeo parcial).
     * Nota: Este mapeo es parcial, se requiere completar los campos faltantes
     * después de la llamada al método.
     *
     * @param request el DTO de solicitud
     * @return la entidad predicción mapeada parcialmente
     */
    @Mapping(target = "prediccionId", ignore = true)
    @Mapping(target = "demandaPredichaTotal", ignore = true)
    @Mapping(target = "metricasError", ignore = true)
    @Mapping(target = "algoritmoUsado", ignore = true)
    @Mapping(target = "horizonteTiempo", ignore = true)
    @Mapping(target = "fechaEjecucion", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "parametroAlgoritmo", ignore = true)
    Prediccion requestToPrediccion(GenerarPrediccionRequest request);
}
