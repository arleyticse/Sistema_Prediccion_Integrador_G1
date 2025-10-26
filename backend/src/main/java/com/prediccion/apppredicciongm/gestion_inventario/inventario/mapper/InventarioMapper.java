package com.prediccion.apppredicciongm.gestion_inventario.inventario.mapper;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioUpdateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioAlertaResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioResponse;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventarioMapper {
    
    @Mapping(target = "inventarioId", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "fechaUltimoMovimiento", ignore = true)
    @Mapping(target = "fechaUltimaActualizacion", ignore = true)
    @Mapping(target = "diasSinVenta", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Inventario toEntity(InventarioCreateRequest request);
    
    @Mapping(source = "inventarioId", target = "inventarioId")
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "producto.categoria.nombre", target = "categoriaNombre")
    @Mapping(source = "producto.unidadMedida.nombre", target = "unidadMedida")
    @Mapping(target = "stockTotal", expression = "java(inventario.getStockTotal())")
    @Mapping(target = "necesitaReorden", expression = "java(inventario.necesitaReorden())")
    @Mapping(target = "bajoPuntoMinimo", expression = "java(inventario.bajoPuntoMinimo())")
    InventarioResponse toResponse(Inventario inventario);
    
    @Mapping(source = "inventarioId", target = "inventarioId")
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "producto.categoria.nombre", target = "categoriaNombre")
    @Mapping(target = "tipoAlerta", expression = "java(determinarTipoAlerta(inventario))")
    @Mapping(target = "mensaje", expression = "java(generarMensajeAlerta(inventario))")
    InventarioAlertaResponse toAlertaResponse(Inventario inventario);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "inventarioId", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "fechaUltimoMovimiento", ignore = true)
    @Mapping(target = "fechaUltimaActualizacion", ignore = true)
    @Mapping(target = "diasSinVenta", ignore = true)
    void updateEntityFromDto(InventarioUpdateRequest request, @MappingTarget Inventario inventario);
    
    default String determinarTipoAlerta(Inventario inventario) {
        if (inventario.getStockDisponible() == 0) {
            return "CRITICO";
        } else if (inventario.bajoPuntoMinimo()) {
            return "BAJO_MINIMO";
        } else if (inventario.necesitaReorden()) {
            return "REORDEN";
        } else if (inventario.getDiasSinVenta() != null && inventario.getDiasSinVenta() > 30) {
            return "SIN_MOVIMIENTO";
        }
        return "NORMAL";
    }
    
    default String generarMensajeAlerta(Inventario inventario) {
        if (inventario.getStockDisponible() == 0) {
            return "Producto agotado - Requiere acción inmediata";
        } else if (inventario.bajoPuntoMinimo()) {
            return "Stock por debajo del mínimo (" + inventario.getStockMinimo() + " unidades)";
        } else if (inventario.necesitaReorden()) {
            return "Stock alcanzó punto de reorden (" + inventario.getPuntoReorden() + " unidades)";
        } else if (inventario.getDiasSinVenta() != null && inventario.getDiasSinVenta() > 30) {
            return "Producto sin movimiento por " + inventario.getDiasSinVenta() + " días";
        }
        return "Stock normal";
    }
}
