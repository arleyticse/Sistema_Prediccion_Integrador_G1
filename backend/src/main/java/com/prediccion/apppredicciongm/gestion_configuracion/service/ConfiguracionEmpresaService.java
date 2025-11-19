package com.prediccion.apppredicciongm.gestion_configuracion.service;

import com.prediccion.apppredicciongm.gestion_configuracion.repository.IConfiguracionEmpresaRepositorio;
import com.prediccion.apppredicciongm.models.ConfiguracionEmpresa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Servicio para gestionar la configuración de la empresa.
 * 
 * Implementa el patrón Singleton: solo permite un registro (id = 1).
 * Gestiona la información básica de la empresa y su logo en Base64.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionEmpresaService {

    private final IConfiguracionEmpresaRepositorio repositorio;

    /**
     * Obtiene la configuración de la empresa.
     * Si no existe, crea una configuración por defecto.
     * 
     * @return configuración de la empresa
     */
    @Transactional(readOnly = true)
    public ConfiguracionEmpresa obtenerConfiguracion() {
        return repositorio.obtenerConfiguracion()
                .orElseGet(this::crearConfiguracionPorDefecto);
    }

    /**
     * Obtiene la configuración sin crear una por defecto si no existe.
     * 
     * @return Optional con la configuración
     */
    @Transactional(readOnly = true)
    public Optional<ConfiguracionEmpresa> obtenerConfiguracionOpcional() {
        return repositorio.obtenerConfiguracion();
    }

    /**
     * Actualiza la configuración de la empresa.
     * Si no existe, la crea.
     * 
     * @param configuracion datos a actualizar
     * @return configuración guardada
     */
    @Transactional
    public ConfiguracionEmpresa guardarConfiguracion(ConfiguracionEmpresa configuracion) {
        log.info("Guardando configuración de empresa: {}", configuracion.getNombreEmpresa());
        
        // Asegurar que el ID siempre sea 1 (singleton)
        configuracion.setId(1);
        
        // Si existe una configuración previa, preservar fechaCreacion
        Optional<ConfiguracionEmpresa> existente = repositorio.obtenerConfiguracion();
        if (existente.isPresent()) {
            configuracion.setFechaCreacion(existente.get().getFechaCreacion());
        } else {
            configuracion.setFechaCreacion(LocalDateTime.now());
        }
        
        configuracion.setFechaModificacion(LocalDateTime.now());
        
        return repositorio.save(configuracion);
    }

    /**
     * Actualiza solo el logo de la empresa.
     * 
     * @param logoBase64 logo en formato Base64
     * @param mimeType tipo MIME (image/png, image/jpeg, image/webp)
     * @return configuración actualizada
     */
    @Transactional
    public ConfiguracionEmpresa actualizarLogo(String logoBase64, String mimeType) {
        log.info("Actualizando logo de la empresa (tipo: {})", mimeType);
        
        ConfiguracionEmpresa config = obtenerConfiguracion();
        config.setLogoBase64(logoBase64);
        config.setLogoMimeType(mimeType);
        config.setFechaModificacion(LocalDateTime.now());
        
        return repositorio.save(config);
    }

    /**
     * Elimina el logo de la empresa.
     * 
     * @return configuración actualizada sin logo
     */
    @Transactional
    public ConfiguracionEmpresa eliminarLogo() {
        log.info("Eliminando logo de la empresa");
        
        ConfiguracionEmpresa config = obtenerConfiguracion();
        config.setLogoBase64(null);
        config.setLogoMimeType(null);
        config.setFechaModificacion(LocalDateTime.now());
        
        return repositorio.save(config);
    }

    /**
     * Valida que el tamaño del logo no exceda el límite.
     * 
     * @param logoBase64 logo en Base64
     * @return true si el tamaño es válido
     */
    public boolean validarTamanoLogo(String logoBase64) {
        if (logoBase64 == null || logoBase64.isEmpty()) {
            return true;
        }
        
        // 150,000 caracteres = ~100KB
        int MAX_SIZE = 150000;
        boolean valido = logoBase64.length() <= MAX_SIZE;
        
        if (!valido) {
            log.warn("Logo excede el tamaño máximo: {} caracteres (máx: {})", 
                    logoBase64.length(), MAX_SIZE);
        }
        
        return valido;
    }

    /**
     * Valida el tipo MIME del logo.
     * 
     * @param mimeType tipo MIME a validar
     * @return true si el tipo es válido
     */
    public boolean validarMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        
        String[] tiposPermitidos = {"image/png", "image/jpeg", "image/jpg", "image/webp"};
        for (String tipo : tiposPermitidos) {
            if (tipo.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        
        log.warn("Tipo MIME no permitido: {}", mimeType);
        return false;
    }

    /**
     * Extrae el prefijo del Data URL si existe.
     * Ejemplo: "data:image/png;base64," → ""
     * 
     * @param dataUrl cadena que puede contener prefijo Data URL
     * @return solo la parte Base64 sin prefijo
     */
    public String extraerBase64SinPrefijo(String dataUrl) {
        if (dataUrl == null || dataUrl.isEmpty()) {
            return dataUrl;
        }
        
        // Si contiene el prefijo data:image/...;base64,
        if (dataUrl.startsWith("data:")) {
            int inicio = dataUrl.indexOf(",");
            if (inicio > 0 && inicio < dataUrl.length() - 1) {
                return dataUrl.substring(inicio + 1);
            }
        }
        
        return dataUrl;
    }

    /**
     * Extrae el tipo MIME del Data URL si existe.
     * Ejemplo: "data:image/png;base64,..." → "image/png"
     * 
     * @param dataUrl cadena con prefijo Data URL
     * @return tipo MIME o null si no se encuentra
     */
    public String extraerMimeTypeDeDataUrl(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            return null;
        }
        
        int inicio = dataUrl.indexOf("data:") + 5;
        int fin = dataUrl.indexOf(";");
        
        if (inicio > 5 && fin > inicio) {
            return dataUrl.substring(inicio, fin);
        }
        
        return null;
    }

    /**
     * Crea una configuración por defecto si no existe.
     * 
     * @return configuración por defecto
     */
    @Transactional
    private ConfiguracionEmpresa crearConfiguracionPorDefecto() {
        log.info("Creando configuración por defecto de la empresa");
        
        ConfiguracionEmpresa config = ConfiguracionEmpresa.builder()
                .id(1)
                .nombreEmpresa("Mi Empresa")
                .fechaCreacion(LocalDateTime.now())
                .fechaModificacion(LocalDateTime.now())
                .build();
        
        return repositorio.save(config);
    }
}
