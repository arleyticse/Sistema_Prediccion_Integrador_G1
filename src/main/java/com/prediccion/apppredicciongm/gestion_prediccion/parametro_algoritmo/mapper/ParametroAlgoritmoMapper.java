package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.mapper;

import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.request.ParametroAlgoritmoCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.response.ParametroAlgoritmoResponse;
import com.prediccion.apppredicciongm.models.ParametroAlgoritmo;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DTOs y entidades de ParametroAlgoritmo
 */
@Component
public class ParametroAlgoritmoMapper {

    /**
     * Convierte DTO Request a entidad
     */
    public ParametroAlgoritmo toEntity(ParametroAlgoritmoCreateRequest request) {
        if (request == null) {
            return null;
        }

        return ParametroAlgoritmo.builder()
                .nombreParametro(request.getNombreParametro())
                .valorParametro(request.getValorParametro())
                .tipoAlgoritmo(request.getTipoAlgoritmo())
                .descripcion(request.getDescripcion())
                .valorMinimo(request.getValorMinimo())
                .valorMaximo(request.getValorMaximo())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();
    }

    /**
     * Convierte entidad a DTO Response
     */
    public ParametroAlgoritmoResponse toResponse(ParametroAlgoritmo entity) {
        if (entity == null) {
            return null;
        }

        return ParametroAlgoritmoResponse.builder()
                .parametroId(entity.getParametroId())
                .nombreParametro(entity.getNombreParametro())
                .valorParametro(entity.getValorParametro())
                .tipoAlgoritmo(entity.getTipoAlgoritmo())
                .descripcion(entity.getDescripcion())
                .valorMinimo(entity.getValorMinimo())
                .valorMaximo(entity.getValorMaximo())
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}
