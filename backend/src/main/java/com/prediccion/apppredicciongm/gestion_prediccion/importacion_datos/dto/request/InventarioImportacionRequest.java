package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la importación de registros de inventario desde archivos CSV.
 * 
 * <p>Representa un registro individual del archivo CSV de inventario inicial.
 * Incluye validaciones para asegurar la integridad de los datos antes de
 * la inserción en la base de datos.</p>
 * 
 * <h3>Campos obligatorios del CSV:</h3>
 * <ul>
 *   <li>nombre_producto: Nombre del producto (debe existir en BD)</li>
 *   <li>stock_disponible: Stock disponible actual (≥0)</li>
 *   <li>stock_minimo: Stock mínimo para alertas (≥0)</li>
 * </ul>
 * 
 * <h3>Campos opcionales del CSV:</h3>
 * <ul>
 *   <li>stock_reservado: Stock reservado para pedidos (≥0)</li>
 *   <li>stock_en_transito: Stock en camino (≥0)</li>
 *   <li>stock_maximo: Stock máximo permitido (≥stock_minimo)</li>
 *   <li>punto_reorden: Punto de reorden automático (≥0)</li>
 *   <li>ubicacion_almacen: Ubicación física en almacén</li>
 *   <li>estado: Estado del inventario (NORMAL, BAJO, CRITICO, EXCESO, OBSOLETO, BLOQUEADO)</li>
 *   <li>observaciones: Notas adicionales</li>
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
public class InventarioImportacionRequest {

    /**
     * Número de fila en el archivo CSV (para reportar errores)
     */
    private Integer numeroFila;

    /**
     * Nombre del producto asociado al inventario
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    /**
     * Stock disponible actual en el inventario
     */
    @NotNull(message = "El stock disponible es obligatorio")
    @Min(value = 0, message = "El stock disponible no puede ser negativo")
    private Integer stockDisponible;

    /**
     * Stock mínimo permitido antes de generar alertas
     */
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    /**
     * Stock máximo permitido en el inventario
     */
    @Min(value = 0, message = "El stock máximo no puede ser negativo")
    private Integer stockMaximo;

    /**
     * Punto de reorden para compras automáticas
     */
    @Min(value = 0, message = "El punto de reorden no puede ser negativo")
    private Integer puntoReorden;

    /**
     * Stock reservado para pedidos pendientes
     */
    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Integer stockReservado;

    /**
     * Stock en tránsito (en camino al almacén)
     */
    @Min(value = 0, message = "El stock en tránsito no puede ser negativo")
    private Integer stockEnTransito;

    /**
     * Ubicación física en el almacén
     */
    @Size(max = 50, message = "La ubicación no puede exceder 50 caracteres")
    private String ubicacionAlmacen;

    /**
     * Estado del inventario (NORMAL, BAJO, CRITICO, EXCESO, OBSOLETO, BLOQUEADO)
     */
    @Pattern(regexp = "^(NORMAL|BAJO|CRITICO|EXCESO|OBSOLETO|BLOQUEADO)$", 
             message = "El estado debe ser NORMAL, BAJO, CRITICO, EXCESO, OBSOLETO o BLOQUEADO")
    private String estado;

    /**
     * Observaciones o notas adicionales
     */
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    /**
     * Lista de errores de validación acumulados
     */
    @Builder.Default
    private List<String> errores = new ArrayList<>();

    /**
     * Agrega un error de validación a la lista
     * 
     * @param error Descripción del error
     */
    public void agregarError(String error) {
        if (this.errores == null) {
            this.errores = new ArrayList<>();
        }
        this.errores.add(error);
    }

    /**
     * Verifica si el request tiene errores de validación
     * 
     * @return true si hay errores, false en caso contrario
     */
    public boolean tieneErrores() {
        return errores != null && !errores.isEmpty();
    }

    /**
     * Valida reglas de negocio adicionales
     * 
     * @return true si pasa las validaciones, false en caso contrario
     */
    public boolean validarReglas() {
        boolean valido = true;

        // Validar que stock máximo sea mayor que stock mínimo
        if (stockMaximo != null && stockMinimo != null && stockMaximo < stockMinimo) {
            agregarError("El stock máximo debe ser mayor o igual al stock mínimo");
            valido = false;
        }

        // Validar que punto de reorden esté entre mínimo y máximo
        if (puntoReorden != null && stockMinimo != null && puntoReorden < stockMinimo) {
            agregarError("El punto de reorden debe ser mayor o igual al stock mínimo");
            valido = false;
        }

        // Validar que stock disponible no exceda el máximo
        if (stockMaximo != null && stockDisponible != null && stockDisponible > stockMaximo) {
            agregarError("El stock disponible no puede exceder el stock máximo");
            valido = false;
        }

        return valido;
    }
}
