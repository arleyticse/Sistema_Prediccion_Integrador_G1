package com.prediccion.apppredicciongm.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.prediccion.apppredicciongm.models.Proveedor;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {
    
    @Mapping(target = "proveedorId", ignore = true)
    void updateFromEntity(Proveedor request, @MappingTarget Proveedor proveedor);
}
