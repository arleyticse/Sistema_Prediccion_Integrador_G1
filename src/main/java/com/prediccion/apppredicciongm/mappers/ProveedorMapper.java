package com.prediccion.apppredicciongm.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.prediccion.apppredicciongm.models.Proveedor;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {
    
    @Mapping(target = "proveedorId", ignore = true)//Asegura que el ID no se sobrescriba
    void updateFromEntity(Proveedor request, @MappingTarget Proveedor proveedor);
}
