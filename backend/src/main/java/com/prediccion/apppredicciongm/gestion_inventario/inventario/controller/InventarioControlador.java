package com.prediccion.apppredicciongm.gestion_inventario.inventario.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.AjusteStockRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioUpdateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioAlertaResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.StockResumenResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.services.IInventarioServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inventario", description = "API para gestión de inventario de productos")
@CrossOrigin(origins = "*")
public class InventarioControlador {

    private final IInventarioServicio inventarioServicio;

    @Operation(summary = "Crear nuevo inventario", description = "Crea un registro de inventario para un producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un inventario para el producto")
    })
    @PostMapping
    public ResponseEntity<InventarioResponse> crearInventario(
            @Valid @RequestBody InventarioCreateRequest request) {
        InventarioResponse response = inventarioServicio.crearInventario(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar inventario", description = "Actualiza la información de un inventario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponse> actualizarInventario(
            @Parameter(description = "ID del inventario") @PathVariable Integer id,
            @Valid @RequestBody InventarioUpdateRequest request) {
        InventarioResponse response = inventarioServicio.actualizarInventario(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar inventario", description = "Elimina un registro de inventario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inventario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInventario(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        inventarioServicio.eliminarInventario(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener inventario por ID", description = "Retorna los detalles de un inventario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponse> obtenerInventarioPorId(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        InventarioResponse response = inventarioServicio.obtenerInventarioPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos los inventarios", description = "Retorna una lista paginada de todos los inventarios")
    @GetMapping("")
    public ResponseEntity<Page<InventarioResponse>> listarInventarios(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) int size) {
        Page<InventarioResponse> response = inventarioServicio.listarInventarios(page, size);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Buscar inventario por producto", description = "Retorna el inventario de un producto específico")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<InventarioResponse> buscarPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        InventarioResponse response = inventarioServicio.buscarPorProducto(productoId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar inventarios por categoría", description = "Retorna todos los inventarios de una categoría")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<InventarioResponse>> buscarPorCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Integer categoriaId) {
        List<InventarioResponse> response = inventarioServicio.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar inventarios por estado", description = "Retorna inventarios filtrados por estado (ACTIVO, INACTIVO, AGOTADO, BAJO_STOCK)")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<InventarioResponse>> buscarPorEstado(
            @Parameter(description = "Estado del inventario") @PathVariable EstadoInventario estado,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<InventarioResponse> response = inventarioServicio.buscarPorEstado(estado, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar inventarios por nombre de producto", description = "Búsqueda parcial por nombre de producto")
    @GetMapping("/buscar")
    public ResponseEntity<Page<InventarioResponse>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre del producto") @RequestParam String nombre,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<InventarioResponse> response = inventarioServicio.buscarPorNombre(nombre, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar por rango de stock", description = "Retorna inventarios dentro de un rango de stock")
    @GetMapping("/rango-stock")
    public ResponseEntity<List<InventarioResponse>> buscarPorRangoStock(
            @Parameter(description = "Stock mínimo") @RequestParam Integer minStock,
            @Parameter(description = "Stock máximo") @RequestParam Integer maxStock) {
        List<InventarioResponse> response = inventarioServicio.buscarPorRangoStock(minStock, maxStock);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener alertas de stock bajo", description = "Retorna productos que han alcanzado su punto de reorden")
    @GetMapping("/alertas/stock-bajo")
    public ResponseEntity<List<InventarioAlertaResponse>> obtenerAlertasStockBajo() {
        List<InventarioAlertaResponse> response = inventarioServicio.obtenerAlertasStockBajo();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener alertas críticas", description = "Retorna productos por debajo del stock mínimo")
    @GetMapping("/alertas/criticas")
    public ResponseEntity<List<InventarioAlertaResponse>> obtenerAlertasCriticas() {
        List<InventarioAlertaResponse> response = inventarioServicio.obtenerAlertasCriticas();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener productos agotados", description = "Retorna productos con stock cero")
    @GetMapping("/alertas/agotados")
    public ResponseEntity<List<InventarioAlertaResponse>> obtenerAlertasAgotados() {
        List<InventarioAlertaResponse> response = inventarioServicio.obtenerAlertasAgotados();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener productos sin movimiento", description = "Retorna productos sin movimiento por X días")
    @GetMapping("/alertas/sin-movimiento")
    public ResponseEntity<List<InventarioAlertaResponse>> obtenerAlertasSinMovimiento(
            @Parameter(description = "Número de días sin movimiento") @RequestParam(defaultValue = "30") Integer dias) {
        List<InventarioAlertaResponse> response = inventarioServicio.obtenerAlertasSinMovimiento(dias);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener inventarios sobre stock máximo", description = "Retorna productos que superan su stock máximo")
    @GetMapping("/sobre-maximo")
    public ResponseEntity<List<InventarioResponse>> obtenerInventariosSobreMaximo() {
        List<InventarioResponse> response = inventarioServicio.obtenerInventariosSobreMaximo();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ajustar stock manualmente", description = "Realiza un ajuste manual del stock (positivo o negativo)")
    @PostMapping("/ajustar-stock")
    public ResponseEntity<InventarioResponse> ajustarStock(
            @Valid @RequestBody AjusteStockRequest request) {
        InventarioResponse response = inventarioServicio.ajustarStock(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener resumen general del inventario", description = "Retorna estadísticas generales del inventario")
    @GetMapping("/resumen")
    public ResponseEntity<StockResumenResponse> obtenerResumenGeneral() {
        StockResumenResponse response = inventarioServicio.obtenerResumenGeneral();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar si necesita reorden", description = "Verifica si un inventario ha alcanzado su punto de reorden")
    @GetMapping("/{id}/necesita-reorden")
    public ResponseEntity<Boolean> necesitaReorden(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        boolean necesita = inventarioServicio.necesitaReorden(id);
        return ResponseEntity.ok(necesita);
    }

    @Operation(summary = "Verificar si está bajo punto mínimo", description = "Verifica si un inventario está por debajo del stock mínimo")
    @GetMapping("/{id}/bajo-minimo")
    public ResponseEntity<Boolean> estaBajoPuntoMinimo(
            @Parameter(description = "ID del inventario") @PathVariable Integer id) {
        boolean bajoPuntoMinimo = inventarioServicio.estaBajoPuntoMinimo(id);
        return ResponseEntity.ok(bajoPuntoMinimo);
    }
}
