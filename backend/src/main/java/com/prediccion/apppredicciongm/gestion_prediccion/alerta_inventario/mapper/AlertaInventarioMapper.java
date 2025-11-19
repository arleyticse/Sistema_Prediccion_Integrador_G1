package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.mapper;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaDashboardDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaInventarioResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProductoBasicoDTO;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper para convertir entidades AlertaInventario a DTOs.
 * 
 * Utiliza MapStruct para realizar conversiones automaticas y
 * personalizadas entre entidades y DTOs de transferencia.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Mapper(componentModel = "spring")
public interface AlertaInventarioMapper {
    /**
     * Convierte una entidad AlertaInventario a AlertaInventarioResponse.
     * 
     * @param entidad Entidad de alerta
     * @return DTO de respuesta con informacion completa
     */
    @Mapping(target = "tipoAlerta", expression = "java(entidad.getTipoAlerta().name())")
    @Mapping(target = "nivelCriticidad", expression = "java(entidad.getNivelCriticidad().name())")
    @Mapping(target = "estado", expression = "java(entidad.getEstado().name())")
    @Mapping(target = "producto", source = "producto")
    @Mapping(target = "usuarioAsignadoId", source = "usuarioAsignado.usuarioId")
    @Mapping(target = "usuarioAsignadoNombre", source = "usuarioAsignado.nombre")
    AlertaInventarioResponse toResponse(AlertaInventario entidad);

    /**
     * Convierte una lista de entidades AlertaInventario a lista de DTOs.
     * 
     * @param entidades Lista de entidades
     * @return Lista de DTOs de respuesta
     */
    List<AlertaInventarioResponse> toResponseList(List<AlertaInventario> entidades);

    /**
     * Convierte un Producto a ProductoBasicoDTO.
     * 
     * @param producto Entidad de producto
     * @return DTO con informacion basica del producto
     */
    @Mapping(target = "categoria", source = "categoria.nombre")
    @Mapping(target = "unidadMedida", source = "unidadMedida.nombre")
    @Mapping(target = "proveedor.proveedorId", source = "proveedorPrincipal.proveedorId")
    @Mapping(target = "proveedor.nombreComercial", source = "proveedorPrincipal.nombreComercial")
    @Mapping(target = "proveedor.tiempoEntregaDias", source = "proveedorPrincipal.tiempoEntregaDias")
    @Mapping(target = "proveedor.contacto", source = "proveedorPrincipal.personaContacto")
    @Mapping(target = "proveedor.telefono", source = "proveedorPrincipal.telefono")
    @Mapping(target = "codigoSKU", ignore = true)
    ProductoBasicoDTO toProductoDTO(Producto producto);

    /**
     * Convierte una entidad AlertaInventario a AlertaDashboardDTO.
     * Version optimizada para el dashboard con datos planos.
     * 
     * @param entidad Entidad de alerta
     * @return DTO optimizado para dashboard
     */
    @Mapping(target = "tipoAlerta", expression = "java(entidad.getTipoAlerta().name())")
    @Mapping(target = "nivelCriticidad", expression = "java(entidad.getNivelCriticidad().name())")
    @Mapping(target = "estado", expression = "java(entidad.getEstado().name())")
    @Mapping(target = "productoId", source = "producto.productoId")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "productoCategoria", source = "producto.categoria.nombre")
    @Mapping(target = "costoAdquisicion", source = "producto.costoAdquisicion")
    @Mapping(target = "proveedorId", source = "producto.proveedorPrincipal.proveedorId")
    @Mapping(target = "proveedorNombreComercial", source = "producto.proveedorPrincipal.nombreComercial")
    @Mapping(target = "proveedorTiempoEntrega", source = "producto.proveedorPrincipal.tiempoEntregaDias")
    AlertaDashboardDTO toDashboardDTO(AlertaInventario entidad);

    /**
     * Convierte una lista de entidades a DTOs de dashboard.
     * 
     * @param entidades Lista de entidades
     * @return Lista de DTOs para dashboard
     */
    List<AlertaDashboardDTO> toDashboardDTOList(List<AlertaInventario> entidades);
}
