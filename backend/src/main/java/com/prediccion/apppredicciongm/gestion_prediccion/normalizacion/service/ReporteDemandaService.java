package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.dto.request.NormalizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.errors.NormalizacionException;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository.IRegistroDemandaRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.RegistroDemanda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de normalización de demanda optimizado para procesamiento masivo.
 * Procesa movimientos de kardex y genera registros de demanda agregados por día.
 * 
 * Optimizaciones aplicadas:
 * - Procesamiento por lotes (batch) con transacciones cortas
 * - Liberación periódica del contexto de persistencia (EntityManager.clear())
 * - Flush controlado para evitar acumulación en memoria
 * - Sin transacción única larga que bloquee conexiones
 *
 * @author Sistema de Predicción
 * @version 2.0
 * @since 2025-10-20
 */
@Service
@Slf4j
public class ReporteDemandaService implements IReporteDemandaService {

    private static final int BATCH_SIZE = 50;
    private static final DateTimeFormatter PERIODO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IKardexRepositorio kardexRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IRegistroDemandaRepositorio registroDemandaRepositorio;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    public ReporteDemandaService(
            IKardexRepositorio kardexRepositorio,
            IProductoRepositorio productoRepositorio,
            IRegistroDemandaRepositorio registroDemandaRepositorio,
            PlatformTransactionManager transactionManager) {
        this.kardexRepositorio = kardexRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.registroDemandaRepositorio = registroDemandaRepositorio;
        this.transactionManager = transactionManager;
    }

    /**
     * Cron job automático que se ejecuta todos los días a las 23:00.
     * Normaliza la demanda de los últimos 30 días para todos los productos.
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void normalizarDemandaAutomatico() {
        log.info("[NORMALIZACION] Iniciando normalización automática de demanda...");
        try {
            int registrosProcessados = normalizarDemandaTodos(30);
            log.info("[NORMALIZACION] Normalización automática completada. Registros procesados: {}",
                    registrosProcessados);
        } catch (Exception e) {
            log.error("[NORMALIZACION] Error en normalización automática: {}", e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int normalizarDemandaProducto(Producto producto, int diasProcesar) {
        if (producto == null) {
            throw new NormalizacionException("El producto no puede ser nulo");
        }

        log.debug("Normalizando demanda para producto: {} (ID: {})",
                producto.getNombre(), producto.getProductoId());

        if (diasProcesar < 1) {
            throw new NormalizacionException("diasProcesar debe ser al menos 1");
        }

        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(diasProcesar);
        LocalDateTime fechaFin = LocalDateTime.now();

        try {
            List<Kardex> movimientosVenta = kardexRepositorio
                    .findAllByProductoAndFechaBetween(
                            producto.getProductoId(),
                            fechaInicio,
                            fechaFin,
                            org.springframework.data.domain.PageRequest.of(0, 10000))
                    .stream()
                    .filter(k -> k.getTipoMovimiento() == TipoMovimiento.SALIDA_VENTA && !k.isAnulado())
                    .toList();

            log.debug("Encontrados {} movimientos SALIDA_VENTA para producto {}",
                    movimientosVenta.size(), producto.getNombre());

            Map<LocalDate, Integer> demandaPorFecha = new HashMap<>();
            for (Kardex kardex : movimientosVenta) {
                LocalDate fecha = kardex.getFechaMovimiento().toLocalDate();
                int cantidad = kardex.getMovimientoNeto();
                demandaPorFecha.merge(fecha, Math.abs(cantidad), Integer::sum);
            }

            int registrosCreados = 0;
            for (Map.Entry<LocalDate, Integer> entrada : demandaPorFecha.entrySet()) {
                LocalDate fecha = entrada.getKey();
                int demanda = entrada.getValue();

                Optional<RegistroDemanda> existente = registroDemandaRepositorio
                        .findByProductoAndFecha(producto, fecha);

                RegistroDemanda registro;
                if (existente.isPresent()) {
                    registro = existente.get();
                    registro.setCantidadHistorica(demanda);
                } else {
                    registro = new RegistroDemanda();
                    registro.setProducto(producto);
                    registro.setFechaRegistro(fecha.atStartOfDay());
                    registro.setCantidadHistorica(demanda);
                    registro.setPeriodoRegistro(fecha.format(PERIODO_FORMATTER));
                    registrosCreados++;
                }
                registroDemandaRepositorio.save(registro);
            }

            log.info("[NORMALIZACION] Normalizados {} registros para producto: {} ({} registros nuevos)",
                    demandaPorFecha.size(), producto.getNombre(), registrosCreados);

            return demandaPorFecha.size();

        } catch (Exception e) {
            log.error("Error normalizando demanda para producto {}: {}",
                    producto.getNombre(), e.getMessage(), e);
            throw new NormalizacionException(
                    "Error normalizando demanda para producto: " + producto.getNombre(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Optimizado para procesamiento masivo con transacciones por lote.
     * NO usa @Transactional a nivel de método para evitar conexiones largas.
     */
    @Override
    public int normalizarDemandaTodos(int diasProcesar) {
        log.info("[OPTIMIZADO] Normalizando demanda para TODOS los productos. Días: {}", diasProcesar);

        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(diasProcesar);
        long inicio = System.currentTimeMillis();

        try {
            int registrosInsertados = normalizarDemandaMasivaOptimizada(fechaInicio);

            long fin = System.currentTimeMillis();
            long duracion = fin - inicio;

            log.info("[OPTIMIZADO] Normalización masiva completada en {}ms. Registros procesados: {}",
                    duracion, registrosInsertados);

            return registrosInsertados;

        } catch (Exception e) {
            log.error("[OPTIMIZADO] Error crítico en normalizarDemandaTodos: {}", e.getMessage(), e);
            throw new NormalizacionException("Error en normalización masiva de demanda", e);
        }
    }

    /**
     * Normalización masiva optimizada con transacciones por lote.
     * Cada batch se procesa en su propia transacción corta.
     */
    private int normalizarDemandaMasivaOptimizada(LocalDateTime fechaInicio) {
        log.debug("[NORMALIZACION] Ejecutando normalización masiva optimizada desde: {}", fechaInicio);

        // Obtener datos fuera de transacción (solo lectura)
        List<Object[]> demandaAgrupada = kardexRepositorio.findDemandaAgrupadaPorProductoYFecha(fechaInicio);
        log.info("[NORMALIZACION] Obtenidos {} registros agrupados de kardex", demandaAgrupada.size());

        if (demandaAgrupada.isEmpty()) {
            log.warn("[NORMALIZACION] No hay movimientos de venta en el período especificado");
            return 0;
        }

        // Contadores atómicos para uso en lambdas
        AtomicInteger registrosNuevos = new AtomicInteger(0);
        AtomicInteger registrosActualizados = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        // Dividir en batches
        List<List<Object[]>> batches = particionarEnBatches(demandaAgrupada, BATCH_SIZE);
        int totalBatches = batches.size();

        log.info("[NORMALIZACION] Procesando {} registros en {} batches de máximo {} elementos",
                demandaAgrupada.size(), totalBatches, BATCH_SIZE);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            List<Object[]> batch = batches.get(batchIndex);
            final int currentBatchIndex = batchIndex;

            try {
                txTemplate.execute(status -> {
                    int localNuevos = 0;
                    int localActualizados = 0;

                    for (Object[] fila : batch) {
                        try {
                            Integer productoId = (Integer) fila[0];
                            LocalDate fecha = ((java.sql.Date) fila[1]).toLocalDate();
                            Long cantidad = ((Number) fila[2]).longValue();

                            Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                            if (productoOpt.isEmpty()) {
                                log.warn("[NORMALIZACION] Producto no encontrado: {}", productoId);
                                continue;
                            }

                            Producto producto = productoOpt.get();
                            Optional<RegistroDemanda> existente = registroDemandaRepositorio
                                    .findByProductoAndFecha(producto, fecha);

                            RegistroDemanda registro;
                            if (existente.isPresent()) {
                                registro = existente.get();
                                registro.setCantidadHistorica(cantidad.intValue());
                                localActualizados++;
                            } else {
                                registro = new RegistroDemanda();
                                registro.setProducto(producto);
                                registro.setFechaRegistro(fecha.atStartOfDay());
                                registro.setCantidadHistorica(cantidad.intValue());
                                registro.setPeriodoRegistro(fecha.format(PERIODO_FORMATTER));
                                localNuevos++;
                            }

                            registroDemandaRepositorio.save(registro);
                        } catch (Exception e) {
                            log.error("[NORMALIZACION] Error procesando fila: {}", e.getMessage());
                            errores.incrementAndGet();
                        }
                    }

                    // Flush y clear para liberar memoria y conexión
                    entityManager.flush();
                    entityManager.clear();

                    registrosNuevos.addAndGet(localNuevos);
                    registrosActualizados.addAndGet(localActualizados);

                    return null;
                });

                // Log de progreso cada 10 batches o al final
                if ((currentBatchIndex + 1) % 10 == 0 || currentBatchIndex == totalBatches - 1) {
                    log.info("[NORMALIZACION] Progreso: {}/{} batches ({} nuevos, {} actualizados)",
                            currentBatchIndex + 1, totalBatches,
                            registrosNuevos.get(), registrosActualizados.get());
                }

            } catch (Exception e) {
                log.error("[NORMALIZACION] Error en batch {}: {}", currentBatchIndex, e.getMessage());
                errores.incrementAndGet();
            }
        }

        int totalProcesados = registrosNuevos.get() + registrosActualizados.get();
        log.info("[NORMALIZACION] Normalización masiva completada: {} nuevos, {} actualizados, {} errores",
                registrosNuevos.get(), registrosActualizados.get(), errores.get());

        return totalProcesados;
    }

    /**
     * Divide una lista en sublistas de tamaño máximo especificado.
     */
    private <T> List<List<T>> particionarEnBatches(List<T> lista, int tamañoBatch) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamañoBatch) {
            int fin = Math.min(i + tamañoBatch, lista.size());
            batches.add(new ArrayList<>(lista.subList(i, fin)));
        }
        return batches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int procesarNormalizacionManual(NormalizacionRequest request) {
        log.info("Procesando normalización manual: {}", request);

        try {
            int diasProcesar = request.getDiasProcesar() != null ? request.getDiasProcesar() : 30;

            if (request.getProductoId() != null) {
                Optional<Producto> producto = productoRepositorio.findById(request.getProductoId().intValue());
                if (producto.isEmpty()) {
                    throw new NormalizacionException(
                            "Producto no encontrado con ID: " + request.getProductoId());
                }

                if (request.isRecalcularTodos()) {
                    limpiarDemandaProducto(producto.get());
                    if (request.isNotificaciones()) {
                        log.info("[NORMALIZACION] Registros de demanda limpiados para producto: {}",
                                producto.get().getNombre());
                    }
                }

                return normalizarDemandaProducto(producto.get(), diasProcesar);
            } else {
                if (request.isRecalcularTodos()) {
                    List<Producto> productos = productoRepositorio.findAll();
                    int totalLimpiados = 0;
                    for (Producto p : productos) {
                        totalLimpiados += limpiarDemandaProducto(p);
                    }
                    if (request.isNotificaciones()) {
                        log.info("[NORMALIZACION] Todos los registros de demanda limpiados. Total: {}", totalLimpiados);
                    }
                }

                return normalizarDemandaTodos(diasProcesar);
            }
        } catch (NormalizacionException e) {
            log.error("Error en normalización manual: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long obtenerCantidadDatosHistoricos(Producto producto) {
        if (producto == null) {
            throw new NormalizacionException("El producto no puede ser nulo");
        }

        long cantidad = registroDemandaRepositorio.countByProducto(producto);
        log.debug("Cantidad de registros históricos para {}: {}",
                producto.getNombre(), cantidad);
        return cantidad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hayDatosSuficientes(Producto producto) {
        long cantidad = obtenerCantidadDatosHistoricos(producto);
        boolean suficiente = cantidad >= 12;

        log.debug("Validación de datos para {}: {} registros (suficiente: {})",
                producto.getNombre(), cantidad, suficiente);

        return suficiente;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int limpiarDemandaProducto(Producto producto) {
        if (producto == null) {
            throw new NormalizacionException("El producto no puede ser nulo");
        }

        try {
            long cantidadAntes = registroDemandaRepositorio.countByProducto(producto);
            registroDemandaRepositorio.deleteByProducto(producto);
            log.warn("[NORMALIZACION] Demanda limpiada para {}: {} registros eliminados",
                    producto.getNombre(), cantidadAntes);
            return (int) cantidadAntes;

        } catch (Exception e) {
            log.error("Error limpiando demanda para {}: {}",
                    producto.getNombre(), e.getMessage(), e);
            throw new NormalizacionException("Error limpiando demanda del producto", e);
        }
    }
}
