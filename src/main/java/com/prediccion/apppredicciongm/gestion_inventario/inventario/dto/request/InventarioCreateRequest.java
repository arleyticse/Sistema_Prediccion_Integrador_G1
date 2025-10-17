package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventarioCreateRequest {
    
    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;
    
    @NotNull(message = "El stock disponible es obligatorio")
    @Min(value = 0, message = "El stock disponible no puede ser negativo")
    private Integer stockDisponible;
    
    @Builder.Default
    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Integer stockReservado = 0;
    
    @Builder.Default
    @Min(value = 0, message = "El stock en tránsito no puede ser negativo")
    private Integer stockEnTransito = 0;
    
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 1, message = "El stock mínimo debe ser al menos 1")
    private Integer stockMinimo;
    
    @Min(value = 1, message = "El stock máximo debe ser al menos 1")
    private Integer stockMaximo;
    
    @NotNull(message = "El punto de reorden es obligatorio")
    @Min(value = 1, message = "El punto de reorden debe ser al menos 1")
    private Integer puntoReorden;
    
    private String ubicacionAlmacen;
    
    private String observaciones;
}
