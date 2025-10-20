package com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KardexCreateRequest {
    
    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;


    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;
    
    private String tipoDocumento;
    
    private String numeroDocumento;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    
    @Min(value = 0, message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;
    
    private String lote;
    
    private LocalDateTime fechaVencimiento;
    
    private Integer proveedorId;
    
    @NotNull(message = "El motivo es obligatorio")
    private String motivo;
    
    private String referencia;
    
    private String observaciones;
    
    private String ubicacion;
}
