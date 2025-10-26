package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventarioUpdateRequest {
    
    @Min(value = 0, message = "El stock disponible no puede ser negativo")
    private Integer stockDisponible;
    
    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Integer stockReservado;
    
    @Min(value = 0, message = "El stock en tránsito no puede ser negativo")
    private Integer stockEnTransito;
    
    @Min(value = 1, message = "El stock mínimo debe ser al menos 1")
    private Integer stockMinimo;
    
    @Min(value = 1, message = "El stock máximo debe ser al menos 1")
    private Integer stockMaximo;
    
    @Min(value = 1, message = "El punto de reorden debe ser al menos 1")
    private Integer puntoReorden;
    
    private String ubicacionAlmacen;
    
    private EstadoInventario estado;
    
    private String observaciones;
}
