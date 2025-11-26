package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service;

import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.models.Prediccion;
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
     * Obtiene el resumen completo de una orden de compra para generar PDF.
     * Incluye datos de empresa, proveedor, detalles de productos y totales.
     *
     * @param ordenId ID de la orden de compra
     * @return ResumenOrdenCompraDTO con toda la información
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException
     *         si la orden no existe
     */
    com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.ResumenOrdenCompraDTO obtenerResumenOrdenCompra(Long ordenId);

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

    /**
     * Genera automáticamente una orden de compra basada en una predicción.
     * Utiliza la demanda predicha y factores de seguridad para calcular cantidades.
     *
     * @param prediccionId ID de la predicción base para generar la orden
     * @return OrdenCompra generada automáticamente
     * @throws RuntimeException si el predicción no existe o no se puede generar la orden
     */
    OrdenCompra generarOrdenDesdePredicion(Integer prediccionId);

    /**
     * Obtiene todas las órdenes de compra en estado BORRADOR.
     * Estas órdenes requieren revisión y aprobación del usuario antes de ser procesadas.
     *
     * @return lista de órdenes en estado BORRADOR
     */
    List<OrdenCompra> obtenerOrdenesBorrador();

    /**
     * Aprueba múltiples órdenes BORRADOR, cambiando su estado a PENDIENTE.
     * Solo procesa órdenes que estén efectivamente en estado BORRADOR.
     * Omite órdenes no encontradas o que no estén en BORRADOR.
     *
     * @param ordenIds lista de IDs de órdenes a aprobar
     */
    void aprobarOrdenesBorrador(List<Long> ordenIds);

    /**
     * Obtiene una orden de compra por su ID.
     *
     * @param ordenId ID de la orden
     * @return OrdenCompra si existe
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException
     *         si no se encuentra la orden
     */
    OrdenCompra obtenerOrdenPorId(Long ordenId);

    /**
     * Marca una orden como recibida (parcial o completa) y registra entradas en kardex.
     * Valida que la orden esté en un estado válido (APROBADA/ENVIADA/EN_TRANSITO) y que
     * las cantidades recibidas no excedan las solicitadas.
     *
     * @param ordenId ID de la orden
     * @param request Detalle de cantidades recibidas por detalle
     */
    void recibirOrden(Long ordenId, com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request.RecibirOrdenRequest request);
}
