package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventarioAlertaResponse {
    
    private Integer inventarioId;
    private Integer productoId;
    private String nombreProducto;
    private String categoriaNombre;
    private Integer stockDisponible;
    private Integer stockMinimo;
    private Integer puntoReorden;
    private EstadoInventario estado;
    private String tipoAlerta;
    private String mensaje;
    private Integer diasSinVenta;
}
