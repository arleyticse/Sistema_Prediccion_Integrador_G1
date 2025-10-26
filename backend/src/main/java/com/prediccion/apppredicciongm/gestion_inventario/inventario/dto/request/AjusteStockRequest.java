package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AjusteStockRequest {
    
    @NotNull(message = "El ID del inventario es obligatorio")
    private Integer inventarioId;
    
    @NotNull(message = "La cantidad a ajustar es obligatoria")
    private Integer cantidad;
    
    @NotNull(message = "El motivo del ajuste es obligatorio")
    private String motivo;
    
    private String observaciones;
}
