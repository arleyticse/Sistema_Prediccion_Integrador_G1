package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.JobExecutionResultDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Prediccion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de jobs programados para la generación automática de alertas de
 * inventario.
 * 
 * Ejecuta tareas programadas (@Scheduled) para:
 * - Detectar stock bajo vs punto de reorden
 * - Detectar predicciones vencidas
 * - Detectar estacionalidad próxima
 * - Detectar stock crítico
 * 
 * Intervalos configurables mediante application.properties
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaInventarioJobService {

    private final IInventarioRepositorio inventarioRepositorio;
    private final IPrediccionRepositorio prediccionRepositorio;
    private final IAlertaInventarioRepositorio alertaRepositorio;

    /**
     * Habilita/deshabilita la ejecución de jobs.
     * Default: true
     */
    @Value("${alerta.job.enabled:true}")
    private Boolean jobEnabled;

    /**
     * Habilita/deshabilita detección de stock bajo.
     * Default: true
     */
    @Value("${alerta.job.stock-bajo.enabled:true}")
    private Boolean stockBajoEnabled;

    /**
     * Habilita/deshabilita detección de predicciones vencidas.
     * Default: true
     */
    @Value("${alerta.job.prediccion-vencida.enabled:true}")
    private Boolean prediccionVencidaEnabled;

    /**
     * Habilita/deshabilita detección de estacionalidad próxima.
     * Default: true
     */
    @Value("${alerta.job.estacionalidad.enabled:true}")
    private Boolean estacionalidadEnabled;

    /**
     * Días antes del mes estacional para generar alerta.
     * Default: 30 días
     */
    @Value("${alerta.job.estacionalidad.dias-anticipacion:30}")
    private Integer diasAnticipacionEstacionalidad;

    /**
     * Días máximos de validez de una predicción.
     * Default: 90 días
     */
    @Value("${alerta.job.prediccion.dias-validez:90}")
    private Integer diasValidezPrediccion;

    /**
     * Job programado: Ejecuta todos los detectores de alertas.
     * 
     * Configuración mediante cron expression en application.properties:
     * alerta.job.cron
     * 
     * Default: Cada 12 horas (0 0 0/12 * * ?)
     * 
     * Ejemplos:
     * - Cada 6 horas: 0 0 0/6 * * ?
     * - Cada 24 horas a las 2:00 AM: 0 0 2 * * ?
     * - Cada 1 hora: 0 0 * * * ?
     */
    @Scheduled(cron = "${alerta.job.cron:0 0 0/12 * * ?}")
    @Transactional
    public void ejecutarGeneracionAutomaticaAlertas() {
        if (!jobEnabled) {
            log.debug("Jobs de alertas deshabilitados en configuración");
            return;
        }

        log.info("=== INICIANDO JOB DE GENERACIÓN AUTOMÁTICA DE ALERTAS ===");
        LocalDateTime inicioEjecucion = LocalDateTime.now();

        try {
            // Ejecutar detectores habilitados
            JobExecutionResultDTO resultadoStockBajo = null;
            JobExecutionResultDTO resultadoPrediccionVencida = null;
            JobExecutionResultDTO resultadoEstacionalidad = null;

            if (stockBajoEnabled) {
                resultadoStockBajo = detectarStockBajo();
                log.info("Stock Bajo - Alertas generadas: {}",
                        resultadoStockBajo.getTotalAlertasGeneradas());
            }

            if (prediccionVencidaEnabled) {
                resultadoPrediccionVencida = detectarPrediccionesVencidas();
                log.info("Predicciones Vencidas - Alertas generadas: {}",
                        resultadoPrediccionVencida.getTotalAlertasGeneradas());
            }

            if (estacionalidadEnabled) {
                resultadoEstacionalidad = detectarEstacionalidadProxima();
                log.info("Estacionalidad Próxima - Alertas generadas: {}",
                        resultadoEstacionalidad.getTotalAlertasGeneradas());
            }

            // Resumen de ejecución
            int totalAlertas = (resultadoStockBajo != null ? resultadoStockBajo.getTotalAlertasGeneradas() : 0) +
                    (resultadoPrediccionVencida != null ? resultadoPrediccionVencida.getTotalAlertasGeneradas() : 0) +
                    (resultadoEstacionalidad != null ? resultadoEstacionalidad.getTotalAlertasGeneradas() : 0);

            long tiempoTotal = java.time.Duration.between(inicioEjecucion, LocalDateTime.now()).toMillis();

            log.info("=== JOB COMPLETADO - Total: {} alertas en {}ms ===",
                    totalAlertas, tiempoTotal);

        } catch (Exception e) {
            log.error("Error durante la ejecución del job de alertas", e);
        }
    }

    /**
     * Detecta inventarios con stock bajo vs punto de reorden.
     * 
     * Criterios:
     * - CRITICA: stock <= 0 (agotado)
     * - ALTA: stock < stock_minimo
     * - MEDIA: stock <= punto_reorden
     * - BAJA: stock <= (punto_reorden * 1.2)
     * 
     * @return Resultado de la ejecución con métricas
     */
    @Transactional
    public JobExecutionResultDTO detectarStockBajo() {
        LocalDateTime inicio = LocalDateTime.now();

        JobExecutionResultDTO resultado = JobExecutionResultDTO.builder()
                .nombreJob("Detección de Stock Bajo")
                .fechaInicio(inicio)
                .build();

        try {
            // Obtener inventarios activos (todos excepto OBSOLETO y BLOQUEADO)
            List<String> estadosValidos = List.of("NORMAL", "BAJO", "CRITICO", "EXCESO");
            List<Inventario> inventarios = inventarioRepositorio.findAll()
                    .stream()
                    .filter(i -> i.getEstado() != null &&
                            estadosValidos.contains(i.getEstado().toString()))
                    .toList();

            resultado.setProductosAnalizados(inventarios.size());

            for (Inventario inventario : inventarios) {
                try {
                    // Validar datos necesarios
                    if (inventario.getProducto() == null)
                        continue;

                    Integer stockActual = inventario.getStockDisponible() != null
                            ? inventario.getStockDisponible()
                            : 0;
                    Integer puntoReorden = inventario.getPuntoReorden() != null
                            ? inventario.getPuntoReorden()
                            : 0;
                    Integer stockMinimo = inventario.getStockMinimo() != null
                            ? inventario.getStockMinimo()
                            : 0;

                    // Determinar si requiere alerta
                    TipoAlerta tipoAlerta = null;
                    NivelCriticidad criticidad = null;
                    String descripcion = null;
                    Integer cantidadSugerida = null;

                    if (stockActual <= 0) {
                        // CRÍTICO: Stock agotado
                        tipoAlerta = TipoAlerta.STOCK_CRITICO;
                        criticidad = NivelCriticidad.CRITICA;
                        descripcion = "Stock completamente agotado. Requiere reposición URGENTE.";
                        cantidadSugerida = Math.max(puntoReorden * 2, stockMinimo * 3);

                    } else if (stockMinimo > 0 && stockActual < stockMinimo) {
                        // ALTA: Por debajo del stock mínimo
                        tipoAlerta = TipoAlerta.STOCK_BAJO;
                        criticidad = NivelCriticidad.ALTA;
                        descripcion = String.format(
                                "Stock actual (%d) por debajo del mínimo (%d).",
                                stockActual, stockMinimo);
                        cantidadSugerida = stockMinimo * 2 - stockActual;

                    } else if (puntoReorden > 0 && stockActual <= puntoReorden) {
                        // MEDIA: En punto de reorden
                        tipoAlerta = TipoAlerta.PUNTO_REORDEN;
                        criticidad = NivelCriticidad.MEDIA;
                        descripcion = String.format(
                                "Stock actual (%d) alcanzó el punto de reorden (%d).",
                                stockActual, puntoReorden);
                        cantidadSugerida = puntoReorden * 2 - stockActual;

                    } else if (puntoReorden > 0 && stockActual <= (puntoReorden * 1.2)) {
                        // BAJA: Cerca del punto de reorden
                        tipoAlerta = TipoAlerta.STOCK_BAJO;
                        criticidad = NivelCriticidad.BAJA;
                        descripcion = String.format(
                                "Stock actual (%d) cercano al punto de reorden (%d).",
                                stockActual, puntoReorden);
                        cantidadSugerida = puntoReorden - stockActual + 50;
                    }

                    // Crear o actualizar alerta
                    if (tipoAlerta != null) {
                        crearOActualizarAlerta(
                                inventario.getProducto().getProductoId(),
                                tipoAlerta,
                                criticidad,
                                descripcion,
                                cantidadSugerida,
                                resultado,
                                stockActual,
                                stockMinimo,
                                puntoReorden);
                    }

                } catch (Exception e) {
                    resultado.setErroresEncontrados(resultado.getErroresEncontrados() + 1);
                    resultado.getMensajesError().add(
                            "Error en producto " + inventario.getProducto().getProductoId() + ": " + e.getMessage());
                }
            }

            resultado.setExitoso(resultado.getErroresEncontrados() == 0);

        } catch (Exception e) {
            resultado.setExitoso(false);
            resultado.getMensajesError().add("Error general: " + e.getMessage());
            log.error("Error al detectar stock bajo", e);
        }

        resultado.setFechaFin(LocalDateTime.now());
        resultado.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, resultado.getFechaFin()).toMillis());

        return resultado;
    }

    /**
     * Detecta predicciones vencidas que requieren actualización.
     * 
     * Criterios:
     * - ALTA: Predicción vencida hace más de 30 días
     * - MEDIA: Predicción vencida hace menos de 30 días
     * - BAJA: Predicción próxima a vencer (menos de 15 días)
     * 
     * @return Resultado de la ejecución con métricas
     */
    @Transactional
    public JobExecutionResultDTO detectarPrediccionesVencidas() {
        LocalDateTime inicio = LocalDateTime.now();

        JobExecutionResultDTO resultado = JobExecutionResultDTO.builder()
                .nombreJob("Detección de Predicciones Vencidas")
                .fechaInicio(inicio)
                .build();

        try {
            List<Prediccion> predicciones = prediccionRepositorio.findPrediccionesVigentes();
            // Procesar solo la última predicción por producto para reducir carga
            java.util.Set<Integer> productosProcesados = new java.util.HashSet<>();
            resultado.setProductosAnalizados(predicciones.size());

            LocalDate hoy = LocalDate.now();
            LocalDate limiteProximoVencer = hoy.plusDays(15);

            for (Prediccion prediccion : predicciones) {
                try {
                    if (prediccion.getProducto() == null || prediccion.getFechaEjecucion() == null) {
                        continue;
                    }

                    Integer pid = prediccion.getProducto().getProductoId();
                    if (pid != null) {
                        if (productosProcesados.contains(pid)) {
                            continue;
                        }
                        productosProcesados.add(pid);
                    }

                    // Calcular fecha esperada de vencimiento (fecha + horizonte)
                    LocalDateTime fechaEjecucion = prediccion.getFechaEjecucion();
                    Integer horizonteDias = prediccion.getHorizonteTiempo() != null
                            ? prediccion.getHorizonteTiempo()
                            : diasValidezPrediccion;

                    LocalDate fechaVencimiento = fechaEjecucion.toLocalDate().plusDays(horizonteDias);
                    long diasDesdeVencimiento = java.time.temporal.ChronoUnit.DAYS.between(fechaVencimiento, hoy);

                    TipoAlerta tipoAlerta = null;
                    NivelCriticidad criticidad = null;
                    String descripcion = null;

                    if (diasDesdeVencimiento > 30) {
                        // ALTA: Vencida hace más de 30 días
                        tipoAlerta = TipoAlerta.PREDICCION_VENCIDA;
                        criticidad = NivelCriticidad.ALTA;
                        descripcion = String.format(
                                "Predicción vencida hace %d días. Requiere actualización urgente.",
                                diasDesdeVencimiento);

                    } else if (diasDesdeVencimiento > 0) {
                        // MEDIA: Vencida recientemente
                        tipoAlerta = TipoAlerta.PREDICCION_VENCIDA;
                        criticidad = NivelCriticidad.MEDIA;
                        descripcion = String.format(
                                "Predicción vencida hace %d días.",
                                diasDesdeVencimiento);

                    } else if (fechaVencimiento.isBefore(limiteProximoVencer)) {
                        // BAJA: Próxima a vencer
                        long diasHastaVencer = Math.abs(diasDesdeVencimiento);
                        tipoAlerta = TipoAlerta.PREDICCION_VENCIDA;
                        criticidad = NivelCriticidad.BAJA;
                        descripcion = String.format(
                                "Predicción vence en %d días.",
                                diasHastaVencer);
                    }

                    if (tipoAlerta != null) {
                        crearOActualizarAlerta(
                                prediccion.getProducto().getProductoId(),
                                tipoAlerta,
                                criticidad,
                                descripcion,
                                null, // No aplica cantidad sugerida
                                resultado,
                                null,
                                null,
                                null);
                    }

                } catch (Exception e) {
                    resultado.setErroresEncontrados(resultado.getErroresEncontrados() + 1);
                    resultado.getMensajesError().add(
                            "Error en predicción " + prediccion.getPrediccionId() + ": " + e.getMessage());
                }
            }

            resultado.setExitoso(resultado.getErroresEncontrados() == 0);

        } catch (Exception e) {
            resultado.setExitoso(false);
            resultado.getMensajesError().add("Error general: " + e.getMessage());
            log.error("Error al detectar predicciones vencidas", e);
        }

        resultado.setFechaFin(LocalDateTime.now());
        resultado.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, resultado.getFechaFin()).toMillis());

        return resultado;
    }

    /**
     * Detecta periodos estacionales próximos.
     * 
     * Criterios:
     * - ALTA: Estacionalidad comienza en menos de 15 días
     * - MEDIA: Estacionalidad comienza en 15-30 días
     * - BAJA: Estacionalidad comienza en 30-45 días
     * 
     * @return Resultado de la ejecución con métricas
     */
    /**
     * MÉTODO OBSOLETO - Desactivado tras eliminación de tabla
     * estacionalidad_producto
     * 
     * Este método fue reemplazado por el sistema moderno de análisis de
     * estacionalidad
     * que utiliza la tabla analisis_estacionalidad con coeficientes mensuales.
     * 
     * @deprecated Usar AnalisisEstacionalidadService en su lugar
     * @see com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service.AnalisisEstacionalidadService
     */
    @Deprecated
    @Transactional
    public JobExecutionResultDTO detectarEstacionalidadProxima() {
        LocalDateTime inicio = LocalDateTime.now();

        JobExecutionResultDTO resultado = JobExecutionResultDTO.builder()
                .nombreJob("Detección de Estacionalidad Próxima (OBSOLETO)")
                .fechaInicio(inicio)
                .exitoso(false)
                .build();

        resultado.getMensajesError().add(
                "Este método fue desactivado. Tabla estacionalidad_producto eliminada. " +
                        "Usar AnalisisEstacionalidadService para análisis de estacionalidad moderno.");

        resultado.setFechaFin(LocalDateTime.now());
        return resultado;

        /*
         * CÓDIGO ORIGINAL COMENTADO - Usaba tabla eliminada estacionalidad_producto
         * 
         * try {
         * List<EstacionalidadProducto> estacionalidades =
         * estacionalidadRepositorio.findAll();
         * resultado.setProductosAnalizados(estacionalidades.size());
         * 
         * int mesActual = LocalDate.now().getMonthValue();
         * int anoActual = LocalDate.now().getYear();
         * 
         * for (EstacionalidadProducto estacionalidad : estacionalidades) {
         * try {
         * if (estacionalidad.getProducto() == null || estacionalidad.getMes() == null)
         * {
         * continue;
         * }
         * 
         * int mesInicio = estacionalidad.getMes();
         * 
         * // Calcular inicio del periodo estacional
         * LocalDate inicioEstacionalidad = LocalDate.of(
         * mesInicio < mesActual ? anoActual + 1 : anoActual,
         * mesInicio,
         * 1
         * );
         * 
         * long diasHastaInicio = java.time.temporal.ChronoUnit.DAYS.between(
         * LocalDate.now(),
         * inicioEstacionalidad
         * );
         * 
         * // Solo alertar si está dentro del rango de anticipación
         * if (diasHastaInicio > 0 && diasHastaInicio <= diasAnticipacionEstacionalidad)
         * {
         * 
         * TipoAlerta tipoAlerta = TipoAlerta.VENCIMIENTO_PROXIMO;
         * NivelCriticidad criticidad;
         * String descripcion;
         * 
         * if (diasHastaInicio <= 15) {
         * criticidad = NivelCriticidad.ALTA;
         * descripcion = String.format(
         * "Periodo estacional inicia en %d días. Factor estimado: %.2f",
         * diasHastaInicio,
         * estacionalidad.getFactorEstacional()
         * );
         * } else if (diasHastaInicio <= 30) {
         * criticidad = NivelCriticidad.MEDIA;
         * descripcion = String.format(
         * "Periodo estacional inicia en %d días. Preparar inventario.",
         * diasHastaInicio
         * );
         * } else {
         * criticidad = NivelCriticidad.BAJA;
         * descripcion = String.format(
         * "Periodo estacional inicia en %d días. Monitorear demanda.",
         * diasHastaInicio
         * );
         * }
         * 
         * // Calcular cantidad sugerida basada en factor estacional
         * Integer cantidadSugerida = null;
         * if (estacionalidad.getFactorEstacional() != null) {
         * Optional<Inventario> inventario = inventarioRepositorio
         * .findByProducto(estacionalidad.getProducto().getProductoId());
         * 
         * if (inventario.isPresent() && inventario.get().getPuntoReorden() != null) {
         * BigDecimal factor = estacionalidad.getFactorEstacional();
         * cantidadSugerida = factor.multiply(
         * new BigDecimal(inventario.get().getPuntoReorden())
         * ).intValue();
         * }
         * }
         * 
         * crearOActualizarAlerta(
         * estacionalidad.getProducto().getProductoId(),
         * tipoAlerta,
         * criticidad,
         * descripcion,
         * cantidadSugerida,
         * resultado
         * );
         * }
         * 
         * } catch (Exception e) {
         * resultado.setErroresEncontrados(resultado.getErroresEncontrados() + 1);
         * resultado.getMensajesError().add(
         * "Error en estacionalidad " + estacionalidad.getEstacionalidadId() + ": " +
         * e.getMessage()
         * );
         * }
         * }
         * 
         * resultado.setExitoso(resultado.getErroresEncontrados() == 0);
         * 
         * } catch (Exception e) {
         * resultado.setExitoso(false);
         * resultado.getMensajesError().add("Error general: " + e.getMessage());
         * log.error("Error al detectar estacionalidad próxima", e);
         * }
         * 
         * resultado.setFechaFin(LocalDateTime.now());
         * resultado.setTiempoEjecucionMs(
         * java.time.Duration.between(inicio, resultado.getFechaFin()).toMillis()
         * );
         * 
         * return resultado;
         */
    }

    /**
     * Crea o actualiza una alerta de inventario.
     * Evita duplicados buscando alertas pendientes del mismo tipo para el producto.
     */
    /**
     * Crea o actualiza una alerta de inventario.
     * Evita duplicados buscando alertas pendientes o recientemente resueltas.
     */
    private void crearOActualizarAlerta(
            Integer productoId,
            TipoAlerta tipoAlerta,
            NivelCriticidad criticidad,
            String descripcion,
            Integer cantidadSugerida,
            JobExecutionResultDTO resultado,
            Integer stockActualOpt,
            Integer stockMinimoOpt,
            Integer puntoReordenOpt) {
        try {
            // Buscar la última alerta generada para este producto y tipo (cualquier estado)
            Optional<AlertaInventario> ultimaAlertaOpt = alertaRepositorio
                    .findTopByProducto_ProductoIdAndTipoAlertaOrderByFechaGeneracionDesc(productoId, tipoAlerta);

            if (ultimaAlertaOpt.isPresent()) {
                AlertaInventario ultimaAlerta = ultimaAlertaOpt.get();

                // Si está PENDIENTE, actualizamos la existente
                if (ultimaAlerta.getEstado() == EstadoAlerta.PENDIENTE) {
                    actualizarAlerta(ultimaAlerta, criticidad, descripcion, cantidadSugerida,
                            stockActualOpt, stockMinimoOpt, puntoReordenOpt, resultado);
                    return;
                }

                // Si está RESUELTA, verificamos si la resolución es reciente
                if (ultimaAlerta.getEstado() == EstadoAlerta.RESUELTA) {
                    // Obtener tiempo de entrega (lead time)
                    int diasEspera = 7; // Default 7 días

                    if (ultimaAlerta.getProducto() != null) {
                        // Prioridad 1: Lead time del producto
                        if (ultimaAlerta.getProducto().getDiasLeadTime() != null) {
                            diasEspera = ultimaAlerta.getProducto().getDiasLeadTime();
                        }
                        // Prioridad 2: Tiempo de entrega del proveedor
                        else if (ultimaAlerta.getProducto().getProveedorPrincipal() != null &&
                                ultimaAlerta.getProducto().getProveedorPrincipal().getTiempoEntregaDias() != null) {
                            diasEspera = ultimaAlerta.getProducto().getProveedorPrincipal().getTiempoEntregaDias();
                        }
                    }

                    // Si se resolvió hace menos tiempo que el lead time, no generamos nueva alerta
                    if (ultimaAlerta.getFechaResolucion() != null) {
                        LocalDateTime fechaLimite = ultimaAlerta.getFechaResolucion().plusDays(diasEspera);
                        if (LocalDateTime.now().isBefore(fechaLimite)) {
                            // Estamos dentro del periodo de espera, no generar duplicado
                            return;
                        }
                    }
                }
            }

            // Si no hay alerta previa, o está resuelta hace mucho, o ignorada, creamos una
            // nueva
            crearNuevaAlerta(productoId, tipoAlerta, criticidad, descripcion, cantidadSugerida,
                    stockActualOpt, stockMinimoOpt, puntoReordenOpt, resultado);

        } catch (Exception e) {
            resultado.setErroresEncontrados(resultado.getErroresEncontrados() + 1);
            resultado.getMensajesError().add(
                    "Error al crear alerta para producto " + productoId + ": " + e.getMessage());
            log.error("Error al crear/actualizar alerta", e);
        }
    }

    private void actualizarAlerta(
            AlertaInventario alerta,
            NivelCriticidad criticidad,
            String descripcion,
            Integer cantidadSugerida,
            Integer stockActualOpt,
            Integer stockMinimoOpt,
            Integer puntoReordenOpt,
            JobExecutionResultDTO resultado) {
        alerta.setNivelCriticidad(criticidad);
        alerta.setMensaje(descripcion);
        alerta.setCantidadSugerida(cantidadSugerida);

        enrichAlertaWithInventoryData(alerta, alerta.getProducto().getProductoId(),
                stockActualOpt, stockMinimoOpt, puntoReordenOpt);

        alertaRepositorio.save(alerta);
        resultado.setAlertasActualizadas(resultado.getAlertasActualizadas() + 1);
    }

    private void crearNuevaAlerta(
            Integer productoId,
            TipoAlerta tipoAlerta,
            NivelCriticidad criticidad,
            String descripcion,
            Integer cantidadSugerida,
            Integer stockActualOpt,
            Integer stockMinimoOpt,
            Integer puntoReordenOpt,
            JobExecutionResultDTO resultado) {
        AlertaInventario nuevaAlerta = new AlertaInventario();
        nuevaAlerta.setProducto(new com.prediccion.apppredicciongm.models.Inventario.Producto());
        nuevaAlerta.getProducto().setProductoId(productoId);
        nuevaAlerta.setTipoAlerta(tipoAlerta);
        nuevaAlerta.setNivelCriticidad(criticidad);
        nuevaAlerta.setMensaje(descripcion);
        nuevaAlerta.setCantidadSugerida(cantidadSugerida);
        nuevaAlerta.setEstado(EstadoAlerta.PENDIENTE);
        nuevaAlerta.setFechaGeneracion(LocalDateTime.now());

        enrichAlertaWithInventoryData(nuevaAlerta, productoId,
                stockActualOpt, stockMinimoOpt, puntoReordenOpt);

        AlertaInventario guardada = alertaRepositorio.save(nuevaAlerta);
        resultado.getAlertasGeneradasIds().add(guardada.getAlertaId());
        resultado.setAlertasNuevas(resultado.getAlertasNuevas() + 1);

        // Actualizar contadores globales
        resultado.setTotalAlertasGeneradas(resultado.getTotalAlertasGeneradas() + 1);
        resultado.getAlertasPorTipo().merge(tipoAlerta.name(), 1, Integer::sum);
        resultado.getAlertasPorCriticidad().merge(criticidad.name(), 1, Integer::sum);
    }

    private void enrichAlertaWithInventoryData(
            AlertaInventario alerta,
            Integer productoId,
            Integer stockActualOpt,
            Integer stockMinimoOpt,
            Integer puntoReordenOpt) {
        Integer stock = stockActualOpt;
        Integer min = stockMinimoOpt;
        Integer rop = puntoReordenOpt;

        if (stock == null || min == null || rop == null) {
            Optional<Inventario> inventarioOpt = inventarioRepositorio.findByProducto(productoId);
            if (inventarioOpt.isPresent()) {
                Inventario inv = inventarioOpt.get();
                stock = inv.getStockDisponible();
                min = inv.getStockMinimo();
                rop = inv.getPuntoReorden();
            }
        }

        int stockVal = stock != null ? stock : 0;
        int minVal = min != null ? min : 0;
        int ropVal = rop != null ? rop : 0;

        alerta.setStockActual(stockVal);
        alerta.setStockMinimo(minVal);

        if (alerta.getCantidadSugerida() == null) {
            int base = Math.max(ropVal - stockVal, minVal - stockVal);
            if (base < 0)
                base = 0;
            if (alerta.getTipoAlerta() == TipoAlerta.DEMANDA_ANOMALA) {
                base = Math.max(base, ropVal > 0 ? ropVal : minVal);
            }
            alerta.setCantidadSugerida(base);
        }
    }
}
