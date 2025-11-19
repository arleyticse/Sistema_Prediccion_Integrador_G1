package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.request.GenerarOrdenRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.OrdenCompraResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.ResumenOrdenCompraDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenYaConfirmadaException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.ProductoSinProveedorException;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.mapper.OrdenCompraMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service.IOrdenCompraService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.DatosInsuficientesException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.PrediccionNoEncontradaException;
import com.prediccion.apppredicciongm.models.OrdenCompra;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador REST para operaciones de órdenes de compra automáticas.
 * Proporciona endpoints para generar, consultar y gestionar órdenes basadas en predicciones.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Órdenes de Compra", description = "API para gestionar órdenes de compra automáticas generadas desde predicciones")
public class OrdenCompraControlador {

    private final IOrdenCompraService ordenService;
    private final OrdenCompraMapper ordenMapper;

    /**
     * Genera una nueva orden de compra automáticamente basada en una predicción.
     *
     * Endpoint: POST /api/ordenes/generar/{prediccionId}
     *
     * Lógica:
     * 1. Obtiene la predicción ARIMA
     * 2. Calcula: Cantidad = (Predicción × 1.2) - Stock + PuntoReorden
     * 3. Genera orden automática con estado PENDIENTE
     * 4. Asigna proveedor del producto
     *
     * @param prediccionId ID de la predicción que genera la orden
     * @param request datos adicionales de la orden (opcional)
     * @return orden de compra generada
     */
    @PostMapping("/generar/{prediccionId}")
    @Operation(summary = "Generar orden automática",
               description = "Genera una orden de compra automática basada en predicción ARIMA")
    public ResponseEntity<OrdenCompraResponse> generarOrden(
            @PathVariable Integer prediccionId,
            @RequestBody(required = false) GenerarOrdenRequest request) {

        log.info("[ORDEN] POST /generar/{} - Generando orden automática", prediccionId);

        try {
            OrdenCompra orden = ordenService.generarOrdenAutomatica(prediccionId);
            OrdenCompraResponse response = ordenMapper.ordenCompraToResponse(orden);

            log.info("[ORDEN] Orden generada exitosamente: {} (ID: {})", 
                    orden.getNumeroOrden(), orden.getOrdenCompraId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (PrediccionNoEncontradaException e) {
            log.error("[ORDEN] Predicción no encontrada: {}", prediccionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (DatosInsuficientesException e) {
            log.warn("[ORDEN] Advertencia: No es necesaria la orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (ProductoSinProveedorException e) {
            log.error("[ORDEN] Producto sin proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al generar orden: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las órdenes de compra con paginación.
     *
     * Endpoint: GET /api/ordenes?page=0&size=10
     *
     * @param page número de página (0-indexed, default=0)
     * @param size tamaño de página (default=10)
     * @return página de órdenes de compra
     */
    @GetMapping
    @Operation(summary = "Obtener todas las órdenes",
               description = "Obtiene todas las órdenes de compra con paginación")
    public ResponseEntity<Page<OrdenCompraResponse>> obtenerTodasLasOrdenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("[ORDEN] GET / - Obteniendo todas las órdenes (page={}, size={})", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrdenCompra> ordenesPage = ordenService.obtenerTodasLasOrdenes(pageable);
            Page<OrdenCompraResponse> responsePage = ordenesPage.map(ordenMapper::ordenCompraToResponse);

            log.info("[ORDEN] Se encontraron {} órdenes de {} total", 
                    responsePage.getNumberOfElements(), responsePage.getTotalElements());
            return ResponseEntity.ok(responsePage);

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener órdenes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las órdenes de compra para un producto específico.
     *
     * Endpoint: GET /api/ordenes/producto/{productoId}?page=0&size=10
     *
     * @param productoId ID del producto
     * @param page número de página (0-indexed, default=0)
     * @param size tamaño de página (default=10)
     * @return lista paginada de órdenes del producto
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Obtener órdenes por producto",
               description = "Obtiene todas las órdenes de compra para un producto específico con paginación")
    public ResponseEntity<Page<OrdenCompraResponse>> obtenerOrdenesPorProducto(
            @PathVariable Integer productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("[ORDEN] GET /producto/{} - Obteniendo órdenes (page={}, size={})", 
                productoId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrdenCompra> ordenesPage = ordenService.obtenerOrdenesPorProducto(productoId, pageable);
            Page<OrdenCompraResponse> responsePage = ordenesPage.map(ordenMapper::ordenCompraToResponse);

            log.info("[ORDEN] Se encontraron {} órdenes de {} total", 
                    responsePage.getNumberOfElements(), responsePage.getTotalElements());
            return ResponseEntity.ok(responsePage);

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener órdenes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene la última orden de compra para un producto.
     *
     * Endpoint: GET /api/ordenes/ultima/{productoId}
     *
     * @param productoId ID del producto
     * @return la orden más reciente del producto
     */
    @GetMapping("/ultima/{productoId}")
    @Operation(summary = "Obtener última orden",
               description = "Obtiene la orden de compra más reciente para un producto específico")
    public ResponseEntity<OrdenCompraResponse> obtenerUltimaOrden(
            @PathVariable Integer productoId) {

        log.info("[ORDEN] GET /ultima/{} - Obteniendo última orden", productoId);

        try {
            OrdenCompra orden = ordenService.obtenerUltimaOrden(productoId);
            OrdenCompraResponse response = ordenMapper.ordenCompraToResponse(orden);

            log.info("[ORDEN] Última orden obtenida: {}", orden.getNumeroOrden());
            return ResponseEntity.ok(response);

        } catch (OrdenCompraNoEncontradaException e) {
            log.warn("[ORDEN] Advertencia: No existe orden para producto: {}", productoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener última orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene el resumen completo de una orden para generación de PDF.
     *
     * Endpoint: GET /api/ordenes/{ordenId}/pdf-data
     *
     * @param ordenId ID de la orden
     * @return el resumen completo con empresa, proveedor, detalles y totales
     */
    @GetMapping("/{ordenId}/pdf-data")
    @Operation(summary = "Obtener datos para PDF",
               description = "Obtiene todos los datos necesarios para generar un PDF de la orden de compra")
    public ResponseEntity<ResumenOrdenCompraDTO> obtenerDatosParaPDF(
            @PathVariable Long ordenId) {

        log.info("[ORDEN] GET /{}/pdf-data - Obteniendo datos completos para PDF", ordenId);

        try {
            ResumenOrdenCompraDTO resumen = ordenService.obtenerResumenOrdenCompra(ordenId);
            log.info("[ORDEN] Resumen obtenido para orden: {}", ordenId);
            return ResponseEntity.ok(resumen);

        } catch (RuntimeException e) {
            log.warn("[ORDEN] Advertencia: Orden no encontrada: {}", ordenId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener resumen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene una orden de compra específica por su ID.
     *
     * Endpoint: GET /api/ordenes/{ordenId}
     *
     * @param ordenId ID de la orden
     * @return los detalles de la orden
     */
    @GetMapping("/{ordenId}")
    @Operation(summary = "Obtener orden por ID",
               description = "Obtiene los detalles completos de una orden de compra específica")
    public ResponseEntity<OrdenCompraResponse> obtenerOrdenPorId(
            @PathVariable Long ordenId) {

        log.info("[ORDEN] GET /{} - Obteniendo orden por ID", ordenId);

        try {
            OrdenCompra orden = ordenService.obtenerOrdenPorId(ordenId);
            OrdenCompraResponse response = ordenMapper.ordenCompraToResponse(orden);
            log.info("[ORDEN] Orden obtenida: {}", ordenId);
            return ResponseEntity.ok(response);

        } catch (OrdenCompraNoEncontradaException e) {
            log.warn("[ORDEN] Advertencia: Orden no encontrada: {}", ordenId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Confirma una orden de compra (cambia su estado a CONFIRMADA).
     *
     * Endpoint: POST /api/ordenes/{ordenId}/confirmar
     *
     * Operación idempotente: si la orden ya está confirmada, no falla.
     *
     * @param ordenId ID de la orden a confirmar
     * @return respuesta sin contenido (204 No Content)
     */
    @PostMapping("/{ordenId}/confirmar")
    @Operation(summary = "Confirmar orden",
               description = "Cambia el estado de una orden a CONFIRMADA")
    public ResponseEntity<Void> confirmarOrden(
            @PathVariable Long ordenId) {

        log.info("[ORDEN] POST /{}/confirmar - Confirmando orden", ordenId);

        try {
            ordenService.confirmarOrden(ordenId);

            log.info("[ORDEN] Orden confirmada: {}", ordenId);
            return ResponseEntity.noContent().build();

        } catch (OrdenCompraNoEncontradaException e) {
            log.warn("[ORDEN] Advertencia: Orden no encontrada: {}", ordenId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al confirmar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancela una orden de compra (cambia su estado a CANCELADA).
     *
     * Endpoint: DELETE /api/ordenes/{ordenId}
     *
     * Solo se pueden cancelar órdenes en estado PENDIENTE.
     *
     * @param ordenId ID de la orden a cancelar
     * @return respuesta sin contenido (204 No Content)
     */
    @DeleteMapping("/{ordenId}")
    @Operation(summary = "Cancelar orden",
               description = "Cancela una orden de compra (solo si está en estado PENDIENTE)")
    public ResponseEntity<Void> cancelarOrden(
            @PathVariable Long ordenId) {

        log.info("[ORDEN] DELETE /{} - Cancelando orden", ordenId);

        try {
            ordenService.cancelarOrden(ordenId);

            log.info("[ORDEN] Orden cancelada: {}", ordenId);
            return ResponseEntity.noContent().build();

        } catch (OrdenCompraNoEncontradaException e) {
            log.warn("[ORDEN] Advertencia: Orden no encontrada: {}", ordenId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (OrdenYaConfirmadaException e) {
            log.warn("[ORDEN] Advertencia: No se puede cancelar orden confirmada: {}", ordenId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al cancelar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las órdenes en estado BORRADOR pendientes de aprobación.
     *
     * Endpoint: GET /api/ordenes/borradores
     *
     * @return lista de órdenes en BORRADOR
     */
    @GetMapping("/borradores")
    @Operation(summary = "Obtener órdenes BORRADOR",
               description = "Obtiene todas las órdenes en estado BORRADOR que requieren aprobación")
    public ResponseEntity<List<OrdenCompraResponse>> obtenerOrdenesBorrador() {

        log.info("[ORDEN] GET /borradores - Obteniendo órdenes en BORRADOR");

        try {
            List<OrdenCompra> ordenes = ordenService.obtenerOrdenesBorrador();
            List<OrdenCompraResponse> responses = ordenes.stream()
                    .map(ordenMapper::ordenCompraToResponse)
                    .toList();

            log.info("[ORDEN] Se encontraron {} órdenes en BORRADOR", responses.size());
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("[ORDEN] Error al obtener órdenes BORRADOR: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Aprueba múltiples órdenes BORRADOR, cambiando su estado a PENDIENTE.
     *
     * Endpoint: POST /api/ordenes/aprobar-borrador
     *
     * Request body: { "ordenIds": [1, 2, 3] }
     *
     * @param ordenIds lista de IDs de órdenes a aprobar
     * @return respuesta sin contenido (204 No Content)
     */
    @PostMapping("/aprobar-borrador")
    @Operation(summary = "Aprobar órdenes BORRADOR",
               description = "Aprueba múltiples órdenes cambiando su estado de BORRADOR a PENDIENTE")
    public ResponseEntity<Void> aprobarOrdenesBorrador(
            @RequestBody List<Long> ordenIds) {

        log.info("[ORDEN] POST /aprobar-borrador - Aprobando {} órdenes", ordenIds.size());

        try {
            ordenService.aprobarOrdenesBorrador(ordenIds);

            log.info("[ORDEN] Órdenes aprobadas exitosamente");
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("[ORDEN] Error al aprobar órdenes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Manejador de excepciones para el controlador.
     * Proporciona respuestas HTTP consistentes para excepciones del dominio.
     */
    @ExceptionHandler(OrdenCompraNoEncontradaException.class)
    public ResponseEntity<?> manejarOrdenNoEncontrada(OrdenCompraNoEncontradaException e) {
        log.error("[ORDEN] Excepción: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden no encontrada: " + e.getMessage());
    }

    @ExceptionHandler(OrdenYaConfirmadaException.class)
    public ResponseEntity<?> manejarOrdenYaConfirmada(OrdenYaConfirmadaException e) {
        log.error("[ORDEN] Excepción: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Operación no permitida: " + e.getMessage());
    }

    @ExceptionHandler(ProductoSinProveedorException.class)
    public ResponseEntity<?> manejarProductoSinProveedor(ProductoSinProveedorException e) {
        log.error("[ORDEN] Excepción: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error de configuración: " + e.getMessage());
    }

    @ExceptionHandler(DatosInsuficientesException.class)
    public ResponseEntity<?> manejarDatosInsuficientes(DatosInsuficientesException e) {
        log.warn("[ORDEN] Advertencia: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validación fallida: " + e.getMessage());
    }

    @ExceptionHandler(PrediccionNoEncontradaException.class)
    public ResponseEntity<?> manejarPrediccionNoEncontrada(PrediccionNoEncontradaException e) {
        log.error("[ORDEN] Excepción: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Predicción no encontrada: " + e.getMessage());
    }
}
