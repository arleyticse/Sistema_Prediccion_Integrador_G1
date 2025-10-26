package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.mapper;

import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.OrdenCompraResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper para convertir entre entidad OrdenCompra y DTOs de respuesta.
 * Utiliza MapStruct para generar automáticamente las implementaciones.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Mapper(componentModel = "spring")
public interface OrdenCompraMapper {
    
    /**
     * Convierte una entidad OrdenCompra a OrdenCompraResponse.
     *
     * @param ordenCompra la entidad a convertir
     * @return el DTO de respuesta
     */
    @Mapping(source = "ordenCompraId", target = "ordenCompraId")
    @Mapping(source = "numeroOrden", target = "numeroOrden")
    @Mapping(source = "proveedor.nombreComercial", target = "proveedorNombre")
    @Mapping(source = "estadoOrden", target = "estadoOrden", defaultValue = "PENDIENTE")
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "productoNombre", ignore = true)
    @Mapping(target = "cantidadSolicitada", ignore = true)
    @Mapping(target = "detallesCalculo", ignore = true)
    OrdenCompraResponse ordenCompraToResponse(OrdenCompra ordenCompra);
    
    /**
     * Convierte una lista de órdenes a lista de DTOs de respuesta.
     *
     * @param ordenes lista de entidades a convertir
     * @return lista de DTOs de respuesta
     */
    List<OrdenCompraResponse> ordenCompraListToResponseList(List<OrdenCompra> ordenes);
}
