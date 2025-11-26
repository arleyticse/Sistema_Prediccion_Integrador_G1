package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de normalización de demanda.
 * Procesa movimientos de kardex y genera registros de demanda agregados por
 * día.
 * 
 * Responsabilidades:
 * - Extrae SALIDA_VENTA del kardex (señal de demanda de cliente)
 * - Agrega cantidades por día y producto
 * - Evita duplicados de registros
 * - Ejecuta automáticamente cada noche a las 23:00
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReporteDemandaService implements IReporteDemandaService {

    private final IKardexRepositorio kardexRepositorio;

    private final IProductoRepositorio productoRepositorio;

    private final IRegistroDemandaRepositorio registroDemandaRepositorio;

    /**
     * Cron job automático que se ejecuta todos los días a las 23:00.
     * Normaliza la demanda de los últimos 30 días para todos los productos.
     * 
     * Formato Spring Cron: segundos minutos horas día mes día-semana
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
    @Transactional
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
            // Obtener todos los SALIDA_VENTA para el producto en el rango de fechas
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

            // Agrupar por fecha
            Map<LocalDate, Integer> demandaPorFecha = new HashMap<>();
            for (Kardex kardex : movimientosVenta) {
                LocalDate fecha = kardex.getFechaMovimiento().toLocalDate();
                int cantidad = kardex.getMovimientoNeto();

                demandaPorFecha.merge(fecha, Math.abs(cantidad), Integer::sum);
            }

            int registrosCreados = 0;

            // Crear o actualizar registros de demanda
            for (Map.Entry<LocalDate, Integer> entrada : demandaPorFecha.entrySet()) {
                LocalDate fecha = entrada.getKey();
                int demanda = entrada.getValue();

                Optional<RegistroDemanda> existente = registroDemandaRepositorio
                        .findByProductoAndFecha(producto, fecha);

                RegistroDemanda registro;
                if (existente.isPresent()) {
                    registro = existente.get();
                    registro.setCantidadHistorica(demanda);
                    log.debug("Actualizando registro de demanda existente para {} en {}",
                            producto.getNombre(), fecha);
                } else {
                    registro = new RegistroDemanda();
                    registro.setProducto(producto);
                    registro.setFechaRegistro(fecha.atStartOfDay());
                    registro.setCantidadHistorica(demanda);
                    registro.setPeriodoRegistro(fecha.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    registrosCreados++;
                    log.debug("Creando nuevo registro de demanda para {} en {}",
                            producto.getNombre(), fecha);
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
     * Optimizado para procesamiento masivo usando consulta SQL nativa
     */
    @Override
    @Transactional
    public int normalizarDemandaTodos(int diasProcesar) {
        log.info("[OPTIMIZADO] Normalizando demanda para TODOS los productos. Días: {}", diasProcesar);

        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(diasProcesar);
        long inicio = System.currentTimeMillis();

        try {
            // Ejecutar normalización optimizada por lotes usando SQL nativo
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
     * Normalización masiva optimizada usando SQL nativo.
     * Procesa todos los productos en una sola operación batch.
     */
    @Transactional
    private int normalizarDemandaMasivaOptimizada(LocalDateTime fechaInicio) {
        log.debug("[NORMALIZACION] Ejecutando normalización masiva optimizada desde: {}", fechaInicio);

        try {
            // Obtener todos los movimientos de venta agrupados por producto y fecha
            List<Object[]> demandaAgrupada = kardexRepositorio.findDemandaAgrupadaPorProductoYFecha(fechaInicio);

            log.info("[NORMALIZACION] Obtenidos {} registros agrupados de kardex", demandaAgrupada.size());

            int registrosNuevos = 0;
            int registrosActualizados = 0;
            int batchSize = 100;
            int contador = 0;

            for (Object[] fila : demandaAgrupada) {
                Integer productoId = (Integer) fila[0];
                LocalDate fecha = ((java.sql.Date) fila[1]).toLocalDate();
                Long cantidad = ((Number) fila[2]).longValue();

                // Buscar producto
                Optional<Producto> productoOpt = productoRepositorio.findById(productoId);
                if (productoOpt.isEmpty()) {
                    log.warn("[NORMALIZACION] Advertencia: Producto no encontrado: {}", productoId);
                    continue;
                }

                Producto producto = productoOpt.get();

                // Buscar registro existente
                Optional<RegistroDemanda> existente = registroDemandaRepositorio
                        .findByProductoAndFecha(producto, fecha);

                RegistroDemanda registro;
                if (existente.isPresent()) {
                    registro = existente.get();
                    registro.setCantidadHistorica(cantidad.intValue());
                    registrosActualizados++;
                } else {
                    registro = new RegistroDemanda();
                    registro.setProducto(producto);
                    registro.setFechaRegistro(fecha.atStartOfDay());
                    registro.setCantidadHistorica(cantidad.intValue());
                    registro.setPeriodoRegistro(fecha.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    registrosNuevos++;
                }

                registroDemandaRepositorio.save(registro);

                contador++;

                // Flush cada batch para liberar memoria
                if (contador % batchSize == 0) {
                    registroDemandaRepositorio.flush();
                    log.debug("💾 Batch procesado: {} registros", contador);
                }
            }

            // Flush final
            registroDemandaRepositorio.flush();

            log.info("[NORMALIZACION] Normalización masiva completada: {} nuevos, {} actualizados",
                    registrosNuevos, registrosActualizados);

            return registrosNuevos + registrosActualizados;

        } catch (Exception e) {
            log.error("[NORMALIZACION] Error en normalización masiva optimizada: {}", e.getMessage(), e);
            throw new NormalizacionException("Error en normalización masiva", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int procesarNormalizacionManual(NormalizacionRequest request) {
        log.info("Procesando normalización manual: {}", request);

        try {
            int diasProcesar = request.getDiasProcesar() != null ? request.getDiasProcesar() : 30;

            if (request.getProductoId() != null) {
                // Normalizar producto específico
                Optional<Producto> producto = productoRepositorio.findById(request.getProductoId().intValue());
                if (producto.isEmpty()) {
                    throw new NormalizacionException(
                            "Producto no encontrado con ID: " + request.getProductoId());
                }

                if (request.isRecalcularTodos()) {
                    // Limpiar y recalcular
                    limpiarDemandaProducto(producto.get());
                    if (request.isNotificaciones()) {
                        log.info("[NORMALIZACION] Registros de demanda limpiados para producto: {}",
                                producto.get().getNombre());
                    }
                }

                return normalizarDemandaProducto(producto.get(), diasProcesar);
            } else {
                // Normalizar todos
                if (request.isRecalcularTodos()) {
                    // Limpiar todos y recalcular
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
    @Transactional
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
