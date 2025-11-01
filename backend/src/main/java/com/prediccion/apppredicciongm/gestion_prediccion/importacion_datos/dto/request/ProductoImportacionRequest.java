package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para importación de productos desde CSV
 * Representa una fila del archivo CSV
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImportacionRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @NotNull(message = "El costo de adquisición es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo de adquisición debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El costo de adquisición debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal costoAdquisicion;

    @NotNull(message = "El costo de mantenimiento es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo de mantenimiento no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El costo de mantenimiento debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal costoMantenimiento;

    @DecimalMin(value = "0.00", message = "El costo de mantenimiento anual no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El costo de mantenimiento anual debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal costoMantenimientoAnual;

    @NotNull(message = "El costo de pedido es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo de pedido no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El costo de pedido debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal costoPedido;

    @NotNull(message = "Los días de lead time son obligatorios")
    @Min(value = 1, message = "Los días de lead time deben ser al menos 1")
    @Max(value = 365, message = "Los días de lead time no pueden exceder 365")
    private Integer diasLeadTime;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombreCategoria;

    @NotBlank(message = "La abreviatura de la unidad de medida es obligatoria")
    private String abreviaturaUnidadMedida;

    // Campos auxiliares para el procesamiento
    private Integer numeroFila;
    private String errores;

    public void agregarError(String error) {
        if (this.errores == null || this.errores.isEmpty()) {
            this.errores = error;
        } else {
            this.errores += " | " + error;
        }
    }

    public boolean tieneErrores() {
        return this.errores != null && !this.errores.isEmpty();
    }
}
