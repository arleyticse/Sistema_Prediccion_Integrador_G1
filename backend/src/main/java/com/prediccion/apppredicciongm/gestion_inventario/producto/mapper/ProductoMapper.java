package com.prediccion.apppredicciongm.gestion_inventario.producto.mapper;

import org.mapstruct.*;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoUpdateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponseTable;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductoMapper {
    
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "proveedorPrincipal", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "costoMantenimientoAnual", ignore = true)
    Producto toEntity(ProductoCreateRequest request);
    
    @Mapping(source = "productoId", target = "productoId")
    @Mapping(source = "categoria", target = "categoria")
    @Mapping(source = "unidadMedida.", target = "unidadMedida")
    @Mapping(source = "proveedorPrincipal", target = "proveedorPrincipal")
    @Mapping(target = "tieneInventario", ignore = true)
    @Mapping(target = "stockDisponible", ignore = true)
    @Mapping(target = "stockMinimo", ignore = true)
    @Mapping(target = "puntoReorden", ignore = true)
    @Mapping(target = "estadoInventario", ignore = true)
    @Mapping(target = "valorInventario", ignore = true)
    ProductoResponse toResponse(Producto producto);
    
    @Mapping(source = "productoId", target = "productoId")
    @Mapping(source = "unidadMedida.nombre", target = "unidadMedida")
    ProductoResponseTable toResponseTable(Producto producto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "proveedorPrincipal", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "costoMantenimientoAnual", ignore = true)
    void updateEntityFromDto(ProductoCreateRequest request, @MappingTarget Producto producto);
}
