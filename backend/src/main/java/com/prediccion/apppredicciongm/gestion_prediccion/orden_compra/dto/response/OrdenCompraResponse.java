package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la respuesta de consulta de orden de compra.
 * Incluye información de la orden y sus detalles de cálculo.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraResponse {

    /**
     * ID único de la orden de compra.
     */
    private Long ordenCompraId;

    /**
     * Número único de orden generado por el sistema.
     * Formato: OC-YYYYMMDD-XXXXX
     */
    private String numeroOrden;

    /**
     * ID del producto asociado a la orden.
     */
    private Integer productoId;

    /**
     * Nombre del producto en la orden.
     */
    private String productoNombre;

    /**
     * Cantidad total de unidades solicitadas.
     */
    private Integer cantidadSolicitada;

    /**
     * Nombre del proveedor seleccionado para la orden.
     */
    private String proveedorNombre;

    /**
     * Estado actual de la orden.
     * Valores: PENDIENTE, CONFIRMADA, RECIBIDA, CANCELADA
     */
    private String estadoOrden;

    /**
     * Fecha en la que se generó la orden (solo fecha, sin hora).
     */
    private LocalDate fechaOrden;

    /**
     * Fecha esperada de entrega.
     */
    private LocalDate fechaEntregaEsperada;

    /**
     * Fecha real de entrega (null si aún no ha llegado).
     */
    private LocalDate fechaEntregaReal;

    /**
     * Total de la orden (cantidad × precio unitario).
     */
    private BigDecimal totalOrden;

    /**
     * Indica si la orden fue generada automáticamente.
     */
    private Boolean generadaAutomaticamente;

    /**
     * Detalles del cálculo en formato JSON.
     * Incluye: predicción, stock, punto reorden, fórmula aplicada.
     */
    private String detallesCalculo;

    /**
     * Observaciones o notas adicionales sobre la orden.
     */
    private String observaciones;
}
