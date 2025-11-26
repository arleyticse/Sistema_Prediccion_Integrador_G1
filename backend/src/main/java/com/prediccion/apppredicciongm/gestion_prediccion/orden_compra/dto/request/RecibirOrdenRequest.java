package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecibirOrdenRequest {
    private List<DetalleRecibidoRequest> detalles;
    private String numeroDocumentoProveedor; // opcional (factura/guía)
    private String observaciones;
}
