package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.request.EstacionalidadCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.response.EstacionalidadResponse;
import com.prediccion.apppredicciongm.models.EstacionalidadProducto;

/**
 * Mapper para convertir entre EntidadEstacionalidadProducto y sus DTOs.
 * Utiliza MapStruct para generar automáticamente implementaciones de mapeo.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@Mapper(componentModel = "spring")
public interface EstacionalidadMapper {

    /**
     * Convierte EstacionalidadCreateRequest a entidad EstacionalidadProducto.
     *
     * @param request datos de entrada
     * @return entidad EstacionalidadProducto
     */
    EstacionalidadProducto toEntity(EstacionalidadCreateRequest request);

    /**
     * Convierte entidad EstacionalidadProducto a EstacionalidadResponse.
     *
     * @param entity entidad del modelo
     * @return DTO de respuesta
     */
    EstacionalidadResponse toResponse(EstacionalidadProducto entity);

    /**
     * Actualiza una entidad EstacionalidadProducto desde un DTO de entrada.
     *
     * @param request datos de entrada
     * @param entity entidad a actualizar (anotada con @MappingTarget)
     */
    void updateEntityFromDto(EstacionalidadCreateRequest request, @MappingTarget EstacionalidadProducto entity);
}
