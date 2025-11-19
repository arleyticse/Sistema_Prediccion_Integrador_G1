package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import com.prediccion.apppredicciongm.models.RegistroDemanda;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository.IRegistroDemandaRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.DatosInsuficientesException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.ProductoNoEncontradoException;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.errors.PrediccionNoEncontradaException;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de predicción ARIMA (AutoRegressive Integrated Moving Average).
 * Implementa algoritmo de predicción de demanda usando Apache Commons Math.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PrediccionService implements IPrediccionService {

    // Inyecciones de dependencias
    private final IPrediccionRepositorio prediccionRepositorio;
    private final IRegistroDemandaRepositorio registroDemandaRepositorio;
    private final IProductoRepositorio productoRepositorio;
    
    // Constantes para ARIMA
    private static final int MINIMO_REGISTROS_HISTORICOS = 12;
    private static final double FACTOR_TENDENCIA = 0.05;
    private static final double FACTOR_ESTACIONAL = 1.1;
    private static final double FACTOR_SUAVIZADO = 0.3;
    
    // Constante para limpieza automática de historial (mantener últimas N predicciones)
    private static final int MAX_PREDICCIONES_POR_CONFIGURACION = 5;

    /**
     * Genera una predicción ARIMA mejorada para un producto integrando estacionalidad.
     *
     * @param productoId ID del producto
     * @param diasProcesar número de días a procesar
     * @return la predicción generada
     * @throws ProductoNoEncontradoException si el producto no existe
     * @throws DatosInsuficientesException si no hay suficientes datos históricos
     */
    @Override
    public Prediccion generarPrediccion(Integer productoId, int diasProcesar) {
        log.info("Iniciando generacion de prediccion mejorada con estacionalidad para producto ID: {}", productoId);

        // 1. Validar que el producto existe
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado: {}", productoId);
                    return new ProductoNoEncontradoException("Producto con ID " + productoId + " no encontrado");
                });

        // 2. Obtener datos históricos
        List<RegistroDemanda> historial = registroDemandaRepositorio.findByProducto(producto);
        log.debug("Registros historicos encontrados: {}", historial.size());

        // 3. Validar cantidad de datos históricos
        if (historial.size() < MINIMO_REGISTROS_HISTORICOS) {
            log.warn("Datos insuficientes para prediccion. Minimo: {}, Encontrados: {}", 
                    MINIMO_REGISTROS_HISTORICOS, historial.size());
            throw new DatosInsuficientesException(
                    "Se requieren minimo " + MINIMO_REGISTROS_HISTORICOS + " registros historicos. Encontrados: " + historial.size()
            );
        }

        // 4. Aplicar algoritmo ARIMA básico (estacionalidad manejada por AnalisisEstacionalidad)
        Integer demandaPredicha = calcularPrediccionARIMA(historial, diasProcesar);
        BigDecimal metricasError = calcularPrecision(historial, demandaPredicha);

        // 5. Buscar si existe predicción reciente para reutilizar o actualizar
        Optional<Prediccion> prediccionExistente = prediccionRepositorio
            .findByProductoAndAlgoritmoUsadoAndHorizonteTiempo(producto, "ARIMA", diasProcesar);
        
        Prediccion prediccion;
        if (prediccionExistente.isPresent()) {
            // Actualizar predicción existente
            prediccion = prediccionExistente.get();
            log.info("[PREDICCION] Actualizando predicción existente ID: {}", prediccion.getPrediccionId());
        } else {
            // Crear nueva predicción
            prediccion = new Prediccion();
            prediccion.setProducto(producto);
            prediccion.setAlgoritmoUsado("ARIMA");
            prediccion.setHorizonteTiempo(diasProcesar);
            log.info("[PREDICCION] Creando nueva predicción");
        }
        
        // Actualizar valores calculados
        prediccion.setDemandaPredichaTotal(demandaPredicha);
        prediccion.setMetricasError(metricasError);
        prediccion.setFechaEjecucion(LocalDateTime.now());

        Prediccion prediccionGuardada = prediccionRepositorio.save(prediccion);
        
        // 6. Limpieza automática: mantener solo las últimas N predicciones
        limpiarPrediccionesAntiguas(producto, "ARIMA", diasProcesar);
        
        log.info("[PREDICCION] Prediccion procesada exitosamente: ID {}, Demanda: {}, Precision: {}", 
                prediccionGuardada.getPrediccionId(), demandaPredicha, metricasError);

        return prediccionGuardada;
    }

    /**
     * Obtiene la última predicción de un producto.
     *
     * @param productoId ID del producto
     * @return la última predicción
     * @throws ProductoNoEncontradoException si el producto no existe
     * @throws PrediccionNoEncontradaException si no hay predicciones
     */
    @Override
    public Prediccion obtenerUltimaPrediccion(Integer productoId) {
        log.info("Buscando última predicción para producto ID: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        Optional<Prediccion> prediccion = prediccionRepositorio.findFirstByProductoOrderByFechaEjecucionDesc(producto);
        return prediccion.orElseThrow(() -> {
            log.warn("No hay predicciones para el producto: {}", productoId);
            return new PrediccionNoEncontradaException("No hay predicciones disponibles para este producto");
        });
    }

    /**
     * Obtiene predicciones paginadas para un producto.
     *
     * @param productoId ID del producto
     * @param pageable información de paginación
     * @return lista de predicciones
     */
    @Override
    public List<Prediccion> obtenerPrediccionesByProducto(Integer productoId, Pageable pageable) {
        log.info("Obteniendo predicciones paginadas para producto ID: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado"));

        List<Prediccion> predicciones = prediccionRepositorio.findByProductoOrderByFechaEjecucionDesc(producto);
        
        // Aplicar paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), predicciones.size());
        
        if (start >= predicciones.size()) {
            return List.of();
        }
        
        return predicciones.subList(start, end);
    }

    /**
     * Actualiza la precisión de una predicción.
     *
     * @param prediccionId ID de la predicción
     * @param nuevaPrecision nueva precisión
     * @throws PrediccionNoEncontradaException si la predicción no existe
     */
    @Override
    public void actualizarPrecision(Long prediccionId, double nuevaPrecision) {
        log.info("Actualizando precisión de predicción ID: {} a {}", prediccionId, nuevaPrecision);

        Prediccion prediccion = prediccionRepositorio.findById(prediccionId.intValue())
                .orElseThrow(() -> new PrediccionNoEncontradaException("Predicción no encontrada"));

        prediccion.setMetricasError(BigDecimal.valueOf(nuevaPrecision));
        prediccionRepositorio.save(prediccion);
        log.info("Precisión actualizada");
    }

    /**
     * Elimina una predicción.
     *
     * @param prediccionId ID de la predicción
     */
    @Override
    public void eliminarPrediccion(Long prediccionId) {
        log.info("Eliminando predicción ID: {}", prediccionId);

        if (!prediccionRepositorio.existsById(prediccionId.intValue())) {
            log.warn("Predicción no encontrada para eliminar: {}", prediccionId);
            throw new PrediccionNoEncontradaException("Predicción no encontrada");
        }

        prediccionRepositorio.deleteById(prediccionId.intValue());
        log.info("Predicción eliminada");
    }

    /**
     * Obtiene todas las predicciones con paginación.
     *
     * @param pageable información de paginación
     * @return página de predicciones
     */
    @Override
    public Page<Prediccion> obtenerTodasLasPredicciones(Pageable pageable) {
        log.info("Obteniendo todas las predicciones con paginación");
        
        try {
            Page<Prediccion> predicciones = prediccionRepositorio.findAll(pageable);

            log.info("Se encontraron {} predicciones de {} total",
                    predicciones.getNumberOfElements(), predicciones.getTotalElements());
            
            return predicciones;
            
        } catch (Exception e) {
            log.error("Error al obtener predicciones: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener las predicciones", e);
        }
    }

    /**
     * Calcula la predicción ARIMA usando Apache Commons Math.
     * NOTA: La estacionalidad ahora se maneja en AnalisisEstacionalidad con coeficientes mensuales.
     * Este servicio usa ARIMA básico, la integración completa se hace en SmartPrediccionService.
     *
     * @param historial datos históricos de demanda
     * @param diasProcesar días a predecir
     * @return demanda predicha
     */
    private Integer calcularPrediccionARIMA(List<RegistroDemanda> historial, int diasProcesar) {
        log.debug("Calculando predicción ARIMA...");

        // 1. Extraer valores de demanda histórica
        double[] demandas = historial.stream()
                .mapToDouble(r -> r.getCantidadHistorica() != null ? r.getCantidadHistorica().doubleValue() : 0.0)
                .toArray();

        // 2. Crear estadísticas con Apache Commons Math
        DescriptiveStatistics stats = new DescriptiveStatistics(demandas);
        double media = stats.getMean();

        log.debug("Media: {}", media);

        // 3. Componentes ARIMA
        double baseline = media;
        double tendencia = calcularTendencia(demandas);
        double estacionalidad = calcularEstacionalidad(demandas, 7);
        double suavizado = baseline * (1 + FACTOR_SUAVIZADO);

        // 4. Combinar componentes ARIMA
        double prediccion = (baseline * 0.40) +
                           (tendencia * 0.30) +
                           (estacionalidad * 0.20) +
                           (suavizado * 0.10);

        // 5. Aplicar factor de seguridad
        double prediccionFinal = prediccion * 1.2;

        log.debug("Componentes ARIMA - Base: {}, Tendencia: {}, Estacional: {}, Final: {}", 
                baseline, tendencia, estacionalidad, prediccionFinal);

        return Math.round((float) prediccionFinal);
    }

    /**
     * Calcula la componente de tendencia.
     *
     * @param demandas array de valores históricos
     * @return factor de tendencia
     */
    private double calcularTendencia(double[] demandas) {
        if (demandas.length < 2) return 0;

        int mitad = demandas.length / 2;
        double promedioPrimera = 0, promedioSegunda = 0;

        for (int i = 0; i < mitad; i++) {
            promedioPrimera += demandas[i];
        }
        promedioPrimera /= mitad;

        for (int i = mitad; i < demandas.length; i++) {
            promedioSegunda += demandas[i];
        }
        promedioSegunda /= (demandas.length - mitad);

        double tendencia = promedioSegunda / promedioPrimera;
        log.debug("📈 Tendencia calculada: {}", tendencia);

        return promedioPrimera > 0 ? (tendencia > 1 ? promedioSegunda * (1 + FACTOR_TENDENCIA) : promedioSegunda * 0.95) : promedioSegunda;
    }

    /**
     * Calcula la componente estacional.
     *
     * @param demandas array de valores históricos
     * @param periodo período de estacionalidad
     * @return factor estacional
     */
    private double calcularEstacionalidad(double[] demandas, int periodo) {
        if (demandas.length < periodo) return 1.0;

        double sumaUltimoPeriodo = 0;
        for (int i = Math.max(0, demandas.length - periodo); i < demandas.length; i++) {
            sumaUltimoPeriodo += demandas[i];
        }

        double mediaUltimoPeriodo = sumaUltimoPeriodo / periodo;

        DescriptiveStatistics stats = new DescriptiveStatistics(demandas);
        double mediaTotal = stats.getMean();

        double estacionalidad = mediaTotal > 0 ? mediaUltimoPeriodo / mediaTotal : 1.0;
        log.debug("Estacionalidad calculada: {}", estacionalidad);

        return estacionalidad * FACTOR_ESTACIONAL;
    }

    /**
     * Calcula la precisión usando MAPE.
     *
     * @param historial datos históricos
     * @param prediccion valor predicho
     * @return precisión
     */
    private BigDecimal calcularPrecision(List<RegistroDemanda> historial, Integer prediccion) {
        if (historial.isEmpty()) return BigDecimal.ZERO;

        double mediaHistorica = historial.stream()
                .mapToDouble(r -> r.getCantidadHistorica() != null ? r.getCantidadHistorica().doubleValue() : 0.0)
                .average()
                .orElse(0);

        double error = Math.abs(prediccion - mediaHistorica);
        double mape = mediaHistorica > 0 ? (error / mediaHistorica) * 100 : 0;
        double precision = Math.max(0, 100 - Math.min(100, mape));

        log.debug("Precisión MAPE: {}%", precision);
        return BigDecimal.valueOf(Math.round(precision * 100.0) / 100.0);
    }

    /**
     * Limpia predicciones antiguas manteniendo solo las últimas N por configuración.
     * Estrategia intermedia: conserva historial reciente pero evita acumulación infinita.
     * 
     * @param producto el producto
     * @param algoritmo algoritmo usado
     * @param horizonte horizonte de tiempo
     */
    private void limpiarPrediccionesAntiguas(Producto producto, String algoritmo, Integer horizonte) {
        try {
            // Obtener todas las predicciones de esta configuración ordenadas por fecha DESC
            List<Prediccion> todasLasPredicciones = prediccionRepositorio
                .findPrediccionesAntiguasParaLimpieza(producto, algoritmo, horizonte);
            
            // Si hay más de MAX_PREDICCIONES_POR_CONFIGURACION, eliminar las antiguas
            if (todasLasPredicciones.size() > MAX_PREDICCIONES_POR_CONFIGURACION) {
                // Mantener las primeras N (más recientes), eliminar el resto
                List<Prediccion> prediccionesAEliminar = todasLasPredicciones
                    .subList(MAX_PREDICCIONES_POR_CONFIGURACION, todasLasPredicciones.size());
                
                log.info("[PREDICCION] Limpiando {} predicciones antiguas del producto {} (manteniendo últimas {})",
                        prediccionesAEliminar.size(), producto.getProductoId(), MAX_PREDICCIONES_POR_CONFIGURACION);
                
                prediccionRepositorio.deleteAll(prediccionesAEliminar);
            }
        } catch (Exception e) {
            // No fallar el guardado de predicción si falla la limpieza
            log.warn("[PREDICCION] Advertencia: Error en limpieza de predicciones antiguas: {}", e.getMessage());
        }
    }
}
