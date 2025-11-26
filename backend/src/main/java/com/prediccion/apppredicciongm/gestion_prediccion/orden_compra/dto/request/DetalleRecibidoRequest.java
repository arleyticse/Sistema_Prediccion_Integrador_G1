package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleRecibidoRequest {
    private Long detalleId;
    private Integer cantidadRecibida;
}
