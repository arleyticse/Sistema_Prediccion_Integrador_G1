package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.request.EstacionalidadCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.response.EstacionalidadResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service.IEstacionalidadServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para gestión de patrones estacionales de productos.
 * Proporciona endpoints para crear, actualizar, obtener y eliminar estacionalidades.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@RestController
@RequestMapping("/api/estacionalidades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Estacionalidades", description = "Gestión de patrones estacionales de demanda")
public class EstacionalidadControlador {

    private final IEstacionalidadServicio servicio;

    /**
     * Crea un nuevo patrón estacional.
     *
     * @param request datos del patrón estacional a crear
     * @return respuesta con el patrón creado y código 201
     */
    @PostMapping
    @Operation(summary = "Crear estacionalidad", 
               description = "Crea un nuevo patrón estacional para un producto en un mes específico")
    public ResponseEntity<EstacionalidadResponse> crearEstacionalidad(
            @Valid @RequestBody EstacionalidadCreateRequest request) {
        log.info("POST /api/estacionalidades - Crear estacionalidad");
        EstacionalidadResponse response = servicio.crearEstacionalidad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un patrón estacional existente.
     *
     * @param estacionalidadId ID del patrón a actualizar
     * @param request nuevos datos del patrón
     * @return respuesta con los datos actualizados
     */
    @PutMapping("/{estacionalidadId}")
    @Operation(summary = "Actualizar estacionalidad", 
               description = "Actualiza los datos de un patrón estacional existente")
    public ResponseEntity<EstacionalidadResponse> actualizarEstacionalidad(
            @Parameter(description = "ID del patrón estacional") @PathVariable Long estacionalidadId,
            @Valid @RequestBody EstacionalidadCreateRequest request) {
        log.info("PUT /api/estacionalidades/{} - Actualizar estacionalidad", estacionalidadId);
        EstacionalidadResponse response = servicio.actualizarEstacionalidad(estacionalidadId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un patrón estacional por ID.
     *
     * @param estacionalidadId ID del patrón estacional
     * @return respuesta con los datos del patrón
     */
    @GetMapping("/{estacionalidadId}")
    @Operation(summary = "Obtener estacionalidad por ID", 
               description = "Obtiene los datos de un patrón estacional específico")
    public ResponseEntity<EstacionalidadResponse> obtenerPorId(
            @Parameter(description = "ID del patrón estacional") @PathVariable Long estacionalidadId) {
        log.info("GET /api/estacionalidades/{} - Obtener estacionalidad", estacionalidadId);
        EstacionalidadResponse response = servicio.obtenerEstacionalidadPorId(estacionalidadId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el patrón estacional de un producto para un mes específico.
     *
     * @param productoId ID del producto
     * @param mes número del mes (1-12)
     * @return respuesta con el patrón estacional
     */
    @GetMapping("/producto/{productoId}/mes/{mes}")
    @Operation(summary = "Obtener estacionalidad por producto y mes", 
               description = "Obtiene el patrón estacional de un producto para un mes específico")
    public ResponseEntity<EstacionalidadResponse> obtenerPorProductoYMes(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @Parameter(description = "Número del mes (1-12)") @PathVariable Integer mes) {
        log.info("GET /api/estacionalidades/producto/{}/mes/{}", productoId, mes);
        EstacionalidadResponse response = servicio.obtenerEstacionalidadPorProductoYMes(productoId, mes);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los patrones estacionales de un producto.
     *
     * @param productoId ID del producto
     * @return respuesta con la lista de patrones
     */
    @GetMapping("/producto/{productoId}/todos")
    @Operation(summary = "Listar estacionalidades por producto", 
               description = "Lista todos los patrones estacionales de un producto")
    public ResponseEntity<?> obtenerPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        log.info("GET /api/estacionalidades/producto/{}/todos", productoId);
        var response = servicio.obtenerEstacionalidadPorProducto(productoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista patrones estacionales de un producto con paginación.
     *
     * @param productoId ID del producto
     * @param pagina número de página (comienza en 0)
     * @param tamano tamaño de la página
     * @return página de patrones estacionales
     */
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Listar estacionalidades por producto (paginado)", 
               description = "Lista patrones estacionales con paginación")
    public ResponseEntity<Page<EstacionalidadResponse>> obtenerPorProductoPaginado(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int tamano) {
        log.info("GET /api/estacionalidades/producto/{} - pagina: {}, tamano: {}", productoId, pagina, tamano);
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<EstacionalidadResponse> response = servicio.obtenerEstacionalidadPorProductoPaginado(productoId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los patrones estacionales con paginación.
     *
     * @param pagina número de página (comienza en 0)
     * @param tamano tamaño de la página
     * @return página de patrones estacionales
     */
    @GetMapping
    @Operation(summary = "Listar todas las estacionalidades", 
               description = "Lista todos los patrones estacionales con paginación")
    public ResponseEntity<Page<EstacionalidadResponse>> listarTodas(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int tamano) {
        log.info("GET /api/estacionalidades - pagina: {}, tamano: {}", pagina, tamano);
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<EstacionalidadResponse> response = servicio.listarTodasLasEstacionalidades(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca patrones estacionales por descripción de temporada.
     *
     * @param descripcion palabra clave de búsqueda
     * @param pagina número de página
     * @param tamano tamaño de la página
     * @return página de patrones que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar estacionalidades por descripción", 
               description = "Busca patrones estacionales por palabra clave en la descripción")
    public ResponseEntity<Page<EstacionalidadResponse>> buscar(
            @Parameter(description = "Palabra clave de búsqueda") @RequestParam String descripcion,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int tamano) {
        log.info("GET /api/estacionalidades/buscar?descripcion={}", descripcion);
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<EstacionalidadResponse> response = servicio.buscarPorDescripcionTemporada(descripcion, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un patrón estacional.
     *
     * @param estacionalidadId ID del patrón a eliminar
     * @return respuesta de éxito
     */
    @DeleteMapping("/{estacionalidadId}")
    @Operation(summary = "Eliminar estacionalidad", 
               description = "Elimina un patrón estacional existente")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del patrón estacional") @PathVariable Long estacionalidadId) {
        log.info("DELETE /api/estacionalidades/{}", estacionalidadId);
        servicio.eliminarEstacionalidad(estacionalidadId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula el factor estacional promedio de un producto.
     *
     * @param productoId ID del producto
     * @return respuesta con el factor promedio
     */
    @GetMapping("/producto/{productoId}/factor-promedio")
    @Operation(summary = "Calcular factor estacional promedio", 
               description = "Calcula el factor estacional promedio de todos los patrones de un producto")
    public ResponseEntity<?> calcularFactorPromedio(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        log.info("GET /api/estacionalidades/producto/{}/factor-promedio", productoId);
        var response = servicio.calcularFactorEstacionalPromedio(productoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Calcula la demanda ajustada por estacionalidad.
     *
     * @param productoId ID del producto
     * @param mes mes para el cual calcular
     * @param demandaBase demanda sin ajuste estacional
     * @return respuesta con la demanda ajustada
     */
    @GetMapping("/producto/{productoId}/demanda-ajustada")
    @Operation(summary = "Calcular demanda ajustada por estacionalidad", 
               description = "Calcula la demanda ajustada multiplicando la demanda base por el factor estacional")
    public ResponseEntity<?> calcularDemandaAjustada(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @Parameter(description = "Mes (1-12)") @RequestParam Integer mes,
            @Parameter(description = "Demanda base sin ajuste") @RequestParam Integer demandaBase) {
        log.info("GET /api/estacionalidades/producto/{}/demanda-ajustada?mes={}&demandaBase={}", 
                productoId, mes, demandaBase);
        Integer demandaAjustada = servicio.calcularDemandaAjustadaPorEstacionalidad(productoId, mes, demandaBase);
        return ResponseEntity.ok(demandaAjustada);
    }
}
