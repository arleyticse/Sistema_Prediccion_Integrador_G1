package com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoCreateRequest {
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;
    
    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;
    
    @NotNull(message = "La unidad de medida es obligatoria")
    private Integer unidadMedidaId;
    
    @NotNull(message = "El proveedor principal es obligatorio")
    private Integer proveedorId;
    
    @NotNull(message = "Los días de lead time son obligatorios")
    @Min(value = 1, message = "Los días de lead time deben ser al menos 1")
    private Integer diasLeadTime;
    
    @NotNull(message = "El costo de adquisición es obligatorio")
    @Min(value = 0, message = "El costo de adquisición no puede ser negativo")
    private BigDecimal costoAdquisicion;
    
    @NotNull(message = "El costo de mantenimiento es obligatorio")
    @Min(value = 0, message = "El costo de mantenimiento no puede ser negativo")
    private BigDecimal costoMantenimiento;
    
    @NotNull(message = "El costo de pedido es obligatorio")
    @Min(value = 0, message = "El costo de pedido no puede ser negativo")
    private BigDecimal costoPedido;
}
