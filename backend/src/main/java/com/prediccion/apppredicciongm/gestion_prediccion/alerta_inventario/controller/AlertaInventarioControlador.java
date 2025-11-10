package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.*;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaInventarioResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service.IAlertaInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de alertas de inventario.
 * 
 * Proporciona endpoints para:
 * - Listar y filtrar alertas con paginación
 * - Agrupar alertas por proveedor
 * - Actualizar estados de alertas
 * - Procesar alertas en lote (batch)
 * 
 * Base URL: /api/alertas-inventario
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@RestController
@RequestMapping("/api/alertas-inventario")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AlertaInventarioControlador {

    private final IAlertaInventarioService alertaService;

    /**
     * Lista todas las alertas con filtros y paginación.
     * 
     * GET /api/alertas-inventario
     * 
     * Parámetros de consulta:
     * - estado: Filtrar por estado (PENDIENTE, EN_PROCESO, RESUELTA, etc.)
     * - criticidad: Filtrar por criticidad (BAJA, MEDIA, ALTA, CRITICA)
     * - tipoAlerta: Filtrar por tipo de alerta
     * - productoId: Filtrar por producto específico
     * - proveedorId: Filtrar por proveedor específico
     * - fechaDesde: Fecha inicio del rango
     * - fechaHasta: Fecha fin del rango
     * - page: Número de página (base 0)
     * - size: Tamaño de página (default: 20)
     * - sort: Campo de ordenamiento (default: fechaDeteccion,desc)
     * 
     * @param estado Estado de la alerta
     * @param criticidad Nivel de criticidad
     * @param tipoAlerta Tipo de alerta
     * @param productoId ID del producto
     * @param proveedorId ID del proveedor
     * @param fechaDesde Fecha inicio
     * @param fechaHasta Fecha fin
     * @param pageable Configuración de paginación
     * @return Página de alertas encontradas
     */
    @GetMapping
    public ResponseEntity<Page<AlertaInventarioResponse>> listarAlertas(
            @RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) NivelCriticidad criticidad,
            @RequestParam(required = false) TipoAlerta tipoAlerta,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) Integer proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @PageableDefault(size = 20, sort = "fechaDeteccion", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/alertas-inventario - Filtros: estado={}, criticidad={}, tipo={}, producto={}, proveedor={}",
                estado, criticidad, tipoAlerta, productoId, proveedorId);

        try {
            Page<AlertaInventarioResponse> alertas = alertaService.listarAlertasFiltradas(
                    estado, criticidad, tipoAlerta, productoId, proveedorId,
                    fechaDesde, fechaHasta, pageable
            );

            log.info("Alertas encontradas: {} (página {}/{})",
                    alertas.getTotalElements(), alertas.getNumber() + 1, alertas.getTotalPages());

            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al listar alertas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene alertas para el dashboard con información completa.
     * 
     * GET /api/alertas-inventario/dashboard
     * 
     * Retorna alertas con:
     * - Información completa del producto
     * - Datos del proveedor principal
     * - Stock actual y mínimo desde inventario
     * - Ideal para visualización en dashboard
     * 
     * @return Lista de alertas para dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<AlertaInventarioResponse>> obtenerAlertasDashboard() {
        log.info("GET /api/alertas-inventario/dashboard");

        try {
            // Obtener todas las alertas con datos enriquecidos
            List<AlertaInventarioResponse> alertas = alertaService.listarAlertas();

            log.info("Alertas dashboard: {} encontradas", alertas.size());

            return ResponseEntity.ok(alertas);

        } catch (Exception e) {
            log.error("Error al obtener alertas para dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene una alerta específica por su ID.
     * 
     * GET /api/alertas-inventario/{id}
     * 
     * @param id ID de la alerta
     * @return Alerta encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertaInventarioResponse> obtenerAlerta(
            @PathVariable Long id
    ) {
        log.info("GET /api/alertas-inventario/{}", id);

        try {
            AlertaInventarioResponse alerta = alertaService.obtenerAlertaPorId(id);
            return ResponseEntity.ok(alerta);

        } catch (RuntimeException e) {
            log.error("Alerta no encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error al obtener alerta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Agrupa alertas pendientes por proveedor con totales.
     * 
     * GET /api/alertas-inventario/agrupadas-proveedor
     * 
     * Retorna un mapa con:
     * - proveedorId → Lista de alertas del proveedor
     * - Incluye metadatos de totales por proveedor
     * 
     * @return Alertas agrupadas por proveedor
     */
    @GetMapping("/agrupadas-proveedor")
    public ResponseEntity<Map<String, Object>> obtenerAlertasAgrupadasPorProveedor() {
        log.info("GET /api/alertas-inventario/agrupadas-proveedor");

        try {
            Map<Integer, List<AlertaInventarioResponse>> alertasAgrupadas = 
                    alertaService.obtenerAlertasAgrupadasPorProveedor();

            // Calcular totales por proveedor
            Map<Integer, Map<String, Object>> proveedoresConTotales = new HashMap<>();

            for (Map.Entry<Integer, List<AlertaInventarioResponse>> entry : alertasAgrupadas.entrySet()) {
                Integer proveedorId = entry.getKey();
                List<AlertaInventarioResponse> alertasProveedor = entry.getValue();

                Map<String, Object> proveedorInfo = new HashMap<>();
                proveedorInfo.put("alertas", alertasProveedor);
                proveedorInfo.put("totalAlertas", alertasProveedor.size());

                // Calcular cantidad total sugerida
                Integer cantidadTotal = alertasProveedor.stream()
                        .map(AlertaInventarioResponse::getCantidadSugerida)
                        .filter(c -> c != null)
                        .reduce(0, Integer::sum);

                proveedorInfo.put("cantidadTotalSugerida", cantidadTotal);

                // Contar alertas por criticidad
                Map<String, Long> alertasPorCriticidad = new HashMap<>();
                alertasProveedor.forEach(a -> {
                    String criticidad = a.getNivelCriticidad();
                    alertasPorCriticidad.merge(criticidad, 1L, Long::sum);
                });

                proveedorInfo.put("alertasPorCriticidad", alertasPorCriticidad);
                proveedoresConTotales.put(proveedorId, proveedorInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("proveedores", proveedoresConTotales);
            response.put("totalProveedores", proveedoresConTotales.size());

            log.info("Alertas agrupadas: {} proveedores encontrados", proveedoresConTotales.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al agrupar alertas por proveedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza el estado de una alerta específica.
     * 
     * PUT /api/alertas-inventario/{id}/estado
     * 
     * Body:
     * {
     *   "nuevoEstado": "EN_PROCESO",
     *   "observaciones": "Iniciando proceso de compra",
     *   "usuarioId": 1
     * }
     * 
     * @param id ID de la alerta
     * @param request Datos del nuevo estado
     * @return Alerta actualizada
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<AlertaInventarioResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoAlertaRequest request
    ) {
        log.info("PUT /api/alertas-inventario/{}/estado - Nuevo estado: {}", id, request.getNuevoEstado());

        try {
            AlertaInventarioResponse alertaActualizada = alertaService.actualizarEstadoAlerta(
                    id,
                    request.getNuevoEstado(),
                    request.getObservaciones(),
                    request.getUsuarioId()
            );

            log.info("Estado actualizado exitosamente: {} → {}", id, request.getNuevoEstado());

            return ResponseEntity.ok(alertaActualizada);

        } catch (RuntimeException e) {
            log.error("Error al actualizar estado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marca múltiples alertas como EN_PROCESO en batch.
     * 
     * POST /api/alertas-inventario/batch/marcar-en-proceso
     * 
     * Body:
     * {
     *   "alertaIds": [1, 2, 3, 4, 5],
     *   "usuarioId": 1,
     *   "observaciones": "Atendiendo alertas críticas"
     * }
     * 
     * @param request IDs de alertas y datos del proceso
     * @return Resultado de la operación batch
     */
    @PostMapping("/batch/marcar-en-proceso")
    public ResponseEntity<Map<String, Object>> marcarEnProcesoBatch(
            @Valid @RequestBody MarcarEnProcesoRequest request
    ) {
        log.info("POST /api/alertas-inventario/batch/marcar-en-proceso - {} alertas",
                request.getAlertaIds().size());

        try {
            List<AlertaInventarioResponse> alertasActualizadas = 
                    alertaService.marcarAlertasEnProcesoBatch(
                            request.getAlertaIds(),
                            request.getUsuarioId(),
                            request.getObservaciones()
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("alertasActualizadas", alertasActualizadas);
            response.put("totalActualizadas", alertasActualizadas.size());
            response.put("mensaje", String.format("%d alertas marcadas como EN_PROCESO",
                    alertasActualizadas.size()));

            log.info("Batch completado: {} alertas marcadas EN_PROCESO", alertasActualizadas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en batch marcar-en-proceso: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", "Error al procesar alertas: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Resuelve múltiples alertas en batch.
     * 
     * POST /api/alertas-inventario/batch/resolver
     * 
     * Body:
     * {
     *   "alertaIds": [1, 2, 3],
     *   "accionTomada": "Órdenes de compra generadas",
     *   "usuarioId": 1
     * }
     * 
     * @param request IDs de alertas y acción tomada
     * @return Resultado de la operación batch
     */
    @PostMapping("/batch/resolver")
    public ResponseEntity<Map<String, Object>> resolverAlertasBatch(
            @Valid @RequestBody ResolverAlertasBatchRequest request
    ) {
        log.info("POST /api/alertas-inventario/batch/resolver - {} alertas",
                request.getAlertaIds().size());

        try {
            List<AlertaInventarioResponse> alertasResueltas = 
                    alertaService.resolverAlertasBatch(
                            request.getAlertaIds(),
                            request.getAccionTomada(),
                            request.getUsuarioId()
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("alertasResueltas", alertasResueltas);
            response.put("totalResueltas", alertasResueltas.size());
            response.put("mensaje", String.format("%d alertas resueltas exitosamente",
                    alertasResueltas.size()));

            log.info("Batch completado: {} alertas resueltas", alertasResueltas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en batch resolver: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", "Error al resolver alertas: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Ignora múltiples alertas en batch.
     * 
     * POST /api/alertas-inventario/batch/ignorar
     * 
     * Body:
     * {
     *   "alertaIds": [1, 2, 3],
     *   "motivo": "Stock suficiente en bodega alternativa",
     *   "usuarioId": 1
     * }
     * 
     * @param request IDs de alertas y motivo
     * @return Resultado de la operación batch
     */
    @PostMapping("/batch/ignorar")
    public ResponseEntity<Map<String, Object>> ignorarAlertasBatch(
            @Valid @RequestBody IgnorarAlertasBatchRequest request
    ) {
        log.info("POST /api/alertas-inventario/batch/ignorar - {} alertas",
                request.getAlertaIds().size());

        try {
            List<AlertaInventarioResponse> alertasIgnoradas = 
                    alertaService.ignorarAlertasBatch(
                            request.getAlertaIds(),
                            request.getMotivo(),
                            request.getUsuarioId()
                    );

            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("alertasIgnoradas", alertasIgnoradas);
            response.put("totalIgnoradas", alertasIgnoradas.size());
            response.put("mensaje", String.format("%d alertas ignoradas",
                    alertasIgnoradas.size()));

            log.info("Batch completado: {} alertas ignoradas", alertasIgnoradas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en batch ignorar: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exitoso", false);
            errorResponse.put("mensaje", "Error al ignorar alertas: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene estadísticas de alertas.
     * 
     * GET /api/alertas-inventario/estadisticas
     * 
     * Retorna métricas como:
     * - Total de alertas por estado
     * - Total de alertas por criticidad
     * - Total de alertas por tipo
     * - Alertas más antiguas pendientes
     * 
     * @return Estadísticas de alertas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("GET /api/alertas-inventario/estadisticas");

        try {
            List<AlertaInventarioResponse> todasLasAlertas = alertaService.listarAlertas();

            Map<String, Object> estadisticas = new HashMap<>();

            // Total general
            estadisticas.put("totalAlertas", todasLasAlertas.size());

            // Por estado
            Map<String, Long> porEstado = new HashMap<>();
            todasLasAlertas.forEach(a -> 
                    porEstado.merge(a.getEstado(), 1L, Long::sum)
            );
            estadisticas.put("alertasPorEstado", porEstado);

            // Por criticidad
            Map<String, Long> porCriticidad = new HashMap<>();
            todasLasAlertas.forEach(a -> 
                    porCriticidad.merge(a.getNivelCriticidad(), 1L, Long::sum)
            );
            estadisticas.put("alertasPorCriticidad", porCriticidad);

            // Por tipo
            Map<String, Long> porTipo = new HashMap<>();
            todasLasAlertas.forEach(a -> 
                    porTipo.merge(a.getTipoAlerta(), 1L, Long::sum)
            );
            estadisticas.put("alertasPorTipo", porTipo);

            // Alertas pendientes más antiguas (top 5)
            List<AlertaInventarioResponse> maAntiguas = todasLasAlertas.stream()
                    .filter(a -> "PENDIENTE".equals(a.getEstado()))
                    .sorted((a1, a2) -> a1.getFechaGeneracion().compareTo(a2.getFechaGeneracion()))
                    .limit(5)
                    .toList();
            estadisticas.put("alertasMasAntiguas", maAntiguas);

            log.info("Estadísticas generadas: {} alertas totales", todasLasAlertas.size());

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            log.error("Error al generar estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
