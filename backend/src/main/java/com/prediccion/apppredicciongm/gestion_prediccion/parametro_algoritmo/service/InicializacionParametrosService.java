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
 * Servicio para inicializar parámetros de algoritmos por defecto
 * Se ejecuta automáticamente al iniciar la aplicación
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InicializacionParametrosService implements ApplicationRunner {

    private final IParametroAlgoritmoRepositorio parametroRepositorio;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (parametroRepositorio.count() == 0) {
            log.info("Inicializando parámetros de algoritmos por defecto...");
            inicializarParametrosPorDefecto();
        } else {
            log.info("Los parámetros de algoritmos ya están inicializados");
        }
    }

    /**
     * Inicializa todos los parámetros por defecto para cada algoritmo
     */
    private void inicializarParametrosPorDefecto() {
        try {
            // Parámetros para SMA (Simple Moving Average)
            crearParametrosSMA();
            
            // Parámetros para SES (Simple Exponential Smoothing)
            crearParametrosSES();
            
            // Parámetros para Holt-Winters
            crearParametrosHoltWinters();
            
            // Parámetros para ARIMA
            crearParametrosARIMA();
            
            log.info("[PARAMETROS] Parámetros de algoritmos inicializados correctamente");
            
        } catch (Exception e) {
            log.error("[PARAMETROS] Error al inicializar parámetros de algoritmos: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea parámetros para algoritmo SMA
     */
    private void crearParametrosSMA() {
        List<ParametroAlgoritmo> parametrosSMA = Arrays.asList(
            ParametroAlgoritmo.builder()
                .nombreParametro("periodos")
                .valorParametro(new BigDecimal("7"))
                .valorMinimo(new BigDecimal("2"))
                .valorMaximo(new BigDecimal("30"))
                .tipoAlgoritmo("SMA")
                .descripcion("Número de períodos para calcular la media móvil")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("min_datos_historicos")
                .valorParametro(new BigDecimal("14"))
                .valorMinimo(new BigDecimal("7"))
                .valorMaximo(new BigDecimal("90"))
                .tipoAlgoritmo("SMA")
                .descripcion("Mínimo de datos históricos requeridos")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build()
        );
        
        parametroRepositorio.saveAll(parametrosSMA);
        log.debug("Parámetros SMA creados");
    }

    /**
     * Crea parámetros para algoritmo SES
     */
    private void crearParametrosSES() {
        List<ParametroAlgoritmo> parametrosSES = Arrays.asList(
            ParametroAlgoritmo.builder()
                .nombreParametro("alpha")
                .valorParametro(new BigDecimal("0.3"))
                .valorMinimo(new BigDecimal("0.1"))
                .valorMaximo(new BigDecimal("0.9"))
                .tipoAlgoritmo("SES")
                .descripcion("Factor de suavizamiento para SES")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("min_datos_historicos")
                .valorParametro(new BigDecimal("10"))
                .valorMinimo(new BigDecimal("5"))
                .valorMaximo(new BigDecimal("60"))
                .tipoAlgoritmo("SES")
                .descripcion("Mínimo de datos históricos requeridos")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build()
        );
        
        parametroRepositorio.saveAll(parametrosSES);
        log.debug("Parámetros SES creados");
    }

    /**
     * Crea parámetros para algoritmo Holt-Winters
     */
    private void crearParametrosHoltWinters() {
        List<ParametroAlgoritmo> parametrosHW = Arrays.asList(
            ParametroAlgoritmo.builder()
                .nombreParametro("alpha")
                .valorParametro(new BigDecimal("0.3"))
                .valorMinimo(new BigDecimal("0.1"))
                .valorMaximo(new BigDecimal("0.9"))
                .tipoAlgoritmo("HOLT_WINTERS")
                .descripcion("Factor de suavizamiento del nivel")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("beta")
                .valorParametro(new BigDecimal("0.2"))
                .valorMinimo(new BigDecimal("0.1"))
                .valorMaximo(new BigDecimal("0.9"))
                .tipoAlgoritmo("HOLT_WINTERS")
                .descripcion("Factor de suavizamiento de la tendencia")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("gamma")
                .valorParametro(new BigDecimal("0.1"))
                .valorMinimo(new BigDecimal("0.05"))
                .valorMaximo(new BigDecimal("0.5"))
                .tipoAlgoritmo("HOLT_WINTERS")
                .descripcion("Factor de suavizamiento de la estacionalidad")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("periodos_estacional")
                .valorParametro(new BigDecimal("12"))
                .valorMinimo(new BigDecimal("4"))
                .valorMaximo(new BigDecimal("24"))
                .tipoAlgoritmo("HOLT_WINTERS")
                .descripcion("Número de períodos por ciclo estacional")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("min_datos_historicos")
                .valorParametro(new BigDecimal("36"))
                .valorMinimo(new BigDecimal("24"))
                .valorMaximo(new BigDecimal("120"))
                .tipoAlgoritmo("HOLT_WINTERS")
                .descripcion("Mínimo de datos históricos (mínimo 2 ciclos estacionales)")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build()
        );
        
        parametroRepositorio.saveAll(parametrosHW);
        log.debug("Parámetros Holt-Winters creados");
    }

    /**
     * Crea parámetros para algoritmo ARIMA
     */
    private void crearParametrosARIMA() {
        List<ParametroAlgoritmo> parametrosARIMA = Arrays.asList(
            ParametroAlgoritmo.builder()
                .nombreParametro("p_order")
                .valorParametro(new BigDecimal("1"))
                .valorMinimo(new BigDecimal("0"))
                .valorMaximo(new BigDecimal("5"))
                .tipoAlgoritmo("ARIMA")
                .descripcion("Orden autoregresivo (p)")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("d_order")
                .valorParametro(new BigDecimal("1"))
                .valorMinimo(new BigDecimal("0"))
                .valorMaximo(new BigDecimal("2"))
                .tipoAlgoritmo("ARIMA")
                .descripcion("Orden de diferenciación (d)")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("q_order")
                .valorParametro(new BigDecimal("1"))
                .valorMinimo(new BigDecimal("0"))
                .valorMaximo(new BigDecimal("5"))
                .tipoAlgoritmo("ARIMA")
                .descripcion("Orden de media móvil (q)")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build(),
                
            ParametroAlgoritmo.builder()
                .nombreParametro("max_iteraciones")
                .valorParametro(new BigDecimal("100"))
                .valorMinimo(new BigDecimal("50"))
                .valorMaximo(new BigDecimal("500"))
                .tipoAlgoritmo("ARIMA")
                .descripcion("Máximo número de iteraciones para convergencia")
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
                .build()
        );
        
        parametroRepositorio.saveAll(parametrosARIMA);
        log.debug("Parámetros ARIMA creados");
    }
}