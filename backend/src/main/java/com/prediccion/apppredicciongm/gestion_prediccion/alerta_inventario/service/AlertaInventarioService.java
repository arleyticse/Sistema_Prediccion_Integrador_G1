package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ActualizarEstadoRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.CrearAlertaRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaDashboardDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaInventarioResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.AlertaInventarioNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.AlertaYaResueltaException;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.mapper.AlertaInventarioMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de gestion de alertas de inventario.
 * 
 * Proporciona operaciones CRUD y logica de negocio para alertas
 * de inventario, incluyendo agrupacion por proveedor.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AlertaInventarioService implements IAlertaInventarioService {

    private final IAlertaInventarioRepositorio alertaRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IInventarioRepositorio inventarioRepositorio;
    private final IUsuarioRepository usuarioRepositorio;
    private final AlertaInventarioMapper alertaMapper;
    private final ProveedorService proveedorService;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaInventarioResponse> obtenerAlertasPendientes() {
        log.info("Consultando alertas pendientes");
        
        List<AlertaInventario> alertas = alertaRepositorio.findAlertasPendientes();
        log.debug("Se encontraron {} alertas pendientes", alertas.size());
        
        return alertaMapper.toResponseList(alertas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaDashboardDTO> obtenerAlertasParaDashboard() {
        log.info("Consultando alertas para dashboard con informacion de proveedor");
        
        List<AlertaInventario> alertas = alertaRepositorio.findAlertasPendientesConProducto();
        
        // Enriquecer alertas con datos de inventario si tienen campos NULL
        alertas.forEach(alerta -> {
            if (alerta.getStockActual() == null || alerta.getStockMinimo() == null) {
                Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(
                    alerta.getProducto().getProductoId()
                );
                if (inventarioOpt.isPresent()) {
                    Inventario inv = inventarioOpt.get();
                    if (alerta.getStockActual() == null) {
                        alerta.setStockActual(inv.getStockDisponible());
                    }
                    if (alerta.getStockMinimo() == null) {
                        alerta.setStockMinimo(inv.getStockMinimo());
                    }
                }
            }
        });
        
        List<AlertaDashboardDTO> dashboardDTOs = alertaMapper.toDashboardDTOList(alertas);
        
        // Enriquecer con informacion del proveedor principal
        dashboardDTOs.forEach(dto -> {
            var proveedorInfo = proveedorService.obtenerProveedorPrincipal(dto.getProductoId());
            if (proveedorInfo != null) {
                dto.setProveedorId(proveedorInfo.getProveedorId());
                dto.setProveedorNombreComercial(proveedorInfo.getNombreComercial());
                dto.setProveedorTiempoEntrega(proveedorInfo.getTiempoEntregaDias());
            } else {
                log.warn("No se encontro proveedor principal para producto ID: {}", dto.getProductoId());
                dto.setProveedorNombreComercial("Sin proveedor asignado");
            }
        });
        
        log.debug("Se prepararon {} alertas para dashboard", dashboardDTOs.size());
        return dashboardDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<AlertaDashboardDTO>> agruparAlertasPorProveedor() {
        log.info("Agrupando alertas por proveedor");
        
        List<AlertaDashboardDTO> alertas = obtenerAlertasParaDashboard();
        
        Map<String, List<AlertaDashboardDTO>> agrupadas = alertas.stream()
            .collect(Collectors.groupingBy(
                alerta -> alerta.getProveedorNombreComercial() != null 
                    ? alerta.getProveedorNombreComercial() 
                    : "Sin proveedor",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        log.debug("Alertas agrupadas en {} proveedores", agrupadas.size());
        return agrupadas;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaInventarioResponse obtenerAlertaPorId(Long alertaId) {
        log.info("Consultando alerta ID: {}", alertaId);
        
        AlertaInventario alerta = alertaRepositorio.findById(alertaId)
            .orElseThrow(() -> {
                log.error("Alerta no encontrada con ID: {}", alertaId);
                return new AlertaInventarioNoEncontradaException(alertaId);
            });
        
        return alertaMapper.toResponse(alerta);
    }

    @Override
    public AlertaInventarioResponse crearAlerta(CrearAlertaRequest request) {
        log.info("Creando nueva alerta manual para producto ID: {}", request.getProductoId());
        
        // Validar producto
        Producto producto = productoRepositorio.findById(request.getProductoId())
            .orElseThrow(() -> {
                log.error("Producto no encontrado: {}", request.getProductoId());
                return new IllegalArgumentException("Producto no encontrado");
            });
        
        // Verificar duplicado
        if (alertaRepositorio.existeAlertaPendiente(
                request.getProductoId(), 
                request.getTipoAlerta())) {
            log.warn("Ya existe alerta pendiente del mismo tipo para producto ID: {}", 
                request.getProductoId());
            throw new IllegalStateException(
                "Ya existe una alerta pendiente del tipo " + request.getTipoAlerta() + 
                " para este producto"
            );
        }
        
        // Crear entidad
        AlertaInventario alerta = new AlertaInventario();
        alerta.setTipoAlerta(request.getTipoAlerta());
        alerta.setNivelCriticidad(request.getNivelCriticidad());
        alerta.setMensaje(request.getMensaje());
        alerta.setProducto(producto);
        alerta.setStockActual(request.getStockActual());
        alerta.setStockMinimo(request.getStockMinimo());
        alerta.setCantidadSugerida(request.getCantidadSugerida());
        alerta.setObservaciones(request.getObservaciones());
        
        // Asignar usuario si se proporciono
        if (request.getUsuarioAsignadoId() != null) {
            Usuario usuario = usuarioRepositorio.findById(request.getUsuarioAsignadoId())
                .orElse(null);
            alerta.setUsuarioAsignado(usuario);
        }
        
        AlertaInventario alertaGuardada = alertaRepositorio.save(alerta);
        log.info("Alerta creada exitosamente con ID: {}", alertaGuardada.getAlertaId());
        
        return alertaMapper.toResponse(alertaGuardada);
    }

    @Override
    public AlertaInventarioResponse actualizarEstado(Long alertaId, ActualizarEstadoRequest request) {
        log.info("Actualizando estado de alerta ID: {} a estado: {}", alertaId, request.getNuevoEstado());
        
        AlertaInventario alerta = alertaRepositorio.findById(alertaId)
            .orElseThrow(() -> new AlertaInventarioNoEncontradaException(alertaId));
        
        // Validar que no este ya resuelta
        if (alerta.getEstado() == EstadoAlerta.RESUELTA) {
            log.warn("Intento de modificar alerta ya resuelta ID: {}", alertaId);
            throw new AlertaYaResueltaException(alertaId);
        }
        
        // Actualizar segun el nuevo estado
        switch (request.getNuevoEstado()) {
            case EN_PROCESO:
                if (request.getUsuarioId() != null) {
                    Usuario usuario = usuarioRepositorio.findById(request.getUsuarioId()).orElse(null);
                    alerta.marcarEnProceso(usuario);
                }
                break;
                
            case RESUELTA:
                alerta.resolver(request.getAccionTomada());
                break;
                
            case IGNORADA:
                alerta.ignorar(request.getObservaciones());
                break;
                
            default:
                alerta.setEstado(request.getNuevoEstado());
                break;
        }
        
        if (request.getObservaciones() != null) {
            alerta.setObservaciones(request.getObservaciones());
        }
        
        AlertaInventario alertaActualizada = alertaRepositorio.save(alerta);
        log.info("Alerta ID: {} actualizada a estado: {}", alertaId, request.getNuevoEstado());
        
        return alertaMapper.toResponse(alertaActualizada);
    }

    @Override
    public void marcarAlertasComoResueltas(List<Long> alertaIds, String accionTomada) {
        log.info("Marcando {} alertas como resueltas", alertaIds.size());
        
        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        
        alertas.forEach(alerta -> {
            if (alerta.getEstado() != EstadoAlerta.RESUELTA) {
                alerta.resolver(accionTomada);
            }
        });
        
        alertaRepositorio.saveAll(alertas);
        log.info("Se marcaron {} alertas como resueltas", alertas.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarAlertasPendientesPorCriticidad(NivelCriticidad criticidad) {
        return alertaRepositorio.contarAlertasPendientesPorCriticidad(criticidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        log.info("Generando estadisticas de alertas");
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total pendientes
        Long totalPendientes = alertaRepositorio.contarAlertasPendientes();
        estadisticas.put("totalPendientes", totalPendientes);
        
        // Por criticidad
        Map<String, Long> porCriticidad = new HashMap<>();
        porCriticidad.put("CRITICA", contarAlertasPendientesPorCriticidad(NivelCriticidad.CRITICA));
        porCriticidad.put("ALTA", contarAlertasPendientesPorCriticidad(NivelCriticidad.ALTA));
        porCriticidad.put("MEDIA", contarAlertasPendientesPorCriticidad(NivelCriticidad.MEDIA));
        porCriticidad.put("BAJA", contarAlertasPendientesPorCriticidad(NivelCriticidad.BAJA));
        estadisticas.put("porCriticidad", porCriticidad);
        
        // Por estado
        Map<String, Long> porEstado = new HashMap<>();
        porEstado.put("PENDIENTE", alertaRepositorio.findByEstado(EstadoAlerta.PENDIENTE).stream().count());
        porEstado.put("EN_PROCESO", alertaRepositorio.findByEstado(EstadoAlerta.EN_PROCESO).stream().count());
        porEstado.put("RESUELTA", alertaRepositorio.findByEstado(EstadoAlerta.RESUELTA).stream().count());
        estadisticas.put("porEstado", porEstado);
        
        
        log.debug("Estadisticas generadas: {} alertas pendientes", totalPendientes);
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaInventarioResponse> listarAlertas() {
        log.info("Listando todas las alertas");
        List<AlertaInventario> alertas = alertaRepositorio.findAll();
        return alertaMapper.toResponseList(alertas);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AlertaInventarioResponse> listarAlertasFiltradas(
            EstadoAlerta estado,
            NivelCriticidad criticidad,
            com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta tipoAlerta,
            Integer productoId,
            Integer proveedorId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            org.springframework.data.domain.Pageable pageable
    ) {
        log.info("Listando alertas filtradas - Estado: {}, Criticidad: {}, Tipo: {}", estado, criticidad, tipoAlerta);

        // Obtener todas las alertas (en producción usar Specification para query dinámica)
        List<AlertaInventario> todasLasAlertas = alertaRepositorio.findAll();

        // Aplicar filtros manualmente
        List<AlertaInventario> alertasFiltradas = todasLasAlertas.stream()
                .filter(a -> estado == null || a.getEstado() == estado)
                .filter(a -> criticidad == null || a.getNivelCriticidad() == criticidad)
                .filter(a -> tipoAlerta == null || a.getTipoAlerta() == tipoAlerta)
                .filter(a -> productoId == null || (a.getProducto() != null && a.getProducto().getProductoId().equals(productoId)))
                .filter(a -> fechaDesde == null || !a.getFechaGeneracion().isBefore(fechaDesde))
                .filter(a -> fechaHasta == null || !a.getFechaGeneracion().isAfter(fechaHasta))
                .toList();

        // Convertir a Page manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alertasFiltradas.size());
        
        List<AlertaInventario> paginadas = alertasFiltradas.subList(start, end);
        List<AlertaInventarioResponse> respuestas = alertaMapper.toResponseList(paginadas);

        return new org.springframework.data.domain.PageImpl<>(respuestas, pageable, alertasFiltradas.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<AlertaInventarioResponse>> obtenerAlertasAgrupadasPorProveedor() {
        log.info("Obteniendo alertas agrupadas por proveedor");

        List<AlertaInventario> alertasPendientes = alertaRepositorio.findAlertasPendientes();
        Map<Integer, List<AlertaInventarioResponse>> agrupadas = new HashMap<>();

        for (AlertaInventario alerta : alertasPendientes) {
            if (alerta.getProducto() != null) {
                var proveedorInfo = proveedorService.obtenerProveedorPrincipal(alerta.getProducto().getProductoId());
                
                if (proveedorInfo != null) {
                    Integer proveedorId = proveedorInfo.getProveedorId();
                    agrupadas.computeIfAbsent(proveedorId, k -> new ArrayList<>())
                            .add(alertaMapper.toResponse(alerta));
                } else {
                    // Agrupar con ID 0 para "sin proveedor"
                    agrupadas.computeIfAbsent(0, k -> new ArrayList<>())
                            .add(alertaMapper.toResponse(alerta));
                }
            }
        }

        log.info("Alertas agrupadas en {} proveedores", agrupadas.size());
        return agrupadas;
    }

    @Override
    public AlertaInventarioResponse actualizarEstadoAlerta(
            Long alertaId,
            EstadoAlerta nuevoEstado,
            String observaciones,
            Integer usuarioId
    ) {
        log.info("Actualizando estado de alerta {} a {}", alertaId, nuevoEstado);

        AlertaInventario alerta = alertaRepositorio.findById(alertaId)
                .orElseThrow(() -> new AlertaInventarioNoEncontradaException(alertaId));

        if (alerta.getEstado() == EstadoAlerta.RESUELTA) {
            throw new AlertaYaResueltaException(alertaId);
        }

        // Actualizar estado
        alerta.setEstado(nuevoEstado);

        if (observaciones != null) {
            alerta.setObservaciones(observaciones);
        }

        if (usuarioId != null) {
            Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
            alerta.setUsuarioAsignado(usuario);
        }

        AlertaInventario actualizada = alertaRepositorio.save(alerta);
        return alertaMapper.toResponse(actualizada);
    }

    @Override
    public List<AlertaInventarioResponse> marcarAlertasEnProcesoBatch(
            List<Long> alertaIds,
            Integer usuarioId,
            String observaciones
    ) {
        log.info("Marcando {} alertas como EN_PROCESO", alertaIds.size());

        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        Usuario usuario = usuarioId != null ? usuarioRepositorio.findById(usuarioId).orElse(null) : null;

        List<AlertaInventario> actualizadas = new ArrayList<>();

        for (AlertaInventario alerta : alertas) {
            if (alerta.getEstado() != EstadoAlerta.RESUELTA) {
                alerta.setEstado(EstadoAlerta.EN_PROCESO);
                alerta.setUsuarioAsignado(usuario);
                
                if (observaciones != null) {
                    alerta.setObservaciones(observaciones);
                }
                
                actualizadas.add(alerta);
            }
        }

        List<AlertaInventario> guardadas = alertaRepositorio.saveAll(actualizadas);
        return alertaMapper.toResponseList(guardadas);
    }

    @Override
    public List<AlertaInventarioResponse> resolverAlertasBatch(
            List<Long> alertaIds,
            String accionTomada,
            Integer usuarioId
    ) {
        log.info("Resolviendo {} alertas en batch", alertaIds.size());

        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        Usuario usuario = usuarioId != null ? usuarioRepositorio.findById(usuarioId).orElse(null) : null;

        List<AlertaInventario> resueltas = new ArrayList<>();

        for (AlertaInventario alerta : alertas) {
            if (alerta.getEstado() != EstadoAlerta.RESUELTA) {
                alerta.setEstado(EstadoAlerta.RESUELTA);
                alerta.setUsuarioAsignado(usuario);
                
                String observacionesFinal = "Resuelta: " + (accionTomada != null ? accionTomada : "Sin descripción");
                alerta.setObservaciones(observacionesFinal);
                
                resueltas.add(alerta);
            }
        }

        List<AlertaInventario> guardadas = alertaRepositorio.saveAll(resueltas);
        return alertaMapper.toResponseList(guardadas);
    }

    @Override
    public List<AlertaInventarioResponse> ignorarAlertasBatch(
            List<Long> alertaIds,
            String motivo,
            Integer usuarioId
    ) {
        log.info("Ignorando {} alertas en batch", alertaIds.size());

        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        Usuario usuario = usuarioId != null ? usuarioRepositorio.findById(usuarioId).orElse(null) : null;

        List<AlertaInventario> ignoradas = new ArrayList<>();

        for (AlertaInventario alerta : alertas) {
            if (alerta.getEstado() != EstadoAlerta.RESUELTA) {
                alerta.setEstado(EstadoAlerta.IGNORADA);
                alerta.setUsuarioAsignado(usuario);
                
                String observacionesFinal = "Ignorada: " + (motivo != null ? motivo : "Sin motivo especificado");
                alerta.setObservaciones(observacionesFinal);
                
                ignoradas.add(alerta);
            }
        }

        List<AlertaInventario> guardadas = alertaRepositorio.saveAll(ignoradas);
        return alertaMapper.toResponseList(guardadas);
    }
}

