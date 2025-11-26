package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.config;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.ISmartPredictorService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.SmartPredictorServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración para el módulo de predicción inteligente con Machine Learning.
 * 
 * Configura los beans necesarios para el funcionamiento del sistema de
 * predicción
 * usando Smile ML v3.1.1 y permite habilitar/deshabilitar la funcionalidad.
 */
@Slf4j
@Configuration
public class SmartPrediccionConfig {

    /**
     * Bean principal del servicio de predicción inteligente.
     * 
     * Se puede desactivar mediante la propiedad:
     * app.prediccion.smart.enabled=false
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.prediccion.smart.enabled", havingValue = "true", matchIfMissing = true)
    public ISmartPredictorService smartPredictorService(
            SmartPredictorServiceImpl smartPredictorServiceImpl) {
        log.info("[PREDICCION] Inicializando servicio de predicción inteligente con Smile ML v3.1.1");

        log.info("[PREDICCION] Servicio de predicción inteligente configurado correctamente");
        log.info("[PREDICCION] Algoritmos disponibles: {}",
                smartPredictorServiceImpl.obtenerAlgoritmosDisponibles().size());

        return smartPredictorServiceImpl;
    }

    /**
     * Bean de configuración para propiedades del módulo.
     */
    @Bean
    @ConfigurationProperties(prefix = "app.prediccion.smart")
    public SmartPrediccionProperties smartPrediccionProperties() {
        return new SmartPrediccionProperties();
    }

    /**
     * Clase de propiedades para configuración del módulo.
     */
    public static class SmartPrediccionProperties {

        private boolean enabled = true;
        private int minimoRegistrosHistoricos = 10;
        private int horizonteMaximo = 90;
        private int horizonteDefecto = 30;
        private double confianzaMinima = 0.70;
        private String algoritmoDefecto = "AUTO";

        // Getters y Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMinimoRegistrosHistoricos() {
            return minimoRegistrosHistoricos;
        }

        public void setMinimoRegistrosHistoricos(int minimoRegistrosHistoricos) {
            this.minimoRegistrosHistoricos = minimoRegistrosHistoricos;
        }

        public int getHorizonteMaximo() {
            return horizonteMaximo;
        }

        public void setHorizonteMaximo(int horizonteMaximo) {
            this.horizonteMaximo = horizonteMaximo;
        }

        public int getHorizonteDefecto() {
            return horizonteDefecto;
        }

        public void setHorizonteDefecto(int horizonteDefecto) {
            this.horizonteDefecto = horizonteDefecto;
        }

        public double getConfianzaMinima() {
            return confianzaMinima;
        }

        public void setConfianzaMinima(double confianzaMinima) {
            this.confianzaMinima = confianzaMinima;
        }

        public String getAlgoritmoDefecto() {
            return algoritmoDefecto;
        }

        public void setAlgoritmoDefecto(String algoritmoDefecto) {
            this.algoritmoDefecto = algoritmoDefecto;
        }
    }
}