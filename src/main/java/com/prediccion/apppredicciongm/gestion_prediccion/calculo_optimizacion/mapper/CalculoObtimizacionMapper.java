package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.mapper;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalculoObtimizacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoObtimizacionResponse;
import com.prediccion.apppredicciongm.models.CalculoObtimizacion;
import org.springframework.stereotype.Component;

@Component
public class CalculoObtimizacionMapper {

    /**
     * Convierte DTO Request a entidad
     */
    public CalculoObtimizacion toEntity(CalculoObtimizacionCreateRequest request) {
        if (request == null) {
            return null;
        }

        return CalculoObtimizacion.builder()
                .demandaAnualEstimada(request.getDemandaAnualEstimada())
                .costoMantenimiento(request.getCostoMantenimiento())
                .costoPedido(request.getCostoPedido())
                .diasLeadTime(request.getDiasLeadTime())
                .costoUnitario(request.getCostoUnitario())
                .stockSeguridad(request.getStockSeguridad())
                .observaciones(request.getObservaciones())
                .build();
    }

    /**
     * Convierte entidad a DTO Response
     */
    public CalculoObtimizacionResponse toResponse(CalculoObtimizacion entity) {
        if (entity == null) {
            return null;
        }

        return CalculoObtimizacionResponse.builder()
                .calculoId(entity.getCalculoId())
                .producto(null)  // El producto se mapea en el servicio
                .fechaCalculo(entity.getFechaCalculo())
                .demandaAnualEstimada(entity.getDemandaAnualEstimada())
                .eoqCantidadOptima(entity.getEoqCantidadOptima())
                .ropPuntoReorden(entity.getRopPuntoReorden())
                .stockSeguridadSugerido(entity.getStockSeguridadSugerido())
                .costoTotalInventario(entity.getCostoTotalInventario())
                .costoMantenimiento(entity.getCostoMantenimiento())
                .costoPedido(entity.getCostoPedido())
                .diasLeadTime(entity.getDiasLeadTime())
                .costoUnitario(entity.getCostoUnitario())
                .numeroOrdenesAnuales(entity.getNumeroOrdenesAnuales())
                .diasEntreLotes(entity.getDiasEntreLotes())
                .observaciones(entity.getObservaciones())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}
