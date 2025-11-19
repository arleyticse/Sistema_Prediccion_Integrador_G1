package com.prediccion.apppredicciongm.gestion_configuracion.controller;

import com.prediccion.apppredicciongm.gestion_configuracion.service.ConfiguracionEmpresaService;
import com.prediccion.apppredicciongm.models.ConfiguracionEmpresa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestionar la configuración de la empresa.
 * 
 * Endpoints protegidos por rol GERENTE para modificaciones.
 * Lectura disponible para todos los usuarios autenticados.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@RestController
@RequestMapping("/api/configuracion/empresa")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuración Empresa", description = "Gestión de información y logo de la empresa")
public class ConfiguracionEmpresaControlador {

    private final ConfiguracionEmpresaService service;

    /**
     * Obtiene la configuración actual de la empresa.
     * 
     * GET /api/configuracion/empresa
     * 
     * @return configuración de la empresa
     */
    @GetMapping
    @Operation(summary = "Obtener configuración de la empresa")
    public ResponseEntity<ConfiguracionEmpresa> obtenerConfiguracion() {
        log.debug("GET /api/configuracion/empresa");
        
        ConfiguracionEmpresa config = service.obtenerConfiguracion();
        return ResponseEntity.ok(config);
    }

    /**
     * Actualiza la configuración de la empresa.
     * Solo accesible por usuarios con rol GERENTE.
     * 
     * PUT /api/configuracion/empresa
     * 
     * @param configuracion datos a actualizar
     * @return configuración actualizada
     */
    @PutMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Actualizar configuración de la empresa (solo GERENTE)")
    public ResponseEntity<ConfiguracionEmpresa> actualizarConfiguracion(
            @Valid @RequestBody ConfiguracionEmpresa configuracion) {
        
        log.info("PUT /api/configuracion/empresa - Usuario intenta actualizar configuración");
        
        // Validar tamaño del logo si se proporciona
        if (configuracion.getLogoBase64() != null && !configuracion.getLogoBase64().isEmpty()) {
            
            // Extraer base64 sin prefijo Data URL si existe
            String logoBase64 = service.extraerBase64SinPrefijo(configuracion.getLogoBase64());
            
            // Validar tamaño
            if (!service.validarTamanoLogo(logoBase64)) {
                log.warn("Logo excede el tamaño máximo permitido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }
            
            // Extraer mimeType si viene en Data URL
            if (configuracion.getLogoMimeType() == null || configuracion.getLogoMimeType().isEmpty()) {
                String mimeType = service.extraerMimeTypeDeDataUrl(configuracion.getLogoBase64());
                configuracion.setLogoMimeType(mimeType);
            }
            
            // Validar tipo MIME
            if (!service.validarMimeType(configuracion.getLogoMimeType())) {
                log.warn("Tipo MIME no permitido: {}", configuracion.getLogoMimeType());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }
            
            configuracion.setLogoBase64(logoBase64);
        }
        
        ConfiguracionEmpresa actualizada = service.guardarConfiguracion(configuracion);
        
        log.info("Configuración actualizada correctamente: {}", actualizada.getNombreEmpresa());
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Actualiza solo el logo de la empresa.
     * Solo accesible por usuarios con rol GERENTE.
     * 
     * PATCH /api/configuracion/empresa/logo
     * 
     * @param request objeto con logoBase64 y logoMimeType
     * @return configuración actualizada
     */
    @PatchMapping("/logo")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Actualizar solo el logo de la empresa (solo GERENTE)")
    public ResponseEntity<?> actualizarLogo(@RequestBody Map<String, String> request) {
        
        log.info("PATCH /api/configuracion/empresa/logo");
        
        String logoBase64 = request.get("logoBase64");
        String mimeType = request.get("logoMimeType");
        
        if (logoBase64 == null || logoBase64.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Logo Base64 es requerido"));
        }
        
        // Extraer base64 sin prefijo
        logoBase64 = service.extraerBase64SinPrefijo(logoBase64);
        
        // Extraer mimeType si no se proporciona
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = service.extraerMimeTypeDeDataUrl(request.get("logoBase64"));
        }
        
        // Validar tamaño
        if (!service.validarTamanoLogo(logoBase64)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El logo excede el tamaño máximo de 100KB"));
        }
        
        // Validar tipo MIME
        if (!service.validarMimeType(mimeType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tipo de imagen no permitido. Use PNG, JPEG o WebP"));
        }
        
        ConfiguracionEmpresa actualizada = service.actualizarLogo(logoBase64, mimeType);
        
        log.info("Logo actualizado correctamente");
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Elimina el logo de la empresa.
     * Solo accesible por usuarios con rol GERENTE.
     * 
     * DELETE /api/configuracion/empresa/logo
     * 
     * @return configuración sin logo
     */
    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Eliminar logo de la empresa (solo GERENTE)")
    public ResponseEntity<ConfiguracionEmpresa> eliminarLogo() {
        
        log.info("DELETE /api/configuracion/empresa/logo");
        
        ConfiguracionEmpresa actualizada = service.eliminarLogo();
        
        log.info("Logo eliminado correctamente");
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Valida si un logo cumple con las restricciones de tamaño y tipo.
     * Útil para validación en frontend antes de enviar.
     * 
     * POST /api/configuracion/empresa/logo/validar
     * 
     * @param request objeto con logoBase64 y logoMimeType
     * @return resultado de validación
     */
    @PostMapping("/logo/validar")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Validar logo antes de guardar (solo GERENTE)")
    public ResponseEntity<Map<String, Object>> validarLogo(@RequestBody Map<String, String> request) {
        
        String logoBase64 = request.get("logoBase64");
        String mimeType = request.get("logoMimeType");
        
        if (logoBase64 == null || logoBase64.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "mensaje", "Logo Base64 es requerido"
            ));
        }
        
        // Extraer base64 sin prefijo
        logoBase64 = service.extraerBase64SinPrefijo(logoBase64);
        
        // Extraer mimeType si no se proporciona
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = service.extraerMimeTypeDeDataUrl(request.get("logoBase64"));
        }
        
        boolean tamanoValido = service.validarTamanoLogo(logoBase64);
        boolean tipoValido = service.validarMimeType(mimeType);
        
        if (!tamanoValido) {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "mensaje", "El logo excede el tamaño máximo de 100KB",
                    "tamanoActual", logoBase64.length()
            ));
        }
        
        if (!tipoValido) {
            return ResponseEntity.ok(Map.of(
                    "valido", false,
                    "mensaje", "Tipo de imagen no permitido. Use PNG, JPEG o WebP"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
                "valido", true,
                "mensaje", "Logo válido",
                "tamano", logoBase64.length()
        ));
    }
}
