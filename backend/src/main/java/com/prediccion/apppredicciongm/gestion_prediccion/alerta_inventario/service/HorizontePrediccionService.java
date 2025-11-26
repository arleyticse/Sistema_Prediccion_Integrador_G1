package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.math.MathEx;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio para calcular el horizonte de prediccion optimo de forma
 * inteligente.
 * 
 * Analiza multiples factores para recomendar el periodo de prediccion ideal:
 * - Rotacion del producto (dias promedio entre ventas)
 * - Lead time del proveedor
 * - Historial de demanda
 * - Categoria del producto
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HorizontePrediccionService {

    private final IKardexRepositorio kardexRepositorio;

    // Constantes de configuracion
    private static final int HORIZONTE_MINIMO_DIAS = 30; // 1 mes
    private static final int HORIZONTE_MAXIMO_DIAS = 365; // 12 meses
    private static final int HORIZONTE_DEFAULT_DIAS = 365; // 12 meses por defecto

    // Constantes para autocorrelacion
    private static final int MAX_LAG_AUTOCORR = 30;
    private static final double UMBRAL_CORRELACION = 0.5;
    private static final int HORIZONTE_DEFAULT_PERIODOS = 5;
    private static final int MIN_DATOS_AUTOCORR = 14;

    // Umbrales de rotacion (dias)
    private static final int ROTACION_RAPIDA = 15; // Vende cada 15 dias o menos
    private static final int ROTACION_MEDIA = 45; // Vende cada 45 dias
    private static final int ROTACION_LENTA = 90; // Vende cada 90 dias o mas

    /**
     * Calcula el horizonte de prediccion optimo para una alerta.
     * 
     * @param alerta Alerta de inventario
     * @return Horizonte en dias (entre 30 y 365)
     */
    public int calcularHorizonteOptimo(AlertaInventario alerta) {
        try {
            Producto producto = alerta.getProducto();
            if (producto == null) {
                log.warn("Alerta {} sin producto, usando horizonte default", alerta.getAlertaId());
                return HORIZONTE_DEFAULT_DIAS;
            }

            log.debug("Calculando horizonte para producto ID: {}", producto.getProductoId());

            // 1. Calcular rotacion del producto
            Integer diasRotacion = calcularDiasRotacion(producto.getProductoId());

            // 2. Obtener lead time del proveedor
            Integer leadTime = producto.getDiasLeadTime();
            if (leadTime == null || leadTime <= 0) {
                leadTime = 7; // Default: 1 semana
            }

            // 3. Calcular horizonte basado en rotacion
            int horizonteCalculado = calcularHorizontePorRotacion(diasRotacion, leadTime);

            log.debug("Producto ID {}: Rotacion {} dias, Lead time {} dias, Horizonte {} dias",
                    producto.getProductoId(), diasRotacion, leadTime, horizonteCalculado);

            return horizonteCalculado;

        } catch (Exception e) {
            log.error("Error calculando horizonte para alerta {}: {}",
                    alerta.getAlertaId(), e.getMessage(), e);
            return HORIZONTE_DEFAULT_DIAS;
        }
    }

    /**
     * Calcula cuantos dias en promedio tarda en venderse el producto.
     * 
     * @param productoId ID del producto
     * @return Dias promedio entre ventas (null si no hay datos)
     */
    private Integer calcularDiasRotacion(Integer productoId) {
        try {
            // Obtener ultimos 6 meses de movimientos de salida
            LocalDate fechaInicio = LocalDate.now().minusMonths(6);
            List<Kardex> movimientos = kardexRepositorio
                    .findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimiento(
                            productoId.longValue(), fechaInicio, LocalDate.now());

            if (movimientos.isEmpty()) {
                log.debug("Sin movimientos para producto {}, asumiendo rotacion media", productoId);
                return ROTACION_MEDIA;
            }

            // Filtrar solo salidas (ventas)
            List<Kardex> salidas = movimientos.stream()
                    .filter(k -> k.getTipoMovimiento() != null &&
                            k.getTipoMovimiento().esSalida())
                    .toList();

            if (salidas.size() < 2) {
                log.debug("Insuficientes salidas para producto {}, asumiendo rotacion media", productoId);
                return ROTACION_MEDIA;
            }

            // Calcular dias promedio entre ventas
            long diasTotales = ChronoUnit.DAYS.between(
                    salidas.get(0).getFechaMovimiento().toLocalDate(),
                    salidas.get(salidas.size() - 1).getFechaMovimiento().toLocalDate());

            int diasPromedio = (int) (diasTotales / (salidas.size() - 1));

            // Limitar valores extremos
            if (diasPromedio < 1)
                diasPromedio = 1;
            if (diasPromedio > 180)
                diasPromedio = 180;

            return diasPromedio;

        } catch (Exception e) {
            log.warn("Error calculando rotacion para producto {}: {}",
                    productoId, e.getMessage());
            return ROTACION_MEDIA;
        }
    }

    /**
     * Determina el horizonte optimo basado en rotacion y lead time.
     * 
     * Logica:
     * - Rotacion rapida (≤15 dias): 3 meses
     * - Rotacion media (16-45 dias): 6 meses
     * - Rotacion lenta (46-90 dias): 9 meses
     * - Rotacion muy lenta (>90 dias): 12 meses
     * 
     * Se ajusta considerando el lead time del proveedor.
     * 
     * @param diasRotacion Dias promedio de rotacion
     * @param leadTime     Lead time del proveedor
     * @return Horizonte en dias
     */
    private int calcularHorizontePorRotacion(Integer diasRotacion, Integer leadTime) {
        int horizonteBase;

        if (diasRotacion == null) {
            horizonteBase = 180; // 6 meses default
        } else if (diasRotacion <= ROTACION_RAPIDA) {
            // Rotacion rapida: 3 meses
            horizonteBase = 90;
            log.debug("Rotacion RAPIDA (≤15 dias) → Horizonte 3 meses");
        } else if (diasRotacion <= ROTACION_MEDIA) {
            // Rotacion media: 6 meses
            horizonteBase = 180;
            log.debug("Rotacion MEDIA (16-45 dias) → Horizonte 6 meses");
        } else if (diasRotacion <= ROTACION_LENTA) {
            // Rotacion lenta: 9 meses
            horizonteBase = 270;
            log.debug("Rotacion LENTA (46-90 dias) → Horizonte 9 meses");
        } else {
            // Rotacion muy lenta: 12 meses
            horizonteBase = 365;
            log.debug("Rotacion MUY LENTA (>90 dias) → Horizonte 12 meses");
        }

        // Ajustar por lead time (minimo 3x el lead time)
        int minimoRequerido = leadTime * 3;
        if (horizonteBase < minimoRequerido) {
            horizonteBase = minimoRequerido;
            log.debug("Ajustado por lead time: {} dias (3x {} dias)", horizonteBase, leadTime);
        }

        // Aplicar limites
        if (horizonteBase < HORIZONTE_MINIMO_DIAS) {
            return HORIZONTE_MINIMO_DIAS;
        }
        if (horizonteBase > HORIZONTE_MAXIMO_DIAS) {
            return HORIZONTE_MAXIMO_DIAS;
        }

        return horizonteBase;
    }

    /**
     * Obtiene el horizonte default del sistema.
     * 
     * @return Horizonte default en dias (365)
     */
    public int getHorizonteDefault() {
        return HORIZONTE_DEFAULT_DIAS;
    }

    /**
     * Calcula horizontes para multiples alertas de forma eficiente.
     * 
     * @param alertas Lista de alertas
     * @return Horizonte promedio recomendado
     */
    public int calcularHorizontePromedio(List<AlertaInventario> alertas) {
        if (alertas.isEmpty()) {
            return HORIZONTE_DEFAULT_DIAS;
        }

        int sumaHorizontes = alertas.stream()
                .mapToInt(this::calcularHorizonteOptimo)
                .sum();

        return sumaHorizontes / alertas.size();
    }

    // ============================================
    // MÉTODOS DE AUTOCORRELACIÓN (ANÁLISIS ML)
    // ============================================

    /**
     * Calcula el horizonte óptimo usando análisis de autocorrelación con SMILE ML.
     * 
     * Este método detecta patrones estacionales en la serie temporal analizando
     * la autocorrelación para diferentes lags (1-30 períodos).
     * 
     * El algoritmo:
     * 1. Calcula la autocorrelación para lags de 1 a 30
     * 2. Identifica el lag con mayor correlación positiva (> 0.5)
     * 3. Retorna ese lag como horizonte sugerido
     * 4. Si no hay correlaciones significativas, retorna valor por defecto (5)
     * 
     * @param serieHistorica array de valores históricos de demanda
     * @return horizonte sugerido (número de períodos hacia adelante)
     */
    public int calcularHorizonteConAutocorrelacion(double[] serieHistorica) {
        log.debug("Iniciando cálculo de horizonte con autocorrelación para serie de {} elementos",
                serieHistorica != null ? serieHistorica.length : 0);

        // Validaciones básicas
        if (serieHistorica == null || serieHistorica.length < MIN_DATOS_AUTOCORR) {
            log.warn("Serie histórica insuficiente (mínimo {} datos requeridos). Usando horizonte por defecto: {}",
                    MIN_DATOS_AUTOCORR, HORIZONTE_DEFAULT_PERIODOS);
            return HORIZONTE_DEFAULT_PERIODOS;
        }

        // Verificar que la serie tenga variabilidad
        if (!tieneVariabilidad(serieHistorica)) {
            log.warn("Serie histórica sin variabilidad (todos valores iguales). Usando horizonte por defecto: {}",
                    HORIZONTE_DEFAULT_PERIODOS);
            return HORIZONTE_DEFAULT_PERIODOS;
        }

        try {
            // Calcular autocorrelaciones para diferentes lags
            List<CorrelacionLag> correlaciones = calcularAutocorrelaciones(serieHistorica);

            // Encontrar el lag con mayor correlación significativa
            int horizonteSugerido = encontrarMejorHorizonte(correlaciones);

            log.info("Horizonte óptimo calculado con autocorrelación: {} períodos", horizonteSugerido);

            return horizonteSugerido;

        } catch (Exception e) {
            log.error("Error al calcular horizonte con autocorrelación: {}. Usando horizonte por defecto: {}",
                    e.getMessage(), HORIZONTE_DEFAULT_PERIODOS);
            return HORIZONTE_DEFAULT_PERIODOS;
        }
    }

    /**
     * Calcula autocorrelaciones para múltiples lags usando SMILE ML.
     * 
     * @param serie array de valores históricos
     * @return lista de correlaciones con sus respectivos lags
     */
    private List<CorrelacionLag> calcularAutocorrelaciones(double[] serie) {
        List<CorrelacionLag> correlaciones = new ArrayList<>();

        int maxLagPermitido = Math.min(MAX_LAG_AUTOCORR, serie.length / 2);

        for (int lag = 1; lag <= maxLagPermitido; lag++) {
            double correlacion = calcularAutocorrelacion(serie, lag);
            correlaciones.add(new CorrelacionLag(lag, correlacion));

            log.debug("Lag {}: Autocorrelación = {}", lag, String.format("%.4f", correlacion));
        }

        return correlaciones;
    }

    /**
     * Calcula la autocorrelación para un lag específico usando SMILE ML.
     * 
     * @param serie array de valores
     * @param lag   número de períodos de retraso
     * @return coeficiente de autocorrelación (-1 a 1)
     */
    private double calcularAutocorrelacion(double[] serie, int lag) {
        if (lag >= serie.length || lag < 1) {
            return 0.0;
        }

        try {
            // Calcular autocorrelación manualmente (Pearson correlation con lag)
            return calcularCorrelacionConLag(serie, lag);

        } catch (Exception e) {
            log.warn("Error calculando autocorrelación para lag {}: {}", lag, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calcula la correlación entre una serie y su versión desplazada.
     * 
     * @param serie array de valores
     * @param lag   desplazamiento
     * @return coeficiente de correlación
     */
    private double calcularCorrelacionConLag(double[] serie, int lag) {
        int n = serie.length - lag;

        // Calcular medias
        double mediaX = 0.0;
        double mediaY = 0.0;
        for (int i = 0; i < n; i++) {
            mediaX += serie[i];
            mediaY += serie[i + lag];
        }
        mediaX /= n;
        mediaY /= n;

        // Calcular correlación
        double numerador = 0.0;
        double denomX = 0.0;
        double denomY = 0.0;

        for (int i = 0; i < n; i++) {
            double diffX = serie[i] - mediaX;
            double diffY = serie[i + lag] - mediaY;
            numerador += diffX * diffY;
            denomX += diffX * diffX;
            denomY += diffY * diffY;
        }

        double denominador = Math.sqrt(denomX * denomY);
        return denominador > 0.0 ? numerador / denominador : 0.0;
    }

    /**
     * Encuentra el mejor horizonte basándose en las correlaciones calculadas.
     * 
     * Busca el lag con:
     * 1. Correlación mayor al umbral (0.5)
     * 2. El valor más alto entre los que cumplen el criterio
     * 3. Preferencia por lags menores en caso de empate
     * 
     * @param correlaciones lista de correlaciones por lag
     * @return horizonte sugerido
     */
    private int encontrarMejorHorizonte(List<CorrelacionLag> correlaciones) {
        double mejorCorrelacion = 0.0;
        int mejorLag = HORIZONTE_DEFAULT_PERIODOS;

        for (CorrelacionLag cl : correlaciones) {
            // Buscar correlaciones significativas (positivas y mayores al umbral)
            if (cl.correlacion > UMBRAL_CORRELACION && cl.correlacion > mejorCorrelacion) {
                mejorCorrelacion = cl.correlacion;
                mejorLag = cl.lag;
            }
        }

        // Si encontramos un lag con buena correlación, usarlo
        if (mejorCorrelacion > UMBRAL_CORRELACION) {
            log.info("Patrón detectado: Lag {} con correlación {}",
                    mejorLag, String.format("%.4f", mejorCorrelacion));
            return mejorLag;
        }

        // Si no hay correlaciones significativas, buscar el primer pico menor
        return buscarPrimerPico(correlaciones);
    }

    /**
     * Busca el primer pico significativo en las correlaciones.
     * 
     * Un pico es un punto donde la correlación es mayor que sus vecinos.
     * Útil cuando no hay correlaciones por encima del umbral principal.
     * 
     * @param correlaciones lista de correlaciones
     * @return lag del primer pico o valor por defecto
     */
    private int buscarPrimerPico(List<CorrelacionLag> correlaciones) {
        if (correlaciones.size() < 3) {
            return HORIZONTE_DEFAULT_PERIODOS;
        }

        for (int i = 1; i < correlaciones.size() - 1; i++) {
            CorrelacionLag anterior = correlaciones.get(i - 1);
            CorrelacionLag actual = correlaciones.get(i);
            CorrelacionLag siguiente = correlaciones.get(i + 1);

            // Verificar si es un pico local
            if (actual.correlacion > anterior.correlacion &&
                    actual.correlacion > siguiente.correlacion &&
                    actual.correlacion > 0.3) { // Umbral mínimo para pico

                log.info("Pico detectado en lag {} con correlación {}",
                        actual.lag, String.format("%.4f", actual.correlacion));
                return actual.lag;
            }
        }

        log.info("No se detectaron patrones claros. Usando horizonte por defecto: {}",
                HORIZONTE_DEFAULT_PERIODOS);
        return HORIZONTE_DEFAULT_PERIODOS;
    }

    /**
     * Verifica si la serie tiene variabilidad suficiente para análisis.
     * 
     * @param serie array de valores
     * @return true si hay variabilidad, false si todos los valores son iguales
     */
    private boolean tieneVariabilidad(double[] serie) {
        if (serie.length == 0) {
            return false;
        }

        double primerValor = serie[0];
        for (double valor : serie) {
            if (Math.abs(valor - primerValor) > 0.001) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clase interna para almacenar correlación y su lag correspondiente.
     */
    private static class CorrelacionLag {
        final int lag;
        final double correlacion;

        CorrelacionLag(int lag, double correlacion) {
            this.lag = lag;
            this.correlacion = correlacion;
        }
    }
}
