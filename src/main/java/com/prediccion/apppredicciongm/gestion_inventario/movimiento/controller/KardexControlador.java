package com.prediccion.apppredicciongm.gestion_inventario.movimiento.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.KardexResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.MovimientoResumenResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.services.IKardexService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Movimientos de Inventario (Kardex)", description = "API para registro y consulta de movimientos de inventario")
@CrossOrigin(origins = "*")
public class KardexControlador {

    private final IKardexService kardexService;

    @Operation(summary = "Registrar nuevo movimiento",
               description = "Registra un movimiento de inventario (entrada, salida o ajuste)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping
    public ResponseEntity<KardexResponse> registrarMovimiento(
            @Valid @RequestBody KardexCreateRequest request) {
        KardexResponse response = kardexService.registrarMovimiento(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener movimiento por ID",
               description = "Retorna los detalles de un movimiento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<KardexResponse> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento") @PathVariable Long id) {
        KardexResponse response = kardexService.obtenerMovimientoPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos los movimientos",
               description = "Retorna una lista paginada de todos los movimientos")
    @GetMapping
    public ResponseEntity<Page<KardexResponse>> listarMovimientos(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarMovimientos(pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar movimiento",
               description = "Elimina un movimiento de inventario (usar con precaución)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movimiento eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento") @PathVariable Long id) {
        kardexService.eliminarMovimiento(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar movimientos por producto",
               description = "Retorna todos los movimientos de un producto específico")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<Page<KardexResponse>> listarMovimientosPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarMovimientosPorProducto(productoId, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar movimientos por producto y fecha",
               description = "Retorna movimientos de un producto dentro de un rango de fechas")
    @GetMapping("/producto/{productoId}/fecha")
    public ResponseEntity<Page<KardexResponse>> listarMovimientosPorProductoYFecha(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @Parameter(description = "Fecha inicio (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha fin (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarMovimientosPorProductoYFecha(
                productoId, fechaInicio, fechaFin, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener último movimiento de un producto",
               description = "Retorna el movimiento más reciente de un producto")
    @GetMapping("/producto/{productoId}/ultimo")
    public ResponseEntity<KardexResponse> obtenerUltimoMovimientoProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        KardexResponse response = kardexService.obtenerUltimoMovimientoProducto(productoId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar por tipo de movimiento",
               description = "Retorna movimientos filtrados por tipo (COMPRA, VENTA, AJUSTE, etc.)")
    @GetMapping("/tipo/{tipoMovimiento}")
    public ResponseEntity<Page<KardexResponse>> listarPorTipoMovimiento(
            @Parameter(description = "Tipo de movimiento") @PathVariable TipoMovimiento tipoMovimiento,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarPorTipoMovimiento(tipoMovimiento, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar por producto y tipo",
               description = "Retorna movimientos de un producto filtrados por tipo")
    @GetMapping("/producto/{productoId}/tipo/{tipoMovimiento}")
    public ResponseEntity<Page<KardexResponse>> listarPorProductoYTipo(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId,
            @Parameter(description = "Tipo de movimiento") @PathVariable TipoMovimiento tipoMovimiento,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarPorProductoYTipo(productoId, tipoMovimiento, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar por rango de fechas",
               description = "Retorna movimientos dentro de un período específico")
    @GetMapping("/fecha")
    public ResponseEntity<Page<KardexResponse>> listarPorRangoFecha(
            @Parameter(description = "Fecha inicio") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha fin") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarPorRangoFecha(fechaInicio, fechaFin, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar movimientos por proveedor",
               description = "Retorna todos los movimientos asociados a un proveedor")
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<Page<KardexResponse>> listarPorProveedor(
            @Parameter(description = "ID del proveedor") @PathVariable Integer proveedorId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarPorProveedor(proveedorId, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar movimientos por usuario",
               description = "Retorna movimientos registrados por un usuario específico")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<KardexResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Integer usuarioId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) int tamano) {
        Page<KardexResponse> response = kardexService.listarPorUsuario(usuarioId, pagina, tamano);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar por número de documento",
               description = "Busca movimientos por número de factura, guía, etc.")
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<List<KardexResponse>> buscarPorNumeroDocumento(
            @Parameter(description = "Número de documento") @PathVariable String numeroDocumento) {
        List<KardexResponse> response = kardexService.buscarPorNumeroDocumento(numeroDocumento);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar por lote",
               description = "Retorna todos los movimientos de un lote específico")
    @GetMapping("/lote/{lote}")
    public ResponseEntity<List<KardexResponse>> buscarPorLote(
            @Parameter(description = "Código de lote") @PathVariable String lote) {
        List<KardexResponse> response = kardexService.buscarPorLote(lote);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar productos por vencer",
               description = "Retorna movimientos con productos próximos a vencer")
    @GetMapping("/vencimiento-proximo")
    public ResponseEntity<List<KardexResponse>> buscarPorVencimientoProximo(
            @Parameter(description = "Fecha inicio") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha fin") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<KardexResponse> response = kardexService.buscarPorVencimientoProximo(fechaInicio, fechaFin);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener historial de precios",
               description = "Retorna el historial de precios de un producto")
    @GetMapping("/producto/{productoId}/historial-precios")
    public ResponseEntity<List<KardexResponse>> obtenerHistorialPreciosProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        List<KardexResponse> response = kardexService.obtenerHistorialPreciosProducto(productoId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener resumen de movimientos",
               description = "Retorna estadísticas generales de los movimientos")
    @GetMapping("/resumen")
    public ResponseEntity<MovimientoResumenResponse> obtenerResumenMovimientos() {
        MovimientoResumenResponse response = kardexService.obtenerResumenMovimientos();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular saldo actual de un producto",
               description = "Retorna el saldo actual según el último movimiento")
    @GetMapping("/producto/{productoId}/saldo")
    public ResponseEntity<Integer> calcularSaldoActualProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer productoId) {
        Integer saldo = kardexService.calcularSaldoActualProducto(productoId);
        return ResponseEntity.ok(saldo);
    }
}
