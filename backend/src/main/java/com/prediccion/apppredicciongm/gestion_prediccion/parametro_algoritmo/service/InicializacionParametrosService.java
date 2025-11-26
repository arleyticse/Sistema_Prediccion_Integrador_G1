package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.service;

import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.repository.IParametroAlgoritmoRepositorio;
import com.prediccion.apppredicciongm.models.ParametroAlgoritmo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para inicializar parámetros de algoritmos por defecto.
 * Se ejecuta automáticamente al iniciar la aplicación.
 * 
 * Algoritmos soportados (Smile ML v3.1.1):
 * - LINEAR_REGRESSION: Regresión lineal OLS para demanda estable
 * - RANDOM_FOREST: Bosques aleatorios para patrones complejos
 * - GRADIENT_BOOSTING: GBM para alta variabilidad
 * - ARIMA: Series temporales con lag features
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InicializacionParametrosService implements ApplicationRunner {

    private final IParametroAlgoritmoRepositorio parametroRepositorio;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Verificar si necesita migración (algoritmos obsoletos) o inicialización
        boolean necesitaMigracion = parametroRepositorio.findByTipoAlgoritmo("SMA").size() > 0
                || parametroRepositorio.findByTipoAlgoritmo("SES").size() > 0
                || parametroRepositorio.findByTipoAlgoritmo("HOLT_WINTERS").size() > 0;

        if (parametroRepositorio.count() == 0) {
            log.info("[PARAMETROS] Inicializando parámetros de algoritmos por defecto...");
            inicializarParametrosPorDefecto();
        } else if (necesitaMigracion) {
            log.info("[PARAMETROS] Detectados parámetros obsoletos, ejecutando migración...");
            migrarParametrosObsoletos();
        } else {
            log.info("[PARAMETROS] Los parámetros de algoritmos ya están configurados correctamente");
        }
    }

    /**
     * Migra parámetros de algoritmos obsoletos a los nuevos algoritmos Smile ML
     */
    private void migrarParametrosObsoletos() {
        try {
            // Eliminar parámetros de algoritmos obsoletos
            List<String> algoritmosObsoletos = Arrays.asList("SMA", "SES", "HOLT_WINTERS");
            for (String algoritmo : algoritmosObsoletos) {
                List<ParametroAlgoritmo> parametrosObsoletos = parametroRepositorio.findByTipoAlgoritmo(algoritmo);
                if (!parametrosObsoletos.isEmpty()) {
                    parametroRepositorio.deleteAll(parametrosObsoletos);
                    log.info("[PARAMETROS] Eliminados {} parámetros obsoletos de {}", 
                            parametrosObsoletos.size(), algoritmo);
                }
            }

            // Verificar si faltan parámetros de algoritmos nuevos
            if (parametroRepositorio.findByTipoAlgoritmo("LINEAR_REGRESSION").isEmpty()) {
                crearParametrosLinearRegression();
            }
            if (parametroRepositorio.findByTipoAlgoritmo("RANDOM_FOREST").isEmpty()) {
                crearParametrosRandomForest();
            }
            if (parametroRepositorio.findByTipoAlgoritmo("GRADIENT_BOOSTING").isEmpty()) {
                crearParametrosGradientBoosting();
            }
            if (parametroRepositorio.findByTipoAlgoritmo("ARIMA").isEmpty()) {
                crearParametrosARIMA();
            }

            log.info("[PARAMETROS] Migración de parámetros completada exitosamente");

        } catch (Exception e) {
            log.error("[PARAMETROS] Error durante migración de parámetros: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Inicializa todos los parámetros por defecto para cada algoritmo Smile ML
     */
    private void inicializarParametrosPorDefecto() {
        try {
            // Parámetros para LINEAR_REGRESSION (OLS)
            crearParametrosLinearRegression();

            // Parámetros para RANDOM_FOREST
            crearParametrosRandomForest();

            // Parámetros para GRADIENT_BOOSTING
            crearParametrosGradientBoosting();

            // Parámetros para ARIMA (Time Series con Lag Features)
            crearParametrosARIMA();

            log.info("[PARAMETROS] Parámetros de algoritmos Smile ML inicializados correctamente");

        } catch (Exception e) {
            log.error("[PARAMETROS] Error al inicializar parámetros de algoritmos: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea parámetros para LINEAR_REGRESSION (OLS - Ordinary Least Squares).
     * Óptimo para productos con demanda estable: arroz, azúcar, sal, aceite.
     */
    private void crearParametrosLinearRegression() {
        List<ParametroAlgoritmo> parametros = Arrays.asList(
                ParametroAlgoritmo.builder()
                        .nombreParametro("min_datos_historicos")
                        .valorParametro(new BigDecimal("10"))
                        .valorMinimo(new BigDecimal("5"))
                        .valorMaximo(new BigDecimal("50"))
                        .tipoAlgoritmo("LINEAR_REGRESSION")
                        .descripcion("Mínimo de datos históricos requeridos para entrenamiento")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build());

        parametroRepositorio.saveAll(parametros);
        log.debug("[PARAMETROS] Parámetros LINEAR_REGRESSION creados");
    }

    /**
     * Crea parámetros para RANDOM_FOREST.
     * Óptimo para patrones complejos: bebidas, snacks, productos de limpieza.
     */
    private void crearParametrosRandomForest() {
        List<ParametroAlgoritmo> parametros = Arrays.asList(
                ParametroAlgoritmo.builder()
                        .nombreParametro("num_arboles")
                        .valorParametro(new BigDecimal("100"))
                        .valorMinimo(new BigDecimal("50"))
                        .valorMaximo(new BigDecimal("500"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Número de árboles en el bosque")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("max_depth")
                        .valorParametro(new BigDecimal("10"))
                        .valorMinimo(new BigDecimal("3"))
                        .valorMaximo(new BigDecimal("20"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Profundidad máxima de cada árbol")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("max_nodes")
                        .valorParametro(new BigDecimal("100"))
                        .valorMinimo(new BigDecimal("20"))
                        .valorMaximo(new BigDecimal("500"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Número máximo de nodos por árbol")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("node_size")
                        .valorParametro(new BigDecimal("5"))
                        .valorMinimo(new BigDecimal("1"))
                        .valorMaximo(new BigDecimal("20"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Tamaño mínimo de nodo para dividir")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("subsample")
                        .valorParametro(new BigDecimal("1.0"))
                        .valorMinimo(new BigDecimal("0.5"))
                        .valorMaximo(new BigDecimal("1.0"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Proporción de datos para cada árbol (1.0 = todos)")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("min_datos_historicos")
                        .valorParametro(new BigDecimal("30"))
                        .valorMinimo(new BigDecimal("20"))
                        .valorMaximo(new BigDecimal("100"))
                        .tipoAlgoritmo("RANDOM_FOREST")
                        .descripcion("Mínimo de datos históricos requeridos")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build());

        parametroRepositorio.saveAll(parametros);
        log.debug("[PARAMETROS] Parámetros RANDOM_FOREST creados");
    }

    /**
     * Crea parámetros para GRADIENT_BOOSTING (GBM).
     * Óptimo para alta variabilidad: pan, lácteos, frutas, verduras, carnes.
     */
    private void crearParametrosGradientBoosting() {
        List<ParametroAlgoritmo> parametros = Arrays.asList(
                ParametroAlgoritmo.builder()
                        .nombreParametro("num_trees")
                        .valorParametro(new BigDecimal("200"))
                        .valorMinimo(new BigDecimal("100"))
                        .valorMaximo(new BigDecimal("500"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Número de árboles de boosting")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("shrinkage")
                        .valorParametro(new BigDecimal("0.05"))
                        .valorMinimo(new BigDecimal("0.01"))
                        .valorMaximo(new BigDecimal("0.3"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Tasa de aprendizaje (learning rate)")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("max_depth")
                        .valorParametro(new BigDecimal("6"))
                        .valorMinimo(new BigDecimal("3"))
                        .valorMaximo(new BigDecimal("15"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Profundidad máxima de cada árbol")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("max_nodes")
                        .valorParametro(new BigDecimal("50"))
                        .valorMinimo(new BigDecimal("20"))
                        .valorMaximo(new BigDecimal("200"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Número máximo de nodos por árbol")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("node_size")
                        .valorParametro(new BigDecimal("5"))
                        .valorMinimo(new BigDecimal("1"))
                        .valorMaximo(new BigDecimal("20"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Tamaño mínimo de nodo")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("sample_rate")
                        .valorParametro(new BigDecimal("0.7"))
                        .valorMinimo(new BigDecimal("0.5"))
                        .valorMaximo(new BigDecimal("1.0"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Proporción de muestreo por iteración")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("min_datos_historicos")
                        .valorParametro(new BigDecimal("20"))
                        .valorMinimo(new BigDecimal("15"))
                        .valorMaximo(new BigDecimal("100"))
                        .tipoAlgoritmo("GRADIENT_BOOSTING")
                        .descripcion("Mínimo de datos históricos requeridos")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build());

        parametroRepositorio.saveAll(parametros);
        log.debug("[PARAMETROS] Parámetros GRADIENT_BOOSTING creados");
    }

    /**
     * Crea parámetros para ARIMA (Time Series con Lag Features).
     * Especializado para series temporales con dependencia temporal.
     */
    private void crearParametrosARIMA() {
        List<ParametroAlgoritmo> parametrosARIMA = Arrays.asList(
                ParametroAlgoritmo.builder()
                        .nombreParametro("num_lags")
                        .valorParametro(new BigDecimal("7"))
                        .valorMinimo(new BigDecimal("3"))
                        .valorMaximo(new BigDecimal("14"))
                        .tipoAlgoritmo("ARIMA")
                        .descripcion("Número de valores anteriores (lags) para predicción")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build(),

                ParametroAlgoritmo.builder()
                        .nombreParametro("min_datos_historicos")
                        .valorParametro(new BigDecimal("30"))
                        .valorMinimo(new BigDecimal("20"))
                        .valorMaximo(new BigDecimal("100"))
                        .tipoAlgoritmo("ARIMA")
                        .descripcion("Mínimo de datos históricos requeridos")
                        .activo(true)
                        .fechaCreacion(LocalDateTime.now())
                        .fechaActualizacion(LocalDateTime.now())
                        .build());

        parametroRepositorio.saveAll(parametrosARIMA);
        log.debug("[PARAMETROS] Parámetros ARIMA creados");
    }
}