package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service;

import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository.IAnalisisEstacionalidadRepositorio;
import com.prediccion.apppredicciongm.models.AnalisisEstacionalidad;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para el análisis automático de estacionalidad de productos
 * Analiza patrones estacionales basados en datos históricos del kardex
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalisisEstacionalidadService {

    private final IKardexRepositorio kardexRepositorio;
    private final IAnalisisEstacionalidadRepositorio analisisRepositorio;

    /**
     * Análisis automático de estacionalidad - Se ejecuta cada domingo a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Async
    public void analizarEstacionalidadAutomatico() {
        log.info("[ESTACIONALIDAD] Iniciando análisis automático de estacionalidad...");

        try {
            List<Long> productosConDatos = kardexRepositorio.findDistinctProductIds();
            log.info("[ESTACIONALIDAD] Analizando estacionalidad para {} productos", productosConDatos.size());

            int productosAnalizados = 0;
            int patronesEncontrados = 0;

            for (Long productoId : productosConDatos) {
                AnalisisEstacionalidad analisis = analizarEstacionalidadProducto(productoId);
                if (analisis != null && analisis.getTieneEstacionalidad()) {
                    patronesEncontrados++;
                }
                productosAnalizados++;
            }

            log.info(
                    "[ESTACIONALIDAD] Análisis completado: {}/{} productos analizados, {} patrones estacionales encontrados",
                    productosAnalizados, productosConDatos.size(), patronesEncontrados);

        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error en análisis automático de estacionalidad: {}", e.getMessage(), e);
        }
    }

    /**
     * Analiza y guarda estacionalidad de un producto (alias para compatibilidad)
     */
    public AnalisisEstacionalidad analizarYGuardar(Long productoId) {
        return analizarEstacionalidadProducto(productoId);
    }

    /**
     * Analiza la estacionalidad de un producto específico
     */
    public AnalisisEstacionalidad analizarEstacionalidadProducto(Long productoId) {
        try {
            log.debug("[ESTACIONALIDAD] Analizando estacionalidad para producto ID: {}", productoId);

            // Obtener datos históricos de al menos 12 meses
            LocalDate fechaInicio = LocalDate.now().minusMonths(24);
            List<Kardex> datosHistoricos = kardexRepositorio
                    .findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimiento(
                            productoId, fechaInicio, LocalDate.now());

            if (datosHistoricos.size() < 12) {
                log.debug("[ESTACIONALIDAD] Advertencia: Datos insuficientes para producto {}: {} registros",
                        productoId, datosHistoricos.size());
                return null;
            }

            // Agrupar datos por mes
            Map<Integer, List<BigDecimal>> datosPorMes = agruparDatosPorMes(datosHistoricos);

            // Calcular estadísticas mensuales
            Map<Integer, BigDecimal> promediosMensuales = calcularPromediosMensuales(datosPorMes);
            BigDecimal promedioGeneral = calcularPromedioGeneral(promediosMensuales);

            // Detectar estacionalidad
            AnalisisEstacionalidad analisis = detectarPatronEstacional(
                    productoId, promediosMensuales, promedioGeneral);

            // Guardar o actualizar en base de datos
            return guardarAnalisisEstacionalidad(analisis);

        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error analizando estacionalidad producto {}: {}", productoId, e.getMessage(),
                    e);
            return null;
        }
    }

    /**
     * Agrupa los datos de movimientos por mes
     */
    private Map<Integer, List<BigDecimal>> agruparDatosPorMes(List<Kardex> datosHistoricos) {
        return datosHistoricos.stream()
                .filter(k -> "VENTA".equals(k.getTipoMovimiento().toString()) ||
                        "CONSUMO".equals(k.getTipoMovimiento().toString()) ||
                        k.getTipoMovimiento().esSalida())
                .collect(Collectors.groupingBy(
                        k -> k.getFechaMovimiento().getMonthValue(),
                        Collectors.mapping(k -> BigDecimal.valueOf(k.getCantidad()), Collectors.toList())));
    }

    /**
     * Calcula promedios mensuales
     */
    private Map<Integer, BigDecimal> calcularPromediosMensuales(Map<Integer, List<BigDecimal>> datosPorMes) {
        Map<Integer, BigDecimal> promedios = new HashMap<>();

        for (int mes = 1; mes <= 12; mes++) {
            List<BigDecimal> valoresMes = datosPorMes.getOrDefault(mes, Arrays.asList(BigDecimal.ZERO));

            BigDecimal promedio = valoresMes.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(valoresMes.size()), 2, RoundingMode.HALF_UP);

            promedios.put(mes, promedio);
        }

        return promedios;
    }

    /**
     * Calcula el promedio general anual
     */
    private BigDecimal calcularPromedioGeneral(Map<Integer, BigDecimal> promediosMensuales) {
        BigDecimal suma = promediosMensuales.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    /**
     * Detecta patrones estacionales usando coeficiente de variación
     */
    private AnalisisEstacionalidad detectarPatronEstacional(
            Long productoId,
            Map<Integer, BigDecimal> promediosMensuales,
            BigDecimal promedioGeneral) {

        // Calcular coeficientes estacionales
        Map<Integer, BigDecimal> coeficientesEstacionales = new HashMap<>();
        BigDecimal sumaDesviaciones = BigDecimal.ZERO;

        for (int mes = 1; mes <= 12; mes++) {
            BigDecimal promedioMes = promediosMensuales.get(mes);
            BigDecimal coeficiente = promedioGeneral.compareTo(BigDecimal.ZERO) > 0
                    ? promedioMes.divide(promedioGeneral, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ONE;

            coeficientesEstacionales.put(mes, coeficiente);

            // Calcular desviación del promedio
            BigDecimal desviacion = coeficiente.subtract(BigDecimal.ONE).abs();
            sumaDesviaciones = sumaDesviaciones.add(desviacion);
        }

        // Calcular intensidad de estacionalidad
        BigDecimal intensidadEstacionalidad = sumaDesviaciones.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);

        // Determinar si tiene estacionalidad significativa (threshold > 0.15)
        boolean tieneEstacionalidad = intensidadEstacionalidad.compareTo(new BigDecimal("0.15")) > 0;

        // Identificar mes de mayor y menor demanda
        Integer mesMayorDemanda = coeficientesEstacionales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Integer mesMenorDemanda = coeficientesEstacionales.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return AnalisisEstacionalidad.builder()
                .productoId(productoId)
                .tieneEstacionalidad(tieneEstacionalidad)
                .intensidadEstacionalidad(intensidadEstacionalidad)
                .mesMayorDemanda(mesMayorDemanda)
                .mesMenorDemanda(mesMenorDemanda)
                .coeficienteEnero(coeficientesEstacionales.get(1))
                .coeficienteFebrero(coeficientesEstacionales.get(2))
                .coeficienteMarzo(coeficientesEstacionales.get(3))
                .coeficienteAbril(coeficientesEstacionales.get(4))
                .coeficienteMayo(coeficientesEstacionales.get(5))
                .coeficienteJunio(coeficientesEstacionales.get(6))
                .coeficienteJulio(coeficientesEstacionales.get(7))
                .coeficienteAgosto(coeficientesEstacionales.get(8))
                .coeficienteSeptiembre(coeficientesEstacionales.get(9))
                .coeficienteOctubre(coeficientesEstacionales.get(10))
                .coeficienteNoviembre(coeficientesEstacionales.get(11))
                .coeficienteDiciembre(coeficientesEstacionales.get(12))
                .fechaAnalisis(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .activo(true)
                .build();
    }

    /**
     * Guarda o actualiza el análisis de estacionalidad
     */
    private AnalisisEstacionalidad guardarAnalisisEstacionalidad(AnalisisEstacionalidad analisis) {
        try {
            // Verificar si ya existe un análisis para este producto
            Optional<AnalisisEstacionalidad> existente = analisisRepositorio
                    .findByProductoIdAndActivoTrue(analisis.getProductoId());

            if (existente.isPresent()) {
                // Actualizar análisis existente
                AnalisisEstacionalidad actual = existente.get();
                actual.setTieneEstacionalidad(analisis.getTieneEstacionalidad());
                actual.setIntensidadEstacionalidad(analisis.getIntensidadEstacionalidad());
                actual.setMesMayorDemanda(analisis.getMesMayorDemanda());
                actual.setMesMenorDemanda(analisis.getMesMenorDemanda());

                // Actualizar coeficientes
                actual.setCoeficienteEnero(analisis.getCoeficienteEnero());
                actual.setCoeficienteFebrero(analisis.getCoeficienteFebrero());
                actual.setCoeficienteMarzo(analisis.getCoeficienteMarzo());
                actual.setCoeficienteAbril(analisis.getCoeficienteAbril());
                actual.setCoeficienteMayo(analisis.getCoeficienteMayo());
                actual.setCoeficienteJunio(analisis.getCoeficienteJunio());
                actual.setCoeficienteJulio(analisis.getCoeficienteJulio());
                actual.setCoeficienteAgosto(analisis.getCoeficienteAgosto());
                actual.setCoeficienteSeptiembre(analisis.getCoeficienteSeptiembre());
                actual.setCoeficienteOctubre(analisis.getCoeficienteOctubre());
                actual.setCoeficienteNoviembre(analisis.getCoeficienteNoviembre());
                actual.setCoeficienteDiciembre(analisis.getCoeficienteDiciembre());

                actual.setFechaAnalisis(LocalDate.now());
                actual.setFechaActualizacion(LocalDateTime.now());

                return analisisRepositorio.save(actual);
            } else {
                // Crear nuevo análisis
                return analisisRepositorio.save(analisis);
            }

        } catch (Exception e) {
            log.error("[ESTACIONALIDAD] Error guardando análisis de estacionalidad: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene el análisis de estacionalidad para un producto
     */
    public AnalisisEstacionalidad obtenerEstacionalidad(Long productoId) {
        return analisisRepositorio.findByProductoIdAndActivoTrue(productoId)
                .orElse(null);
    }

    /**
     * Fuerza un nuevo análisis para todos los productos
     */
    public void forzarAnalisisCompleto() {
        log.info("[ESTACIONALIDAD] Forzando análisis completo de estacionalidad...");
        analizarEstacionalidadAutomatico();
    }

    /**
     * Obtiene análisis recientes (últimos 30 días)
     */
    public List<AnalisisEstacionalidad> obtenerAnalisisRecientes() {
        LocalDate fechaLimite = LocalDate.now().minusDays(30);
        return analisisRepositorio.findAnalisisRecientes(fechaLimite);
    }

    /**
     * Obtiene todos los análisis activos
     */
    public List<AnalisisEstacionalidad> obtenerTodosLosAnalisis() {
        return analisisRepositorio.findAllActivos();
    }

    /**
     * Obtiene productos con estacionalidad detectada
     */
    public List<AnalisisEstacionalidad> obtenerProductosConEstacionalidad() {
        return analisisRepositorio.findProductosConEstacionalidad();
    }

    /**
     * Obtiene estadísticas de análisis
     */
    public Map<String, Long> obtenerEstadisticasAnalisis() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalAnalizados", analisisRepositorio.countProductosAnalizados());
        stats.put("conEstacionalidad", analisisRepositorio.countProductosConEstacionalidad());
        stats.put("sinEstacionalidad", stats.get("totalAnalizados") - stats.get("conEstacionalidad"));
        return stats;
    }

    /**
     * Limpia análisis antiguos (más de 6 meses)
     */
    @Scheduled(cron = "0 0 3 1 * ?") // Primer día de cada mes a las 3:00 AM
    public void limpiarAnalisisAntiguos() {
        LocalDate fechaLimite = LocalDate.now().minusMonths(6);
        analisisRepositorio.desactivarAnalisisAntiguos(fechaLimite);
        log.info("[ESTACIONALIDAD] Análisis antiguos anteriores a {} han sido desactivados", fechaLimite);
    }
}