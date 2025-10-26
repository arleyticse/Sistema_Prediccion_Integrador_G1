package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEliminadoResponse {
    
    private String mensaje;
    private Integer productoId;
    private Boolean inventarioEliminado;
    private Integer movimientosArchivados;
}
