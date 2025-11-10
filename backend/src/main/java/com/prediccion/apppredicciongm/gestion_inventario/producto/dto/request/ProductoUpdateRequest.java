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
public class ProductoUpdateRequest {
    
    @NotBlank(message = "El nombre del producto no puede estar vacío")
    private String nombre;
    
    private Integer proveedorId;
    
    @Min(value = 1, message = "Los días de lead time deben ser al menos 1")
    private Integer diasLeadTime;
    
    @NotNull(message = "El costo de adquisición es obligatorio")
    @Min(value = 0, message = "El costo de adquisición no puede ser negativo")
    private BigDecimal costoAdquisicion;
    
    @Min(value = 0, message = "El costo de mantenimiento no puede ser negativo")
    private BigDecimal costoMantenimiento;
    
    @Min(value = 0, message = "El costo de pedido no puede ser negativo")
    private BigDecimal costoPedido;
}
