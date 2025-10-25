package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service;

import com.prediccion.apppredicciongm.models.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz de servicio para la gestión de órdenes de compra automáticas.
 * Define el contrato para las operaciones de generación, consulta y confirmación de órdenes.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
public interface IOrdenCompraService {

    /**
     * Genera automáticamente una orden de compra basada en una predicción ARIMA.
     * 
     * Fórmula: Cantidad = (Demanda Predicha × 1.2) - Stock Actual + Punto Reorden
     *
     * @param prediccionId ID de la predicción que genera la orden
     * @return OrdenCompra generada y persistida
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.PrediccionNoEncontradaException
     *         si la predicción no existe
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.DatosInsuficientesException
     *         si el stock es suficiente y no se requiere generar orden
     */
    OrdenCompra generarOrdenAutomatica(Integer prediccionId);

    /**
     * Valida si debe generarse una orden de compra para un producto.
     * Verifica si existe predicción reciente y si el stock está bajo.
     *
     * @param productoId ID del producto a validar
     * @return true si se debe generar orden, false en caso contrario
     */
    boolean validarOrdenCompra(Integer productoId);

    /**
     * Obtiene las órdenes de compra para un producto específico.
     * Incluye paginación para grandes volúmenes de datos.
     *
     * @param productoId ID del producto
     * @param pageable configuración de paginación
     * @return página de órdenes del producto
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.ProductoNoEncontradoException
     *         si el producto no existe
     */
    Page<OrdenCompra> obtenerOrdenesPorProducto(Integer productoId, Pageable pageable);

    /**
     * Obtiene todas las órdenes de compra con paginación.
     *
     * @param pageable configuración de paginación
     * @return página de todas las órdenes
     */
    Page<OrdenCompra> obtenerTodasLasOrdenes(Pageable pageable);

    /**
     * Obtiene la orden más reciente para un producto.
     *
     * @param productoId ID del producto
     * @return OrdenCompra más reciente
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException
     *         si no existe orden para el producto
     */
    OrdenCompra obtenerUltimaOrden(Integer productoId);

    /**
     * Confirma una orden de compra (cambia su estado a CONFIRMADA).
     * Operación idempotente: si ya está confirmada, no falla.
     *
     * @param ordenId ID de la orden a confirmar
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException
     *         si la orden no existe
     */
    void confirmarOrden(Long ordenId);

    /**
     * Cancela una orden de compra (cambia su estado a CANCELADA).
     * Solo se puede cancelar órdenes en estado PENDIENTE.
     *
     * @param ordenId ID de la orden a cancelar
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException
     *         si la orden no existe
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenYaConfirmadaException
     *         si la orden ya ha sido confirmada o recibida
     */
    void cancelarOrden(Long ordenId);
}
