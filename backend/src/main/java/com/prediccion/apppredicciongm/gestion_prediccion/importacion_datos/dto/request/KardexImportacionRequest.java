package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para importación de registros de Kardex desde CSV.
 * 
 * <p>Representa una línea del archivo CSV con todos los datos necesarios
 * para crear un movimiento en el kardex de inventario.</p>
 * 
 * <h3>Campos obligatorios:</h3>
 * <ul>
 *   <li><b>nombreProducto</b>: Nombre del producto (debe existir)</li>
 *   <li><b>tipoMovimiento</b>: ENTRADA o SALIDA</li>
 *   <li><b>cantidad</b>: Cantidad del movimiento (positivo)</li>
 *   <li><b>saldoCantidad</b>: Saldo después del movimiento</li>
 *   <li><b>fechaMovimiento</b>: Fecha del movimiento</li>
 * </ul>
 * 
 * <h3>Campos opcionales:</h3>
 * <ul>
 *   <li>costoUnitario: Costo por unidad</li>
 *   <li>nombreProveedor: Proveedor (para entradas)</li>
 *   <li>lote: Número de lote</li>
 *   <li>fechaVencimiento: Fecha de vencimiento del lote</li>
 *   <li>tipoDocumento: Tipo de documento (Factura, Guía, etc.)</li>
 *   <li>numeroDocumento: Número del documento</li>
 *   <li>referencia: Referencia adicional</li>
 *   <li>motivo: Motivo del movimiento</li>
 *   <li>ubicacion: Ubicación en almacén</li>
 *   <li>observaciones: Notas adicionales</li>
 *   <li>anulado: Si el movimiento está anulado</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KardexImportacionRequest {

    /**
     * Número de fila en el CSV (para reporte de errores)
     */
    private Integer numeroFila;

    /**
     * Nombre del producto (debe existir en la BD)
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    /**
     * Tipo de movimiento: ENTRADA o SALIDA
     */
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Pattern(regexp = "ENTRADA|SALIDA", message = "El tipo de movimiento debe ser ENTRADA o SALIDA")
    private String tipoMovimiento;

    /**
     * Cantidad del movimiento (debe ser positivo)
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    /**
     * Saldo de cantidad después del movimiento
     */
    @NotNull(message = "El saldo de cantidad es obligatorio")
    @Min(value = 0, message = "El saldo de cantidad no puede ser negativo")
    private Integer saldoCantidad;

    /**
     * Costo unitario del producto (opcional, para entradas)
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo unitario debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El costo unitario debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal costoUnitario;

    /**
     * Fecha del movimiento (formato: yyyy-MM-dd HH:mm:ss o yyyy-MM-dd)
     */
    @NotNull(message = "La fecha de movimiento es obligatoria")
    private LocalDateTime fechaMovimiento;

    /**
     * Fecha de vencimiento del lote (opcional)
     */
    private LocalDateTime fechaVencimiento;

    /**
     * Nombre del proveedor (opcional, para entradas)
     */
    @Size(max = 100, message = "El nombre del proveedor no puede exceder 100 caracteres")
    private String nombreProveedor;

    /**
     * Número de lote (opcional)
     */
    @Size(max = 50, message = "El lote no puede exceder 50 caracteres")
    private String lote;

    /**
     * Tipo de documento (Factura, Guía de Remisión, Vale de Salida, etc.)
     */
    @Size(max = 50, message = "El tipo de documento no puede exceder 50 caracteres")
    private String tipoDocumento;

    /**
     * Número del documento
     */
    @Size(max = 50, message = "El número de documento no puede exceder 50 caracteres")
    private String numeroDocumento;

    /**
     * Referencia adicional
     */
    @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
    private String referencia;

    /**
     * Motivo del movimiento
     */
    @Size(max = 200, message = "El motivo no puede exceder 200 caracteres")
    private String motivo;

    /**
     * Ubicación en almacén
     */
    @Size(max = 50, message = "La ubicación no puede exceder 50 caracteres")
    private String ubicacion;

    /**
     * Observaciones adicionales
     */
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    /**
     * Indica si el movimiento está anulado
     */
    private Boolean anulado;

    /**
     * Lista de errores acumulados durante la validación
     */
    @Builder.Default
    private List<String> errores = new ArrayList<>();

    /**
     * Valida reglas de negocio complejas del kardex.
     * 
     * <h3>Reglas validadas:</h3>
     * <ul>
     *   <li>Si es ENTRADA, puede tener proveedor</li>
     *   <li>Si es SALIDA, no debe tener proveedor ni costo unitario</li>
     *   <li>Si tiene lote, debe tener fecha de vencimiento</li>
     *   <li>Fecha de vencimiento debe ser posterior a fecha de movimiento</li>
     *   <li>Saldo debe ser consistente (para ENTRADA: saldo >= cantidad)</li>
     * </ul>
     * 
     * @return true si todas las reglas se cumplen, false en caso contrario
     */
    public boolean validarReglas() {
        errores.clear();

        // Validar consistencia entre tipo de movimiento y campos opcionales
        if ("SALIDA".equals(tipoMovimiento)) {
            if (nombreProveedor != null && !nombreProveedor.trim().isEmpty()) {
                errores.add("Los movimientos de SALIDA no deben tener proveedor");
            }
            // Para salidas, el costo unitario es opcional (puede usarse para calcular el valor de la salida)
        }

        // Validar lote y fecha de vencimiento
        if (lote != null && !lote.trim().isEmpty()) {
            if (fechaVencimiento == null) {
                errores.add("Si especifica un lote, debe indicar la fecha de vencimiento");
            }
        }

        // Validar que fecha de vencimiento sea posterior a fecha de movimiento
        if (fechaVencimiento != null && fechaMovimiento != null) {
            if (fechaVencimiento.isBefore(fechaMovimiento)) {
                errores.add("La fecha de vencimiento debe ser posterior a la fecha de movimiento");
            }
        }

        // Validar saldo consistente
        if (saldoCantidad != null && cantidad != null) {
            if ("ENTRADA".equals(tipoMovimiento)) {
                // Para entrada: el saldo debe ser al menos la cantidad entrante
                // (asumiendo que el saldo es el acumulado después del movimiento)
                if (saldoCantidad < cantidad) {
                    errores.add("Para movimientos de ENTRADA, el saldo debe ser mayor o igual a la cantidad");
                }
            }
            // Para SALIDA, el saldo puede ser cualquier valor >= 0 (ya validado en @Min)
        }

        // Validar que documentos tengan tipo y número completos
        if ((tipoDocumento != null && !tipoDocumento.trim().isEmpty()) && 
            (numeroDocumento == null || numeroDocumento.trim().isEmpty())) {
            errores.add("Si especifica tipo de documento, debe indicar el número de documento");
        }

        return errores.isEmpty();
    }

    /**
     * Agrega un error a la lista de errores.
     * 
     * @param error Mensaje de error a agregar
     */
    public void agregarError(String error) {
        if (errores == null) {
            errores = new ArrayList<>();
        }
        errores.add(error);
    }

    /**
     * Verifica si hay errores acumulados.
     * 
     * @return true si hay al menos un error
     */
    public boolean tieneErrores() {
        return errores != null && !errores.isEmpty();
    }
}
