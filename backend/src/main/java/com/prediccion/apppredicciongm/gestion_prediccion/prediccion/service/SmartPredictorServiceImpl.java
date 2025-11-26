package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service.HorizontePrediccionService;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository.IAnalisisEstacionalidadRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository.IRegistroDemandaRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.repository.IParametroAlgoritmoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.SmartPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.AnalisisEstacionalidad;
import com.prediccion.apppredicciongm.models.ParametroAlgoritmo;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.RegistroDemanda;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

// Imports de Smile ML v3.1.1
import smile.data.DataFrame;
import smile.data.vector.DoubleVector;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;
import smile.regression.RandomForest;
import smile.regression.GradientTreeBoost;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.enums.AlgoritmoSmileML;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de predicción inteligente con Smile ML v3.1.1
 *
 * Esta versión está completamente integrada con los nuevos DTOs y la interfaz
 * ISmartPredictorService.
 * Implementa RF006 (análisis predictivos múltiples) y RF007 (visualizaciones
 * gráficas).
 * 
 * Los parámetros de cada algoritmo se obtienen dinámicamente desde la tabla
 * parametro_algoritmo, permitiendo ajuste fino sin recompilar.
 */
@Slf4j
@Service("smartPredictorServiceImpl")
@RequiredArgsConstructor
public class SmartPredictorServiceImpl implements ISmartPredictorService {

    private final IProductoRepositorio productoRepository;
    private final IRegistroDemandaRepositorio registroDemandaRepository;
    private final HorizontePrediccionService horizontePrediccionService;
    private final IPrediccionRepositorio prediccionRepositorio;
    private final IUsuarioRepository usuarioRepository;
    private final IAnalisisEstacionalidadRepositorio analisisEstacionalidadRepository;
    private final IParametroAlgoritmoRepositorio parametroAlgoritmoRepositorio;

    private static final int MAX_PREDICCIONES_POR_CONFIGURACION = 5;

    @Override
    @Transactional
    public SmartPrediccionResponse generarPrediccionInteligente(SmartPrediccionRequest request) {
        log.info("Iniciando predicción inteligente Smile ML v3.1.1 para producto: {}", request.getIdProducto());

        try {
            // Validar datos del producto
            int minimoRegistros = request.getMinimoRegistrosHistoricos() != null
                    ? request.getMinimoRegistrosHistoricos()
                    : 10;

            if (!validarDatosHistoricosProducto(request.getIdProducto(), minimoRegistros)) {
                Producto producto = obtenerProducto(request.getIdProducto());
                long registrosActuales = registroDemandaRepository.countByProducto(producto);
                throw new IllegalStateException(
                        String.format(
                                "Datos históricos insuficientes para producto %d: tiene %d registros, se requieren mínimo %d",
                                request.getIdProducto(), registrosActuales, minimoRegistros));
            }

            // Obtener producto
            Producto producto = obtenerProducto(request.getIdProducto());

            // Obtener datos históricos normalizados
            List<RegistroDemanda> datosHistoricos = obtenerDatosHistoricos(request.getIdProducto());

            // Preparar serie temporal
            double[] serieTemporal = prepararSerieTemporal(datosHistoricos);

            // Seleccionar algoritmo (si es AUTO, ejecutar selección automática)
            String algoritmoSolicitado = request.getAlgoritmoSeleccionado();
            String algoritmoSeleccionado;

            if (algoritmoSolicitado == null || "AUTO".equalsIgnoreCase(algoritmoSolicitado)) {
                algoritmoSeleccionado = seleccionarMejorAlgoritmo(serieTemporal);
                log.info("Algoritmo seleccionado automáticamente: {}", algoritmoSeleccionado);
            } else {
                algoritmoSeleccionado = algoritmoSolicitado;
                log.info("Algoritmo seleccionado manualmente: {}", algoritmoSeleccionado);
            }

            // Ejecutar predicción
            ResultadoMLInterno resultado = ejecutarPrediccionML(serieTemporal, algoritmoSeleccionado, request);

            // Construir respuesta
            SmartPrediccionResponse response = construirRespuesta(producto, request, resultado);
            Prediccion prediccionPersistida = persistirPrediccion(producto, request, resultado, response);
            response.setIdPrediccion(prediccionPersistida.getPrediccionId().longValue());

            log.info("Predicción persistida con ID: {}", prediccionPersistida.getPrediccionId());

            log.info("Predicción completada: Algoritmo={}, Calidad={}",
                    response.getAlgoritmoUtilizado(), response.getMetricas().getCalificacionCalidad());

            return response;

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-lanzar excepciones de validación sin envolver
            throw e;

        } catch (Exception e) {
            log.error("Error inesperado en predicción inteligente: {}", e.getMessage(), e);
            throw new RuntimeException("Error ejecutando predicción inteligente", e);
        }
    }

    @Override
    public List<SmartPrediccionResponse> procesarProductosConAlertas() {
        log.info("Procesando productos con alertas de inventario");

        try {
            // TODO: Integrar con sistema de alertas
            // Por ahora, procesamos algunos productos de ejemplo
            List<SmartPrediccionResponse> resultados = new ArrayList<>();

            // Obtener productos disponibles
            List<Producto> productos = productoRepository.findAll().stream()
                    .limit(5) // Limitar para pruebas
                    .collect(Collectors.toList());

            for (Producto producto : productos) {
                try {
                    SmartPrediccionRequest request = new SmartPrediccionRequest();
                    request.setIdProducto(Long.valueOf(producto.getProductoId()));
                    request.setAlgoritmoSeleccionado("AUTO");
                    request.setHorizonteTiempo(30);
                    request.setDetectarEstacionalidad(true);
                    request.setGenerarOrdenCompra(true);

                    SmartPrediccionResponse prediccion = generarPrediccionInteligente(request);
                    resultados.add(prediccion);

                } catch (Exception e) {
                    log.warn("Error procesando producto {}: {}", producto.getProductoId(), e.getMessage());
                }
            }

            log.info("Procesamiento masivo completado: {} productos procesados", resultados.size());
            return resultados;

        } catch (Exception e) {
            log.error("Error en procesamiento masivo: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean validarDatosHistoricosProducto(Long idProducto, int minimoRegistros) {
        try {
            Producto producto = obtenerProducto(idProducto);
            long cantidadRegistros = registroDemandaRepository.countByProducto(producto);
            boolean esValido = cantidadRegistros >= minimoRegistros;

            log.debug("Validación producto {}: {} registros (mínimo: {}), válido: {}",
                    idProducto, cantidadRegistros, minimoRegistros, esValido);

            return esValido;

        } catch (Exception e) {
            log.error("Error validando datos del producto {}: {}", idProducto, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> obtenerAlgoritmosDisponibles() {
        return List.of(
                "AUTO",
                "RANDOM_FOREST",
                "LINEAR_REGRESSION",
                "GRADIENT_BOOSTING",
                "POLYNOMIAL_REGRESSION",
                "RIDGE_REGRESSION",
                "LASSO_REGRESSION",
                "ELASTIC_NET",
                "SUPPORT_VECTOR_REGRESSION");
    }

    @Override
    public SmartPrediccionResponse.MetricasCalidad obtenerMetricasCalidad(Long idPrediccion) {
        // TODO: Implementar obtención de métricas desde base de datos
        return SmartPrediccionResponse.MetricasCalidad.builder()
                .rmse(12.5)
                .mae(8.3)
                .mape(15.2)
                .rSquared(0.85)
                .calificacionCalidad("BUENA")
                .nivelConfianza(95)
                .build();
    }

    /**
     * Calcula el horizonte óptimo de predicción usando autocorrelación con SMILE
     * ML.
     *
     * @param idProducto ID del producto
     * @return Horizonte óptimo en días
     */
    public int calcularHorizonteAutomatico(Long idProducto) {
        try {
            List<RegistroDemanda> datosHistoricos = obtenerDatosHistoricos(idProducto);

            if (datosHistoricos.isEmpty()) {
                log.warn("Sin datos históricos para producto {}. Usando horizonte por defecto: 30", idProducto);
                return 30;
            }

            double[] serieTemporal = prepararSerieTemporal(datosHistoricos);
            int horizonteDias = horizontePrediccionService.calcularHorizonteConAutocorrelacion(serieTemporal);

            log.info("Horizonte automático calculado para producto {}: {} días", idProducto, horizonteDias);

            return horizonteDias;

        } catch (Exception e) {
            log.error("Error calculando horizonte automático para producto {}: {}", idProducto, e.getMessage());
            return 30; // Fallback a 30 días
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Obtiene el valor de un parámetro de algoritmo desde la base de datos.
     * Retorna el valor por defecto si no se encuentra el parámetro.
     */
    private int obtenerParametroInt(String tipoAlgoritmo, String nombreParametro, int valorDefecto) {
        return parametroAlgoritmoRepositorio
                .findByNombreParametroAndTipoAlgoritmo(nombreParametro, tipoAlgoritmo)
                .filter(ParametroAlgoritmo::getActivo)
                .map(p -> p.getValorParametro().intValue())
                .orElse(valorDefecto);
    }

    /**
     * Obtiene el valor de un parámetro de algoritmo como double.
     */
    private double obtenerParametroDouble(String tipoAlgoritmo, String nombreParametro, double valorDefecto) {
        return parametroAlgoritmoRepositorio
                .findByNombreParametroAndTipoAlgoritmo(nombreParametro, tipoAlgoritmo)
                .filter(ParametroAlgoritmo::getActivo)
                .map(p -> p.getValorParametro().doubleValue())
                .orElse(valorDefecto);
    }

    private Producto obtenerProducto(Long productoId) {
        return productoRepository.findById(Math.toIntExact(productoId))
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }

    private List<RegistroDemanda> obtenerDatosHistoricos(Long productoId) {
        Producto producto = obtenerProducto(productoId);
        return registroDemandaRepository.findByProducto(producto);
    }

    private double[] prepararSerieTemporal(List<RegistroDemanda> registros) {
        // Los registros ya están ordenados por fecha (query con ORDER BY)
        double[] serieOriginal = registros.stream()
                .mapToDouble(r -> r.getCantidadHistorica() != null ? r.getCantidadHistorica().doubleValue() : 0.0)
                .toArray();
        
        // Calcular variabilidad para decidir si aplicar preprocesamiento
        double variabilidad = calcularCoeficienteVariacion(serieOriginal);
        
        // Si la variabilidad es moderada-alta (>25%), aplicar técnicas de reducción de ruido
        if (variabilidad > 0.25 && serieOriginal.length >= 14) {
            log.info("[PREPROCESAMIENTO] Variabilidad alta detectada (CV={}). Aplicando suavizado + agregación.", 
                    String.format("%.2f", variabilidad));
            
            // Paso 1: Aplicar suavizado exponencial para reducir ruido diario
            double[] serieSuavizada = aplicarSuavizadoExponencial(serieOriginal, 0.3);
            
            // Paso 2: Si aún hay mucha variabilidad, agregar por semana
            double variabilidadSuavizada = calcularCoeficienteVariacion(serieSuavizada);
            if (variabilidadSuavizada > 0.30 && serieOriginal.length >= 21) {
                log.info("[PREPROCESAMIENTO] Aplicando agregación semanal (CV suavizado={})", 
                        String.format("%.2f", variabilidadSuavizada));
                double[] serieAgregada = agregarPorSemana(serieSuavizada);
                log.info("[PREPROCESAMIENTO] Serie reducida de {} a {} puntos (semanal)", 
                        serieOriginal.length, serieAgregada.length);
                return serieAgregada;
            }
            
            log.info("[PREPROCESAMIENTO] Suavizado aplicado. CV original={}, CV suavizado={}", 
                    String.format("%.2f", variabilidad), String.format("%.2f", variabilidadSuavizada));
            return serieSuavizada;
        }
        
        return serieOriginal;
    }
    
    /**
     * Aplica suavizado exponencial simple (SES) para reducir ruido en la serie.
     * 
     * Fórmula: S_t = α * Y_t + (1-α) * S_{t-1}
     * donde:
     * - α (alpha): factor de suavizado (0 < α < 1)
     * - Y_t: valor observado en tiempo t
     * - S_t: valor suavizado en tiempo t
     * 
     * Alpha bajo (0.1-0.3): más suavizado, responde lento a cambios
     * Alpha alto (0.7-0.9): menos suavizado, responde rápido a cambios
     * 
     * @param serie Serie temporal original
     * @param alpha Factor de suavizado (recomendado: 0.2-0.4 para datos ruidosos)
     * @return Serie suavizada
     */
    private double[] aplicarSuavizadoExponencial(double[] serie, double alpha) {
        if (serie.length == 0) return serie;
        
        double[] suavizada = new double[serie.length];
        suavizada[0] = serie[0]; // Primer valor sin cambio
        
        for (int i = 1; i < serie.length; i++) {
            suavizada[i] = alpha * serie[i] + (1 - alpha) * suavizada[i - 1];
        }
        
        return suavizada;
    }
    
    /**
     * Agrega la serie temporal por semanas para reducir variabilidad diaria.
     * 
     * Convierte datos diarios en datos semanales sumando o promediando
     * cada 7 días. Esto es especialmente útil para productos con alta
     * variabilidad día a día pero patrones semanales más estables.
     * 
     * @param serie Serie temporal diaria
     * @return Serie temporal semanal (menor longitud, menor variabilidad)
     */
    private double[] agregarPorSemana(double[] serie) {
        if (serie.length < 7) return serie;
        
        int numSemanas = serie.length / 7;
        double[] semanal = new double[numSemanas];
        
        for (int semana = 0; semana < numSemanas; semana++) {
            double suma = 0;
            int inicio = semana * 7;
            for (int dia = 0; dia < 7; dia++) {
                suma += serie[inicio + dia];
            }
            // Usar promedio diario de la semana (no suma) para mantener escala
            semanal[semana] = suma / 7.0;
        }
        
        return semanal;
    }

    /**
     * Selecciona automáticamente el mejor algoritmo basándose en características de
     * los datos.
     * 
     * Criterios de selección mejorados para minimarket:
     * 1. Datos insuficientes (< 10): LINEAR_REGRESSION (más estable)
     * 2. Datos limitados (< 20): LINEAR_REGRESSION (evita overfitting)
     * 3. NUEVO: Verificar autocorrelación antes de usar ARIMA - si es ruido blanco,
     *    ARIMA producirá predicciones pobres
     * 4. Alta variabilidad (CV > 0.7): GRADIENT_BOOSTING (productos perecederos)
     * 5. Estacionalidad detectada: RANDOM_FOREST (patrones complejos)
     * 6. Variabilidad moderada con autocorrelación: ARIMA
     * 7. Variabilidad moderada sin autocorrelación: RANDOM_FOREST o LINEAR_REGRESSION
     * 8. Baja variabilidad (CV < 0.3): LINEAR_REGRESSION (demanda estable)
     */
    private String seleccionarMejorAlgoritmo(double[] serie) {
        log.debug("Analizando serie temporal para selección automática de algoritmo");

        // Validar datos mínimos
        if (serie.length < AlgoritmoSmileML.LINEAR_REGRESSION.getMinimoRegistrosRequeridos()) {
            log.warn("Datos insuficientes ({}), usando LINEAR_REGRESSION por defecto", serie.length);
            return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
        }

        // Análisis de características de la demanda
        double variabilidad = calcularCoeficienteVariacion(serie);
        boolean tieneEstacionalidad = detectarEstacionalidad(serie);
        double tendencia = calcularTendencia(serie);
        
        // NUEVO: Calcular autocorrelación para verificar si ARIMA es apropiado
        double autocorrelacionLag1 = calcularAutocorrelacion(serie, 1);
        double autocorrelacionLag7 = calcularAutocorrelacion(serie, 7);
        boolean esRuidoBlanco = Math.abs(autocorrelacionLag1) < 0.15 && Math.abs(autocorrelacionLag7) < 0.15;

        log.debug("Características detectadas:");
        log.debug("   - Coeficiente de Variación (CV): {}", String.format("%.3f", variabilidad));
        log.debug("   - Estacionalidad: {}", tieneEstacionalidad);
        log.debug("   - Tendencia: {}", String.format("%.3f", tendencia));
        log.debug("   - Autocorrelación Lag-1: {}", String.format("%.4f", autocorrelacionLag1));
        log.debug("   - Autocorrelación Lag-7: {}", String.format("%.4f", autocorrelacionLag7));
        log.debug("   - Es Ruido Blanco: {}", esRuidoBlanco);
        log.debug("   - Registros disponibles: {}", serie.length);

        // REGLA 1: Datos limitados → LINEAR_REGRESSION
        if (serie.length < AlgoritmoSmileML.GRADIENT_BOOSTING.getMinimoRegistrosRequeridos()) {
            log.info("Seleccionado LINEAR_REGRESSION: datos limitados ({} registros)", serie.length);
            return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
        }

        // REGLA 2: Alta variabilidad → GRADIENT_BOOSTING (productos perecederos)
        if (variabilidad > 0.7) {
            log.info("Seleccionado GRADIENT_BOOSTING: alta variabilidad (CV={:.3f})", variabilidad);
            log.debug("   Optimo para: pan, lácteos, frutas, verduras, carnes");
            return AlgoritmoSmileML.GRADIENT_BOOSTING.getCodigo();
        }

        // REGLA 3: Estacionalidad detectada → RANDOM_FOREST
        if (tieneEstacionalidad && serie.length >= AlgoritmoSmileML.RANDOM_FOREST.getMinimoRegistrosRequeridos()) {
            log.info("Seleccionado RANDOM_FOREST: patrones estacionales detectados");
            log.debug("   Optimo para: bebidas, snacks, productos de limpieza");
            return AlgoritmoSmileML.RANDOM_FOREST.getCodigo();
        }

        // REGLA 4 (MEJORADA): Solo usar ARIMA si hay autocorrelación significativa
        // ARIMA requiere dependencia temporal; sin ella, produce predicciones pobres
        if (variabilidad >= 0.20 && variabilidad <= 0.5
                && serie.length >= AlgoritmoSmileML.ARIMA.getMinimoRegistrosRequeridos()
                && !esRuidoBlanco) {
            log.info("Seleccionado ARIMA: variabilidad moderada (CV={}) con autocorrelación detectada (lag1={}, lag7={})",
                    String.format("%.3f", variabilidad),
                    String.format("%.3f", autocorrelacionLag1),
                    String.format("%.3f", autocorrelacionLag7));
            log.debug("   ARIMA captura dependencia temporal y autocorrelación en series regulares");
            return AlgoritmoSmileML.ARIMA.getCodigo();
        }
        
        // REGLA 4b (NUEVA): Si sería candidato a ARIMA pero es ruido blanco → usar promedio móvil o LINEAR
        if (variabilidad >= 0.20 && variabilidad <= 0.5 && esRuidoBlanco) {
            log.info("Detectado RUIDO BLANCO: autocorrelación muy baja (lag1={}, lag7={}). ARIMA no recomendado.",
                    String.format("%.4f", autocorrelacionLag1),
                    String.format("%.4f", autocorrelacionLag7));
            log.debug("   Datos sin patrón temporal claro - usando LINEAR_REGRESSION como fallback estable");
            return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
        }

        // REGLA 5: Variabilidad moderada-alta → RANDOM_FOREST
        if (variabilidad > 0.5 && variabilidad <= 0.7) {
            if (serie.length >= AlgoritmoSmileML.RANDOM_FOREST.getMinimoRegistrosRequeridos()) {
                log.info("Seleccionado RANDOM_FOREST: variabilidad moderada-alta (CV={})",
                        String.format("%.3f", variabilidad));
                return AlgoritmoSmileML.RANDOM_FOREST.getCodigo();
            } else {
                log.info("Seleccionado LINEAR_REGRESSION: variabilidad moderada pero pocos datos");
                return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
            }
        }

        // REGLA 6: Baja variabilidad y tendencia clara → LINEAR_REGRESSION
        if (variabilidad < 0.20) {
            log.info("Seleccionado LINEAR_REGRESSION: baja variabilidad (CV={})", String.format("%.3f", variabilidad));
            log.debug("   Óptimo para: arroz, azúcar, sal, aceite");
            return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
        }

        // DEFAULT: ARIMA solo si hay autocorrelación, sino LINEAR_REGRESSION
        if (serie.length >= AlgoritmoSmileML.ARIMA.getMinimoRegistrosRequeridos() && !esRuidoBlanco) {
            log.info("Seleccionado ARIMA: algoritmo predeterminado para series temporales con autocorrelación");
            return AlgoritmoSmileML.ARIMA.getCodigo();
        }

        // FALLBACK: LINEAR_REGRESSION (estable para cualquier tipo de datos)
        log.info("Seleccionado LINEAR_REGRESSION: fallback estable para datos sin patrón temporal");
        return AlgoritmoSmileML.LINEAR_REGRESSION.getCodigo();
    }
    
    /**
     * Calcula la autocorrelación de la serie para un lag específico.
     * Valores cercanos a 0 indican "ruido blanco" (sin dependencia temporal).
     * Valores > 0.3 indican dependencia temporal significativa.
     * 
     * La autocorrelación mide cuánto se correlaciona un valor con su valor anterior (lag).
     * Es fundamental para determinar si ARIMA es apropiado.
     * 
     * @param serie Array de valores de la serie temporal
     * @param lag Número de períodos de retraso a considerar
     * @return Coeficiente de autocorrelación entre -1 y 1
     */
    private double calcularAutocorrelacion(double[] serie, int lag) {
        if (serie.length <= lag + 1) {
            return 0.0;
        }
        
        int n = serie.length - lag;
        
        // Calcular medias
        double mediaX = 0.0, mediaY = 0.0;
        for (int i = 0; i < n; i++) {
            mediaX += serie[i];
            mediaY += serie[i + lag];
        }
        mediaX /= n;
        mediaY /= n;
        
        // Calcular covarianza y varianzas
        double covarianza = 0.0;
        double varianzaX = 0.0;
        double varianzaY = 0.0;
        
        for (int i = 0; i < n; i++) {
            double dx = serie[i] - mediaX;
            double dy = serie[i + lag] - mediaY;
            covarianza += dx * dy;
            varianzaX += dx * dx;
            varianzaY += dy * dy;
        }
        
        double denominador = Math.sqrt(varianzaX * varianzaY);
        if (denominador == 0) {
            return 0.0;
        }
        
        return covarianza / denominador;
    }

    private ResultadoMLInterno ejecutarPrediccionML(double[] serie, String algoritmo, SmartPrediccionRequest request) {
        log.info("Ejecutando predicción con algoritmo: {}", algoritmo);

        try {
            int horizonte = request.getHorizonteTiempo() != null ? request.getHorizonteTiempo() : 30;

            log.info("Serie completa: {} valores", serie.length);
            log.info("Primeros 10 valores serie: {}",
                    Arrays.stream(Arrays.copyOfRange(serie, 0, Math.min(10, serie.length))).mapToObj(String::valueOf)
                            .collect(java.util.stream.Collectors.joining(", ")));
            log.info("Últimos 10 valores serie: {}",
                    Arrays.stream(Arrays.copyOfRange(serie, Math.max(0, serie.length - 10), serie.length))
                            .mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(", ")));

            // Split train/validation (80/20) para calcular R² correctamente
            int trainSize = (int) (serie.length * 0.8);
            int validationSize = serie.length - trainSize;

            // Si hay pocos datos, usar todos para entrenamiento
            if (validationSize < 5) {
                trainSize = serie.length;
                validationSize = 0;
                log.debug("Datos insuficientes para validation set, usando todos para entrenamiento");
            }

            log.info("Train/Validation Split: {} training, {} validation", trainSize, validationSize);

            double[] trainSerie = Arrays.copyOfRange(serie, 0, trainSize);
            double[] validationSerie = validationSize > 0 ? Arrays.copyOfRange(serie, trainSize, serie.length)
                    : new double[0];

            if (validationSize > 0) {
                log.info("Validation set - Primeros 5 valores reales: {}",
                        Arrays.stream(Arrays.copyOfRange(validationSerie, 0, Math.min(5, validationSize)))
                                .mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(", ")));
            }

            // Preparar datos temporales para entrenamiento (solo tiempo, sin features que
            // causen data leakage)
            double[] tiempo = new double[trainSerie.length];
            for (int i = 0; i < trainSerie.length; i++) {
                tiempo[i] = i;
            }

            // Crear DataFrame simple con tiempo y demanda
            DataFrame dataFrame = DataFrame.of(
                    DoubleVector.of("tiempo", tiempo),
                    DoubleVector.of("demanda", trainSerie));

            log.debug("DataFrame creado con feature: tiempo");

            // Seleccionar y ejecutar algoritmo específico
            AlgoritmoSmileML algoritmoEnum = AlgoritmoSmileML.fromCodigo(algoritmo);
            List<Double> prediccionesFuturas;
            double[] prediccionesValidacion = new double[validationSize];

            switch (algoritmoEnum) {
                case LINEAR_REGRESSION:
                    prediccionesFuturas = ejecutarLinearRegression(dataFrame, trainSerie, horizonte);
                    if (validationSize > 0) {
                        prediccionesValidacion = ejecutarPrediccionValidacion(dataFrame, trainSerie, validationSize,
                                algoritmoEnum);
                    }
                    break;

                case ARIMA:
                    // ARIMA implementado como OLS con lag features (características de retraso
                    // temporal)
                    prediccionesFuturas = ejecutarTimeSeriesRegression(trainSerie, horizonte);
                    if (validationSize > 0) {
                        prediccionesValidacion = ejecutarPrediccionValidacionTimeSeries(trainSerie, validationSize);
                    }
                    break;

                case RANDOM_FOREST:
                    prediccionesFuturas = ejecutarRandomForest(dataFrame, trainSerie, horizonte);
                    if (validationSize > 0) {
                        prediccionesValidacion = ejecutarPrediccionValidacion(dataFrame, trainSerie, validationSize,
                                algoritmoEnum);
                    }
                    break;

                case GRADIENT_BOOSTING:
                    prediccionesFuturas = ejecutarGradientBoosting(dataFrame, trainSerie, horizonte);
                    if (validationSize > 0) {
                        prediccionesValidacion = ejecutarPrediccionValidacion(dataFrame, trainSerie, validationSize,
                                algoritmoEnum);
                    }
                    break;

                default:
                    log.warn("Algoritmo {} no reconocido, usando Time Series Regression", algoritmo);
                    prediccionesFuturas = ejecutarTimeSeriesRegression(trainSerie, horizonte);
                    if (validationSize > 0) {
                        prediccionesValidacion = ejecutarPrediccionValidacionTimeSeries(trainSerie, validationSize);
                    }
            }

            // Calcular métricas sobre validation set (si existe)
            double rmse = 0.0;
            double mae = 0.0;
            double mape = 0.0;

            if (validationSize > 0) {
                log.info("Validation set - Predicciones vs Reales (primeros 5):");
                for (int i = 0; i < Math.min(5, validationSize); i++) {
                    log.info("   [{}] Predicción: {}, Real: {}", i,
                            String.format("%.2f", prediccionesValidacion[i]),
                            String.format("%.2f", validationSerie[i]));
                }

                rmse = calcularRMSE(validationSerie, prediccionesValidacion);
                mae = calcularMAE(validationSerie, prediccionesValidacion);
                mape = calcularMAPE(validationSerie, prediccionesValidacion);
                log.info("Métricas en validation set: RMSE={}, MAE={}, MAPE={}%",
                        String.format("%.2f", rmse),
                        String.format("%.2f", mae),
                        String.format("%.1f", mape));
            } else {
                log.debug("Sin validation set, métricas no disponibles");
            }

            return ResultadoMLInterno.builder()
                    .algoritmo(algoritmoEnum.getCodigo())
                    .predicciones(prediccionesFuturas)
                    .rmse(rmse)
                    .mae(mae)
                    .mape(mape)
                    .confianza(validationSize > 0 ? calcularConfianza(mape) : 0.7)
                    .build();

        } catch (Exception e) {
            log.error("Error ejecutando algoritmo {}: {}", algoritmo, e.getMessage(), e);
            throw new RuntimeException("Error en predicción ML: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta predicción sobre validation set para calcular métricas
     */
    private double[] ejecutarPrediccionValidacion(DataFrame trainDataFrame, double[] trainSerie, int validationSize,
            AlgoritmoSmileML algoritmo) {
        try {
            Formula formula = Formula.lhs("demanda");
            double[] predicciones = new double[validationSize];

            switch (algoritmo) {
                case LINEAR_REGRESSION:
                    LinearModel modeloLinear = OLS.fit(formula, trainDataFrame);
                    for (int i = 0; i < validationSize; i++) {
                        double tiempoFuturo = trainSerie.length + i;
                        DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
                        predicciones[i] = Math.max(0, modeloLinear.predict(punto.get(0)));
                    }
                    break;

                case RANDOM_FOREST:
                    int numArboles = Math.min(100, Math.max(50, trainSerie.length / 2));
                    RandomForest modeloRF = RandomForest.fit(formula, trainDataFrame,
                            numArboles,
                            1, // mtry = 1 (solo tiempo)
                            10, // max depth
                            100, // max nodes
                            5, // node size
                            1.0 // subsample
                    );
                    for (int i = 0; i < validationSize; i++) {
                        double tiempoFuturo = trainSerie.length + i;
                        DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
                        predicciones[i] = Math.max(0, modeloRF.predict(punto.get(0)));
                    }
                    break;

                case GRADIENT_BOOSTING:
                    GradientTreeBoost modeloGB = GradientTreeBoost.fit(formula, trainDataFrame,
                            new Properties() {
                                {
                                    setProperty("smile.gbt.trees",
                                            String.valueOf(Math.min(200, Math.max(100, trainSerie.length))));
                                    setProperty("smile.gbt.shrinkage", "0.05");
                                    setProperty("smile.gbt.max.depth", "6");
                                    setProperty("smile.gbt.max.nodes", "50");
                                    setProperty("smile.gbt.node.size", "5");
                                    setProperty("smile.gbt.sample.rate", "0.7");
                                }
                            });
                    for (int i = 0; i < validationSize; i++) {
                        double tiempoFuturo = trainSerie.length + i;
                        DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
                        predicciones[i] = Math.max(0, modeloGB.predict(punto.get(0)));
                    }
                    break;
            }

            return predicciones;

        } catch (Exception e) {
            log.error("[PREDICCION] Error en predicción de validación: {}", e.getMessage());
            return new double[validationSize];
        }
    }

    /**
     * Implementación de Regresión Lineal con OLS (Ordinary Least Squares)
     * Óptimo para productos con demanda estable: arroz, azúcar, sal, aceite
     */
    private List<Double> ejecutarLinearRegression(DataFrame dataFrame, double[] serie, int horizonte) {
        log.debug("[PREDICCION] Ejecutando OLS Linear Regression");

        Formula formula = Formula.lhs("demanda");
        LinearModel modelo = OLS.fit(formula, dataFrame);

        List<Double> predicciones = new ArrayList<>();
        for (int i = 0; i < horizonte; i++) {
            double tiempoFuturo = serie.length + i;
            DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
            double prediccion = modelo.predict(punto.get(0));
            predicciones.add(Math.max(0, prediccion));
        }

        return predicciones;
    }

    /**
     * Implementación de Random Forest con parámetros dinámicos desde BD.
     * Óptimo para patrones complejos: bebidas, snacks, productos de limpieza.
     */
    private List<Double> ejecutarRandomForest(DataFrame dataFrame, double[] serie, int horizonte) {
        log.debug("[PREDICCION] Ejecutando Random Forest Regression con parámetros dinámicos");

        Formula formula = Formula.lhs("demanda");

        // Obtener parámetros desde la BD (con fallback a valores por defecto)
        int numArbolesConfig = obtenerParametroInt("RANDOM_FOREST", "num_arboles", 100);
        int maxDepth = obtenerParametroInt("RANDOM_FOREST", "max_depth", 10);
        int maxNodes = obtenerParametroInt("RANDOM_FOREST", "max_nodes", 100);
        int nodeSize = obtenerParametroInt("RANDOM_FOREST", "node_size", 5);
        double subsample = obtenerParametroDouble("RANDOM_FOREST", "subsample", 1.0);

        // Ajustar número de árboles según tamaño de datos
        int numArboles = Math.min(numArbolesConfig, Math.max(50, serie.length / 2));
        int mtry = 1; // Solo tiempo como feature

        log.debug("[PARAMETROS RF] arboles={}, maxDepth={}, maxNodes={}, nodeSize={}, subsample={}",
                numArboles, maxDepth, maxNodes, nodeSize, subsample);

        RandomForest modelo = RandomForest.fit(
                formula,
                dataFrame,
                numArboles,
                mtry,
                maxDepth,
                maxNodes,
                nodeSize,
                subsample);

        List<Double> predicciones = new ArrayList<>();
        for (int i = 0; i < horizonte; i++) {
            double tiempoFuturo = serie.length + i;
            DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
            double prediccion = modelo.predict(punto.get(0));
            predicciones.add(Math.max(0, prediccion));
        }

        return predicciones;
    }

    /**
     * Implementación de Gradient Boosting Machine con parámetros dinámicos.
     * Óptimo para alta variabilidad: pan, lácteos, frutas, verduras, carnes.
     */
    private List<Double> ejecutarGradientBoosting(DataFrame dataFrame, double[] serie, int horizonte) {
        log.debug("[PREDICCION] Ejecutando Gradient Boosting Regression con parámetros dinámicos");

        Formula formula = Formula.lhs("demanda");

        // Obtener parámetros desde la BD (con fallback a valores por defecto)
        int numTreesConfig = obtenerParametroInt("GRADIENT_BOOSTING", "num_trees", 200);
        double shrinkage = obtenerParametroDouble("GRADIENT_BOOSTING", "shrinkage", 0.05);
        int maxDepth = obtenerParametroInt("GRADIENT_BOOSTING", "max_depth", 6);
        int maxNodes = obtenerParametroInt("GRADIENT_BOOSTING", "max_nodes", 50);
        int nodeSize = obtenerParametroInt("GRADIENT_BOOSTING", "node_size", 5);
        double sampleRate = obtenerParametroDouble("GRADIENT_BOOSTING", "sample_rate", 0.7);

        // Ajustar número de árboles según tamaño de datos
        int numTrees = Math.min(numTreesConfig, Math.max(100, serie.length));

        log.debug("[PARAMETROS GBT] trees={}, shrinkage={}, maxDepth={}, maxNodes={}, nodeSize={}, sampleRate={}",
                numTrees, shrinkage, maxDepth, maxNodes, nodeSize, sampleRate);

        java.util.Properties props = new java.util.Properties();
        props.setProperty("smile.gbt.trees", String.valueOf(numTrees));
        props.setProperty("smile.gbt.shrinkage", String.valueOf(shrinkage));
        props.setProperty("smile.gbt.max.depth", String.valueOf(maxDepth));
        props.setProperty("smile.gbt.max.nodes", String.valueOf(maxNodes));
        props.setProperty("smile.gbt.node.size", String.valueOf(nodeSize));
        props.setProperty("smile.gbt.sample.rate", String.valueOf(sampleRate));

        GradientTreeBoost modelo = GradientTreeBoost.fit(formula, dataFrame, props);

        List<Double> predicciones = new ArrayList<>();
        for (int i = 0; i < horizonte; i++) {
            double tiempoFuturo = serie.length + i;
            DataFrame punto = DataFrame.of(DoubleVector.of("tiempo", new double[] { tiempoFuturo }));
            double prediccion = modelo.predict(punto.get(0));
            predicciones.add(Math.max(0, prediccion));
        }

        return predicciones;
    }

    private SmartPrediccionResponse construirRespuesta(Producto producto, SmartPrediccionRequest request,
            ResultadoMLInterno resultado) {

        // Obtener análisis de estacionalidad guardado para aplicar coeficientes
        Optional<AnalisisEstacionalidad> analisisEstacionalidadOpt = analisisEstacionalidadRepository
                .findByProductoIdAndActivoTrue(request.getIdProducto());

        // LOG: Verificar si se encontró análisis de estacionalidad
        if (analisisEstacionalidadOpt.isPresent()) {
            AnalisisEstacionalidad ae = analisisEstacionalidadOpt.get();
            log.info("[ESTACIONALIDAD] Análisis encontrado para producto {}: tieneEstacionalidad={}, intensidad={}, coefNov={}, coefDic={}",
                    request.getIdProducto(), ae.getTieneEstacionalidad(), ae.getIntensidadEstacionalidad(),
                    ae.getCoeficienteNoviembre(), ae.getCoeficienteDiciembre());
        } else {
            log.warn("[ESTACIONALIDAD] NO se encontró análisis de estacionalidad para producto {}", request.getIdProducto());
        }

        // Generar predicciones detalladas con ajuste estacional
        List<SmartPrediccionResponse.PrediccionDetalle> prediccionesDetalladas = new ArrayList<>();
        for (int i = 0; i < resultado.getPredicciones().size(); i++) {
            LocalDate fecha = LocalDate.now().plusDays(i + 1);
            Double demandaBase = resultado.getPredicciones().get(i);

            // Aplicar coeficiente estacional si hay análisis guardado
            Double demandaAjustada = demandaBase;
            if (analisisEstacionalidadOpt.isPresent() && Boolean.TRUE.equals(request.getDetectarEstacionalidad())) {
                BigDecimal coeficiente = obtenerCoeficienteMes(analisisEstacionalidadOpt.get(), fecha.getMonthValue());
                log.info("[ESTACIONALIDAD] Mes {}: coeficiente={}", fecha.getMonthValue(), coeficiente);
                if (coeficiente != null && coeficiente.compareTo(BigDecimal.ZERO) > 0) {
                    demandaAjustada = demandaBase * coeficiente.doubleValue();
                    log.info("[ESTACIONALIDAD] Ajuste aplicado: base={} * coef={} = ajustada={}",
                            demandaBase, coeficiente, demandaAjustada);
                }
            }

            prediccionesDetalladas.add(SmartPrediccionResponse.PrediccionDetalle.builder()
                    .periodo(i + 1)
                    .fecha(fecha.toString())
                    .demandaPredicha(demandaAjustada)
                    .intervaloCorrfianzaInferior(demandaAjustada * 0.9)
                    .intervaloConfianzaSuperior(demandaAjustada * 1.1)
                    .build());
        }

        // Calcular demanda total (ya ajustada estacionalmente)
        double demandaTotal = prediccionesDetalladas.stream()
                .mapToDouble(SmartPrediccionResponse.PrediccionDetalle::getDemandaPredicha)
                .sum();

        // Analizar estacionalidad mensual de datos históricos
        EstacionalidadMensual estacionalidadMensual = analizarEstacionalidadMensual(request.getIdProducto());

        // Determinar algoritmo real y razón de selección
        String algoritmoReal = resultado.getAlgoritmo();
        String razonSeleccion;
        String algoritmoSolicitado = request.getAlgoritmoSeleccionado();

        if (algoritmoSolicitado == null || "AUTO".equalsIgnoreCase(algoritmoSolicitado)) {
            // Modo automático: mostrar algoritmo seleccionado y criterios
            razonSeleccion = construirRazonSeleccionAutomatica(algoritmoReal, request.getIdProducto());
        } else {
            // Modo manual: el usuario eligió el algoritmo
            razonSeleccion = String.format("Algoritmo seleccionado manualmente por el usuario: %s", algoritmoReal);
        }

        SmartPrediccionResponse response = SmartPrediccionResponse.builder()
                .idProducto(request.getIdProducto())
                .nombreProducto(producto.getNombre())
                .fechaEjecucion(LocalDateTime.now())
                .horizonteTiempo(request.getHorizonteTiempo())
                .algoritmoUtilizado(algoritmoReal)
                .razonSeleccionAlgoritmo(razonSeleccion)
                .demandaTotalPredicha(demandaTotal)
                .prediccionesDetalladas(prediccionesDetalladas)
                .metricas(SmartPrediccionResponse.MetricasCalidad.builder()
                        .rmse(resultado.getRmse())
                        .mae(resultado.getMae())
                        .mape(resultado.getMape())
                        .rSquared(0.0) // Deprecado: ya no se usa para calidad
                        .calificacionCalidad(clasificarCalidad(resultado.getMape()))
                        .nivelConfianza(95)
                        .build())
                .estacionalidad(SmartPrediccionResponse.EstacionalidadInfo.builder()
                        // tieneEstacionalidad se basa en si hay análisis guardado Y activo (no solo el flag del request)
                        .tieneEstacionalidad(analisisEstacionalidadOpt.isPresent() && Boolean.TRUE.equals(request.getDetectarEstacionalidad()))
                        .intensidadEstacional(estacionalidadMensual.getIntensidad())
                        .mesMayorDemanda(estacionalidadMensual.getMesMayor())
                        .mesMenorDemanda(estacionalidadMensual.getMesMenor())
                        .patronDetectado(determinarPatronEstacional(estacionalidadMensual.getIntensidad()))
                        .horizonteSugerido(request.getHorizonteTiempo())
                        .build())
                .riesgoQuiebre(SmartPrediccionResponse.RiesgoQuiebreInfo.builder()
                        .stockActual(100) // TODO: Obtener valor real
                        .stockMinimo(20)
                        .puntoReorden(50)
                        .diasHastaQuiebre(calcularDiasHastaQuiebre(demandaTotal, request.getHorizonteTiempo()))
                        .nivelRiesgo(evaluarRiesgo(demandaTotal))
                        .requiereAccionInmediata(false)
                        .build())
                .ordenCompraSugerida(
                        request.getGenerarOrdenCompra() ? SmartPrediccionResponse.OrdenCompraSugerida.builder()
                                .sugerirOrden(true)
                                .cantidadSugerida((int) Math.ceil(demandaTotal * 1.2))
                                .justificacionCantidad("Basado en predicción ML con factor de seguridad")
                                .fechaSugeridaPedido(LocalDate.now().plusDays(7).toString())
                                .prioridadPedido("MEDIA")
                                .build() : null)
                .build();

        return response;
    }

    /**
     * Analiza la estacionalidad mensual de los datos históricos
     */
    private EstacionalidadMensual analizarEstacionalidadMensual(Long idProducto) {
        try {
            List<RegistroDemanda> datosHistoricos = obtenerDatosHistoricos(idProducto);

            if (datosHistoricos.isEmpty() || datosHistoricos.size() < 30) {
                return EstacionalidadMensual.builder()
                        .mesMayor(null)
                        .mesMenor(null)
                        .intensidad(0.0)
                        .build();
            }

            // Agrupar demanda por mes
            Map<Integer, Double> demandaPorMes = datosHistoricos.stream()
                    .collect(Collectors.groupingBy(
                            registro -> registro.getFechaRegistro().getMonthValue(),
                            Collectors.summingDouble(registro -> registro.getCantidadHistorica().doubleValue())));

            if (demandaPorMes.isEmpty()) {
                return EstacionalidadMensual.builder()
                        .mesMayor(null)
                        .mesMenor(null)
                        .intensidad(0.0)
                        .build();
            }

            // Encontrar mes con mayor y menor demanda
            Map.Entry<Integer, Double> mesMayor = demandaPorMes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            Map.Entry<Integer, Double> mesMenor = demandaPorMes.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElse(null);

            // Calcular intensidad (coeficiente de variación)
            double media = demandaPorMes.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double desviacion = Math.sqrt(demandaPorMes.values().stream()
                    .mapToDouble(v -> Math.pow(v - media, 2))
                    .average()
                    .orElse(0.0));

            double intensidad = media > 0 ? desviacion / media : 0.0;

            return EstacionalidadMensual.builder()
                    .mesMayor(mesMayor != null ? mesMayor.getKey() : null)
                    .mesMenor(mesMenor != null ? mesMenor.getKey() : null)
                    .intensidad(intensidad)
                    .build();

        } catch (Exception e) {
            log.warn("[ESTACIONALIDAD] Advertencia: Error analizando estacionalidad mensual para producto {}: {}",
                    idProducto, e.getMessage());
            return EstacionalidadMensual.builder()
                    .mesMayor(null)
                    .mesMenor(null)
                    .intensidad(0.0)
                    .build();
        }
    }

    /**
     * Construye una razón detallada explicando por qué se seleccionó el algoritmo
     */
    private String construirRazonSeleccionAutomatica(String algoritmo, Long idProducto) {
        try {
            List<RegistroDemanda> datosHistoricos = obtenerDatosHistoricos(idProducto);
            
            // Obtener serie ORIGINAL (sin preprocesamiento) para mostrar variabilidad real
            double[] serieOriginal = datosHistoricos.stream()
                    .mapToDouble(r -> r.getCantidadHistorica() != null ? r.getCantidadHistorica().doubleValue() : 0.0)
                    .toArray();
            
            // Obtener serie procesada para comparar
            double[] serieProcesada = prepararSerieTemporal(datosHistoricos);

            double variabilidadOriginal = calcularCoeficienteVariacion(serieOriginal);
            double variabilidadProcesada = calcularCoeficienteVariacion(serieProcesada);
            boolean tieneEstacionalidad = detectarEstacionalidad(serieProcesada);
            double tendencia = calcularTendencia(serieProcesada);
            
            // Detectar si se aplicó preprocesamiento
            boolean seAplicoPreprocesamiento = serieOriginal.length != serieProcesada.length || 
                    Math.abs(variabilidadOriginal - variabilidadProcesada) > 0.05;

            StringBuilder razon = new StringBuilder();
            razon.append("Selección automática mediante análisis ML: ");
            
            // Agregar info de preprocesamiento si se aplicó
            if (seAplicoPreprocesamiento) {
                razon.append("PREPROCESAMIENTO APLICADO: ");
                if (serieOriginal.length != serieProcesada.length) {
                    razon.append("Agregación semanal (")
                            .append(serieOriginal.length).append("→").append(serieProcesada.length)
                            .append(" puntos). ");
                }
                razon.append("CV reducido de ")
                        .append(String.format("%.1f%%", variabilidadOriginal * 100))
                        .append(" a ")
                        .append(String.format("%.1f%%", variabilidadProcesada * 100))
                        .append(". ");
            }

            switch (algoritmo) {
                case "RANDOM_FOREST":
                    if (tieneEstacionalidad) {
                        razon.append("Datos con patrones estacionales detectados (CV=")
                                .append(String.format("%.2f", variabilidadProcesada))
                                .append("). Random Forest maneja mejor relaciones no lineales y patrones complejos.");
                    } else {
                        razon.append("Datos con variabilidad moderada (CV=")
                                .append(String.format("%.2f", variabilidadProcesada))
                                .append("). Random Forest es robusto para casos generales.");
                    }
                    break;

                case "GRADIENT_BOOSTING":
                    razon.append("Alta variabilidad detectada (CV=")
                            .append(String.format("%.2f", variabilidadProcesada))
                            .append("). Gradient Boosting es óptimo para capturar fluctuaciones complejas.");
                    break;

                case "LINEAR_REGRESSION":
                    double autocorrLag1 = calcularAutocorrelacion(serieProcesada, 1);
                    double autocorrLag7 = calcularAutocorrelacion(serieProcesada, 7);
                    boolean esRuidoBlanco = Math.abs(autocorrLag1) < 0.15 && Math.abs(autocorrLag7) < 0.15;
                    
                    if (serieProcesada.length < 10) {
                        razon.append("Pocos datos históricos (")
                                .append(serieProcesada.length)
                                .append(" registros). Regresión lineal previene sobreajuste con datos limitados.");
                    } else if (esRuidoBlanco && variabilidadProcesada > 0.25) {
                        razon.append("Datos tipo 'ruido blanco' detectados (autocorr_lag1=")
                                .append(String.format("%.3f", autocorrLag1))
                                .append(", autocorr_lag7=")
                                .append(String.format("%.3f", autocorrLag7))
                                .append("). Sin patrón temporal claro. ")
                                .append("LINEAR_REGRESSION predice tendencia promedio.");
                        if (seAplicoPreprocesamiento) {
                            razon.append(" Suavizado+agregación mejoran precisión.");
                        }
                    } else {
                        razon.append("Tendencia lineal detectada (coef=")
                                .append(String.format("%.3f", tendencia))
                                .append("). Regresión lineal OLS (Smile ML) es eficiente para tendencias consistentes.");
                    }
                    break;

                default:
                    razon.append(algoritmo)
                            .append(" seleccionado por análisis de características de los datos históricos.");
            }

            return razon.toString();

        } catch (Exception e) {
            log.warn("[PREDICCION] Advertencia: Error construyendo razón de selección: {}", e.getMessage());
            return String.format("Algoritmo %s seleccionado automáticamente basado en características de los datos",
                    algoritmo);
        }
    }

    // ========== MÉTODOS AUXILIARES DE CÁLCULO ==========

    /**
     * Obtiene el coeficiente estacional para un mes específico desde el análisis
     * guardado.
     * Los coeficientes representan la variación respecto al promedio (1.0 =
     * promedio).
     * Ejemplo: coeficiente 1.48 para diciembre significa demanda 48% mayor que el
     * promedio.
     */
    private BigDecimal obtenerCoeficienteMes(AnalisisEstacionalidad analisis, int mes) {
        if (analisis == null) {
            return BigDecimal.ONE;
        }

        return switch (mes) {
            case 1 -> analisis.getCoeficienteEnero();
            case 2 -> analisis.getCoeficienteFebrero();
            case 3 -> analisis.getCoeficienteMarzo();
            case 4 -> analisis.getCoeficienteAbril();
            case 5 -> analisis.getCoeficienteMayo();
            case 6 -> analisis.getCoeficienteJunio();
            case 7 -> analisis.getCoeficienteJulio();
            case 8 -> analisis.getCoeficienteAgosto();
            case 9 -> analisis.getCoeficienteSeptiembre();
            case 10 -> analisis.getCoeficienteOctubre();
            case 11 -> analisis.getCoeficienteNoviembre();
            case 12 -> analisis.getCoeficienteDiciembre();
            default -> BigDecimal.ONE;
        };
    }

    private double calcularCoeficienteVariacion(double[] serie) {
        double media = Arrays.stream(serie).average().orElse(0.0);
        if (media == 0)
            return 1.0;

        double desviacion = Math.sqrt(Arrays.stream(serie)
                .map(x -> Math.pow(x - media, 2))
                .average().orElse(0.0));

        return desviacion / media;
    }

    private boolean detectarEstacionalidad(double[] serie) {
        if (serie.length < 14)
            return false;

        // Análisis simple de autocorrelación para patrones semanales
        int lag = 7;
        if (serie.length <= lag)
            return false;

        double correlacion = calcularCorrelacion(
                Arrays.copyOfRange(serie, 0, serie.length - lag),
                Arrays.copyOfRange(serie, lag, serie.length));

        return Math.abs(correlacion) > 0.3;
    }

    private double calcularTendencia(double[] serie) {
        if (serie.length < 2)
            return 0.0;

        double[] tiempo = new double[serie.length];
        for (int i = 0; i < serie.length; i++) {
            tiempo[i] = i;
        }

        // Calcular pendiente simple
        double n = serie.length;
        double sumX = Arrays.stream(tiempo).sum();
        double sumY = Arrays.stream(serie).sum();
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < serie.length; i++) {
            sumXY += tiempo[i] * serie[i];
            sumX2 += tiempo[i] * tiempo[i];
        }

        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }

    private double calcularRMSE(double[] actual, double[] prediccion) {
        int minLength = Math.min(actual.length, prediccion.length);
        double suma = 0.0;

        for (int i = 0; i < minLength; i++) {
            suma += Math.pow(actual[i] - prediccion[i], 2);
        }

        return Math.sqrt(suma / minLength);
    }

    private double calcularMAE(double[] actual, double[] prediccion) {
        int minLength = Math.min(actual.length, prediccion.length);
        double suma = 0.0;

        for (int i = 0; i < minLength; i++) {
            suma += Math.abs(actual[i] - prediccion[i]);
        }

        return suma / minLength;
    }

    /**
     * Calcula el Mean Absolute Percentage Error (MAPE)
     * Mide el error porcentual promedio de las predicciones
     * 
     * MAPE = (1/n) * Σ |actual - prediccion| / actual * 100
     * 
     * Valores: 0% (perfecto) a 100%+ (muy malo)
     * Interpretación:
     * - MAPE < 10%: Predicción EXCELENTE
     * - 10% ≤ MAPE < 20%: Predicción BUENA
     * - 20% ≤ MAPE < 30%: Predicción REGULAR
     * - MAPE ≥ 30%: Predicción POBRE
     */
    private double calcularMAPE(double[] actual, double[] prediccion) {
        int minLength = Math.min(actual.length, prediccion.length);

        if (minLength == 0)
            return 100.0;

        double sumaErroresPorcentuales = 0.0;
        int countValidos = 0;

        for (int i = 0; i < minLength; i++) {
            // Evitar división por cero cuando el valor real es 0
            if (actual[i] != 0.0) {
                double errorPorcentual = Math.abs((actual[i] - prediccion[i]) / actual[i]) * 100.0;
                sumaErroresPorcentuales += errorPorcentual;
                countValidos++;
            }
        }

        if (countValidos == 0)
            return 100.0;

        return sumaErroresPorcentuales / countValidos;
    }

    /**
     * Calcula nivel de confianza basado en MAPE
     * Confianza inversamente proporcional al error porcentual
     */
    private double calcularConfianza(double mape) {
        // MAPE 0% → confianza 95%
        // MAPE 10% → confianza 85%
        // MAPE 20% → confianza 75%
        // MAPE 30% → confianza 65%
        // MAPE 50%+ → confianza 50%

        if (mape < 10)
            return 0.95 - (mape / 100.0);
        if (mape < 20)
            return 0.85 - ((mape - 10) / 100.0);
        if (mape < 30)
            return 0.75 - ((mape - 20) / 100.0);
        if (mape < 50)
            return 0.65 - ((mape - 30) / 200.0);
        return 0.50;
    }

    /**
     * Calcula el coeficiente de determinación R² (R-squared)
     * Mide qué tan bien el modelo explica la variabilidad de los datos
     * 
     * R² = 1 - (SS_res / SS_tot)
     * donde:
     * - SS_res: suma de cuadrados de residuos
     * - SS_tot: suma total de cuadrados
     * 
     * Valores: 0 (mal) a 1 (perfecto)
     */
    private double calcularR2(double[] actual, double[] prediccion) {
        int minLength = Math.min(actual.length, prediccion.length);

        if (minLength == 0)
            return 0.0;

        // Calcular media de valores reales
        double media = Arrays.stream(actual).limit(minLength).average().orElse(0.0);

        // SS_res: Suma de cuadrados de residuos
        double ssRes = 0.0;
        for (int i = 0; i < minLength; i++) {
            ssRes += Math.pow(actual[i] - prediccion[i], 2);
        }

        // SS_tot: Suma total de cuadrados
        double ssTot = 0.0;
        for (int i = 0; i < minLength; i++) {
            ssTot += Math.pow(actual[i] - media, 2);
        }

        log.debug("[METRICAS] Cálculo R²: n={}, media={}, SS_res={}, SS_tot={}",
                minLength,
                String.format("%.2f", media),
                String.format("%.2f", ssRes),
                String.format("%.2f", ssTot));

        // Evitar división por cero
        if (ssTot == 0.0) {
            log.warn("[METRICAS] Advertencia: SS_tot = 0, todos los valores son idénticos a la media");
            return 0.0;
        }

        // R² = 1 - (SS_res / SS_tot)
        double r2 = 1.0 - (ssRes / ssTot);

        log.debug("[METRICAS] R² sin limitar: {}", String.format("%.4f", r2));

        // R² puede ser negativo si el modelo es peor que simplemente predecir la media
        // En ese caso, retornar 0
        return Math.max(0.0, Math.min(1.0, r2));
    }

    private double calcularCorrelacion(double[] x, double[] y) {
        if (x.length != y.length || x.length == 0)
            return 0.0;

        double mediaX = Arrays.stream(x).average().orElse(0.0);
        double mediaY = Arrays.stream(y).average().orElse(0.0);

        double numerador = 0.0;
        double sumaCuadradosX = 0.0;
        double sumaCuadradosY = 0.0;

        for (int i = 0; i < x.length; i++) {
            double diffX = x[i] - mediaX;
            double diffY = y[i] - mediaY;

            numerador += diffX * diffY;
            sumaCuadradosX += diffX * diffX;
            sumaCuadradosY += diffY * diffY;
        }

        double denominador = Math.sqrt(sumaCuadradosX * sumaCuadradosY);
        return denominador > 0 ? numerador / denominador : 0.0;
    }

    /**
     * Clasifica la calidad de la predicción basándose en MAPE
     * MAPE (Mean Absolute Percentage Error) es más intuitivo que R²
     * para operaciones de inventario
     */
    private String clasificarCalidad(double mape) {
        if (mape < 10)
            return "EXCELENTE"; // Error menor a 10%
        if (mape < 20)
            return "BUENA"; // Error entre 10-20%
        if (mape < 30)
            return "REGULAR"; // Error entre 20-30%
        return "POBRE"; // Error mayor a 30%
    }
    
    /**
     * Determina el patrón estacional basado en la intensidad detectada.
     * 
     * @param intensidad Coeficiente de variación de la demanda mensual
     * @return Tipo de patrón: "FUERTE", "MODERADO", "DEBIL" o "SIN_PATRON"
     */
    private String determinarPatronEstacional(Double intensidad) {
        if (intensidad == null || intensidad < 0.1) {
            return "SIN_PATRON";
        } else if (intensidad < 0.2) {
            return "DEBIL";
        } else if (intensidad < 0.35) {
            return "MODERADO";
        } else {
            return "FUERTE";
        }
    }

    private Integer calcularDiasHastaQuiebre(double demandaTotal, Integer horizonte) {
        double demandaDiaria = demandaTotal / horizonte;
        int stockActual = 100; // TODO: Obtener valor real
        return demandaDiaria > 0 ? (int) Math.ceil(stockActual / demandaDiaria) : 365;
    }

    private String evaluarRiesgo(double demandaTotal) {
        int stockActual = 100; // TODO: Obtener valor real
        double ratio = stockActual / Math.max(demandaTotal, 1.0);

        if (ratio < 0.5)
            return "ALTO";
        if (ratio < 1.0)
            return "MEDIO";
        return "BAJO";
    }

    /**
     * Clase interna para análisis de estacionalidad mensual
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class EstacionalidadMensual {
        private Integer mesMayor;
        private Integer mesMenor;
        private Double intensidad;
    }

    /**
     * Implementación de Regresión de Series Temporales con Lag Features.
     * Simula comportamiento de ARIMA usando OLS con características de retraso.
     * Captura dependencia temporal usando valores anteriores (lags).
     * Los parámetros se obtienen dinámicamente desde la BD.
     */
    private List<Double> ejecutarTimeSeriesRegression(double[] serie, int horizonte) {
        log.debug("[PREDICCION] Ejecutando Time Series Regression con lag features y parámetros dinámicos");

        try {
            // Obtener número de lags desde BD (con fallback a 7 para autocorrelación semanal)
            int numLagsConfig = obtenerParametroInt("ARIMA", "num_lags", 7);
            int numLags = Math.min(numLagsConfig, serie.length / 4);

            log.debug("[PARAMETROS ARIMA] numLags configurado={}, efectivo={}", numLagsConfig, numLags);

            int numFilas = serie.length - numLags;
            if (numFilas < 10) {
                log.warn("[PREDICCION] Advertencia: Datos insuficientes para lag features, usando promedio móvil");
                double promedio = Arrays.stream(Arrays.copyOfRange(serie, Math.max(0, serie.length - 14), serie.length))
                        .average().orElse(serie[serie.length - 1]);
                List<Double> fallback = new ArrayList<>();
                for (int i = 0; i < horizonte; i++) {
                    fallback.add(Math.max(0, promedio));
                }
                return fallback;
            }

            // Construir arrays para DataFrame
            double[][] lagsMatrix = new double[numFilas][numLags];
            double[] target = new double[numFilas];

            for (int i = 0; i < numFilas; i++) {
                for (int lag = 0; lag < numLags; lag++) {
                    lagsMatrix[i][lag] = serie[i + lag];
                }
                target[i] = serie[i + numLags];
            }

            // Crear DataFrame con lag features
            List<DoubleVector> columns = new ArrayList<>();
            for (int lag = 0; lag < numLags; lag++) {
                double[] lagColumn = new double[numFilas];
                for (int i = 0; i < numFilas; i++) {
                    lagColumn[i] = lagsMatrix[i][lag];
                }
                columns.add(DoubleVector.of("lag" + (lag + 1), lagColumn));
            }
            columns.add(DoubleVector.of("demanda", target));

            DataFrame dataFrame = DataFrame.of(columns.toArray(new DoubleVector[0]));
            Formula formula = Formula.lhs("demanda");

            // Entrenar modelo OLS con lag features
            LinearModel modelo = OLS.fit(formula, dataFrame);

            // Generar predicciones iterativas
            List<Double> predicciones = new ArrayList<>();
            double[] bufferLags = Arrays.copyOfRange(serie, serie.length - numLags, serie.length);

            for (int i = 0; i < horizonte; i++) {
                // Crear punto de predicción con lags actuales
                double[] lagValues = new double[numLags];
                System.arraycopy(bufferLags, bufferLags.length - numLags, lagValues, 0, numLags);

                List<DoubleVector> predColumns = new ArrayList<>();
                for (int lag = 0; lag < numLags; lag++) {
                    predColumns.add(DoubleVector.of("lag" + (lag + 1), new double[] { lagValues[lag] }));
                }

                DataFrame punto = DataFrame.of(predColumns.toArray(new DoubleVector[0]));
                double prediccion = Math.max(0, modelo.predict(punto.get(0)));
                predicciones.add(prediccion);

                // Actualizar buffer de lags (shift y agregar nueva predicción)
                double[] nuevoBuffer = new double[bufferLags.length];
                System.arraycopy(bufferLags, 1, nuevoBuffer, 0, bufferLags.length - 1);
                nuevoBuffer[bufferLags.length - 1] = prediccion;
                bufferLags = nuevoBuffer;
            }

            log.debug("[PREDICCION] Time Series Regression completado: {} predicciones con {} lags",
                    predicciones.size(), numLags);
            return predicciones;

        } catch (Exception e) {
            log.error("[PREDICCION] Error en Time Series Regression: {}, usando fallback", e.getMessage());
            double promedio = Arrays.stream(Arrays.copyOfRange(serie, Math.max(0, serie.length - 14), serie.length))
                    .average().orElse(serie[serie.length - 1]);

            List<Double> fallback = new ArrayList<>();
            for (int i = 0; i < horizonte; i++) {
                fallback.add(Math.max(0, promedio));
            }
            return fallback;
        }
    }

    /**
     * Predicción de validación con Time Series Regression
     */
    private double[] ejecutarPrediccionValidacionTimeSeries(double[] trainSerie, int validationSize) {
        try {
            int numLags = Math.min(7, trainSerie.length / 4);
            int numFilas = trainSerie.length - numLags;

            if (numFilas < 10) {
                return new double[validationSize];
            }

            // Construir DataFrame con lags
            double[][] lagsMatrix = new double[numFilas][numLags];
            double[] target = new double[numFilas];

            for (int i = 0; i < numFilas; i++) {
                for (int lag = 0; lag < numLags; lag++) {
                    lagsMatrix[i][lag] = trainSerie[i + lag];
                }
                target[i] = trainSerie[i + numLags];
            }

            List<DoubleVector> columns = new ArrayList<>();
            for (int lag = 0; lag < numLags; lag++) {
                double[] lagColumn = new double[numFilas];
                for (int i = 0; i < numFilas; i++) {
                    lagColumn[i] = lagsMatrix[i][lag];
                }
                columns.add(DoubleVector.of("lag" + (lag + 1), lagColumn));
            }
            columns.add(DoubleVector.of("demanda", target));

            DataFrame dataFrame = DataFrame.of(columns.toArray(new DoubleVector[0]));
            Formula formula = Formula.lhs("demanda");
            LinearModel modelo = OLS.fit(formula, dataFrame);

            // Predicciones iterativas
            double[] predicciones = new double[validationSize];
            double[] bufferLags = Arrays.copyOfRange(trainSerie, trainSerie.length - numLags, trainSerie.length);

            for (int i = 0; i < validationSize; i++) {
                double[] lagValues = new double[numLags];
                System.arraycopy(bufferLags, bufferLags.length - numLags, lagValues, 0, numLags);

                List<DoubleVector> predColumns = new ArrayList<>();
                for (int lag = 0; lag < numLags; lag++) {
                    predColumns.add(DoubleVector.of("lag" + (lag + 1), new double[] { lagValues[lag] }));
                }

                DataFrame punto = DataFrame.of(predColumns.toArray(new DoubleVector[0]));
                predicciones[i] = Math.max(0, modelo.predict(punto.get(0)));

                // Shift buffer
                double[] nuevoBuffer = new double[bufferLags.length];
                System.arraycopy(bufferLags, 1, nuevoBuffer, 0, bufferLags.length - 1);
                nuevoBuffer[bufferLags.length - 1] = predicciones[i];
                bufferLags = nuevoBuffer;
            }

            return predicciones;

        } catch (Exception e) {
            log.error("[PREDICCION] Error en predicción validación Time Series: {}", e.getMessage());
            return new double[validationSize];
        }
    }

    /**
     * Clase interna para resultados intermedios de ML
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class ResultadoMLInterno {
        private String algoritmo;
        private List<Double> predicciones;
        private double rmse;
        private double mae;
        private double mape;
        private double confianza;
    }

    /**
     * Persiste la predicción generada para integrarla con optimización y órdenes.
     * Guarda algoritmo, horizonte, métricas y usuario asociado cuando está
     * disponible.
     * 
     * ESTRATEGIA DE REEMPLAZO:
     * - Busca predicciones existentes del mismo producto
     * - Si existe alguna, actualiza la más reciente
     * - Si no existe, crea una nueva
     * 
     * Esto evita acumulación infinita de predicciones del mismo producto.
     */
    private Prediccion persistirPrediccion(Producto producto,
            SmartPrediccionRequest request,
            ResultadoMLInterno resultado,
            SmartPrediccionResponse response) {

        String algoritmoUsado = response.getAlgoritmoUtilizado();
        Integer horizonteTiempo = response.getHorizonteTiempo();

        // Buscar predicción existente con la misma configuración
        Optional<Prediccion> prediccionExistente = prediccionRepositorio
                .findByProductoAndAlgoritmoUsadoAndHorizonteTiempo(producto, algoritmoUsado, horizonteTiempo);

        Prediccion entidad;
        if (prediccionExistente.isPresent()) {
            // REUTILIZAR entidad existente (actualización)
            entidad = prediccionExistente.get();
            log.info(
                    "[PREDICCION] Actualizando predicción existente ID {} para producto {} (Algoritmo: {}, Horizonte: {})",
                    entidad.getPrediccionId(), producto.getProductoId(), algoritmoUsado, horizonteTiempo);
        } else {
            // CREAR nueva entidad
            entidad = new Prediccion();
            entidad.setProducto(producto);
            entidad.setAlgoritmoUsado(algoritmoUsado);
            entidad.setHorizonteTiempo(horizonteTiempo);
            log.info("[PREDICCION] Creando nueva predicción para producto {} (Algoritmo: {}, Horizonte: {})",
                    producto.getProductoId(), algoritmoUsado, horizonteTiempo);
        }

        // Actualizar campos con nuevos valores
        entidad.setFechaEjecucion(response.getFechaEjecucion());

        int demandaTotal = (int) Math.round(
                resultado.getPredicciones().stream().mapToDouble(Double::doubleValue).sum());
        entidad.setDemandaPredichaTotal(demandaTotal);

        if (!Double.isNaN(resultado.getMape()) && resultado.getMape() > 0) {
            entidad.setMetricasError(BigDecimal.valueOf(resultado.getMape()));
        } else if (!Double.isNaN(resultado.getMae()) && resultado.getMae() > 0) {
            entidad.setMetricasError(BigDecimal.valueOf(resultado.getMae()));
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario != null) {
            entidad.setUsuario(usuario);
        }

        Prediccion prediccionGuardada = prediccionRepositorio.save(entidad);

        // Limpieza automática: mantener solo las últimas N predicciones
        limpiarPrediccionesAntiguas(producto, algoritmoUsado, horizonteTiempo);

        return prediccionGuardada;
    }

    /**
     * Limpia predicciones antiguas manteniendo solo las últimas N por
     * configuración.
     * Estrategia intermedia: conserva historial reciente pero evita acumulación
     * infinita.
     * 
     * @param producto  el producto
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

    /**
     * Obtiene el usuario autenticado si el contexto de seguridad lo provee.
     */
    private Usuario obtenerUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String email = authentication.getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.debug("No se pudo resolver usuario autenticado: {}", e.getMessage());
            return null;
        }
    }
}