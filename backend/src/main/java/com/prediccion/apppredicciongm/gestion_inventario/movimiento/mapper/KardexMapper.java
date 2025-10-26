package com.prediccion.apppredicciongm.gestion_inventario.movimiento.mapper;

import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.KardexResponse;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface KardexMapper {
    
    @Mapping(source = "productoId", target = "producto.productoId")
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "kardexId", ignore = true)
    @Mapping(target = "saldoCantidad", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "anulado", ignore = true)
    Kardex toEntity(KardexCreateRequest request);
    
    @Mapping(source = "producto.productoId", target = "productoId")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "producto.categoria.nombre", target = "categoriaProducto")
    @Mapping(source = "proveedor.proveedorId", target = "proveedorId")
    @Mapping(source = "proveedor.nombreComercial", target = "nombreProveedor")
    @Mapping(source = "usuario.usuarioId", target = "usuarioId")
    @Mapping(source = "usuario.nombre", target = "nombreUsuario")
    @Mapping(target = "valorTotal", expression = "java(calcularValorTotal(kardex))")
    KardexResponse toResponse(Kardex kardex);
    
    default BigDecimal calcularValorTotal(Kardex kardex) {
        if (kardex.getCostoUnitario() != null && kardex.getCantidad() != null) {
            return kardex.getCostoUnitario().multiply(BigDecimal.valueOf(kardex.getCantidad()));
        }
        return BigDecimal.ZERO;
    }
}
