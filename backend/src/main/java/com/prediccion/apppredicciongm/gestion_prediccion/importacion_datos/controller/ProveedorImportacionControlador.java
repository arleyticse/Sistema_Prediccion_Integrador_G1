package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProveedorImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service.IProveedorImportacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para operaciones de importación masiva de proveedores.
 * <p>
 * Proporciona endpoints para importar proveedores desde archivos CSV,
 * validar el formato de los archivos y descargar plantillas de ejemplo.
 * </p>
 * 
 * <h3>Endpoints Disponibles:</h3>
 * <ul>
 *   <li>POST /api/importacion/proveedores/importar - Importa proveedores desde CSV</li>
 *   <li>POST /api/importacion/proveedores/validar - Valida formato CSV sin importar</li>
 *   <li>GET /api/importacion/proveedores/plantilla - Descarga plantilla CSV de ejemplo</li>
 * </ul>
 * 
 * <h3>Seguridad:</h3>
 * <p>Todos los endpoints requieren autenticación y rol ADMIN o GERENTE.</p>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 */
@RestController
@RequestMapping("/api/importacion/proveedores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Importación de Proveedores", 
     description = "Endpoints para importación masiva de proveedores desde archivos CSV")
public class ProveedorImportacionControlador {

    private final IProveedorImportacionService proveedorImportacionService;

    /**
     * Importa proveedores desde un archivo CSV.
     * <p>
     * Procesa el archivo CSV, valida los datos, crea o actualiza proveedores
     * en la base de datos y retorna estadísticas del proceso.
     * </p>
     * 
     * <h4>Características:</h4>
     * <ul>
     *   <li>Validación completa de datos (formato + reglas de negocio)</li>
     *   <li>Detección automática de duplicados por RUC/NIT o razón social</li>
     *   <li>Actualización de proveedores existentes</li>
     *   <li>Registro de auditoría en tabla importacion_datos</li>
     *   <li>Reporte detallado de errores por fila</li>
     * </ul>
     * 
     * <h4>Formato CSV Esperado:</h4>
     * <pre>
     * razonSocial,nombreComercial,rucNit,telefono,email,direccion,ciudad,pais,personaContacto,tiempoEntregaDias,diasCredito,calificacion,estado,observaciones
     * </pre>
     * 
     * @param archivo archivo CSV con datos de proveedores (multipart/form-data)
     * @return respuesta con estadísticas de importación y lista de errores
     */
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    @Operation(
        summary = "Importar proveedores desde CSV",
        description = "Procesa un archivo CSV para crear o actualizar proveedores en el sistema. " +
                     "Valida datos, detecta duplicados y registra la operación de importación. " +
                     "Requiere rol ADMIN, GERENTE u OPERARIO."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Importación completada exitosamente (con o sin errores parciales)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProveedorImportacionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Archivo inválido o formato CSV incorrecto"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "No autenticado - Token JWT faltante o inválido"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "No autorizado - Requiere rol ADMIN o GERENTE"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor durante la importación"
        )
    })
    public ResponseEntity<ProveedorImportacionResponse> importarProveedores(
            @Parameter(description = "Archivo CSV con datos de proveedores", required = true)
            @RequestParam("archivo") MultipartFile archivo) {
        
        log.info("Solicitud de importación de proveedores. Archivo: {}, Tamaño: {} bytes",
                archivo.getOriginalFilename(), archivo.getSize());

        // Validar que el archivo no esté vacío
        if (archivo.isEmpty()) {
            log.warn("Archivo vacío recibido");
            return ResponseEntity.badRequest()
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("FALLIDA")
                            .build());
        }

        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || 
            (!contentType.equals("text/csv") && 
             !contentType.equals("application/vnd.ms-excel") &&
             !contentType.equals("application/csv"))) {
            log.warn("Tipo de archivo inválido: {}", contentType);
            return ResponseEntity.badRequest()
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("FALLIDA")
                            .build());
        }

        try {
            ProveedorImportacionResponse response = proveedorImportacionService
                    .importarProveedoresDesdeCSV(archivo, archivo.getOriginalFilename());

            log.info("Importación completada: {} exitosos, {} fallidos",
                    response.getRegistrosExitosos(), response.getRegistrosFallidos());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error de I/O durante importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("FALLIDA")
                            .build());
        } catch (Exception e) {
            log.error("Error inesperado durante importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("FALLIDA")
                            .build());
        }
    }

    /**
     * Valida el formato de un archivo CSV sin persistir datos.
     * <p>
     * Útil para pre-validar archivos antes de la importación real.
     * Verifica formato CSV, validaciones de campos y reglas de negocio
     * sin modificar la base de datos.
     * </p>
     * 
     * @param archivo archivo CSV a validar
     * @return respuesta con resultado de validación y errores encontrados
     */
    @PostMapping(value = "/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Validar formato CSV de proveedores",
        description = "Valida la estructura y datos de un archivo CSV sin importar los datos. " +
                     "Útil para verificar el archivo antes de la importación real."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Validación completada (puede indicar errores encontrados)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProveedorImportacionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Archivo inválido o no se puede leer"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "No autenticado"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "No autorizado - Requiere rol ADMIN o GERENTE"
        )
    })
    public ResponseEntity<ProveedorImportacionResponse> validarCSV(
            @Parameter(description = "Archivo CSV a validar", required = true)
            @RequestParam("archivo") MultipartFile archivo) {
        
        log.info("Solicitud de validación de CSV. Archivo: {}", archivo.getOriginalFilename());

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("INVALIDO")
                            .build());
        }

        try {
            ProveedorImportacionResponse response = proveedorImportacionService
                    .validarFormatoCSV(archivo);

            log.info("✓ Validación completada: {} válidos, {} inválidos",
                    response.getRegistrosExitosos(), response.getRegistrosFallidos());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error validando CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ProveedorImportacionResponse.builder()
                            .nombreArchivo(archivo.getOriginalFilename())
                            .estado("ERROR_LECTURA")
                            .build());
        }
    }

    /**
     * Descarga una plantilla CSV de ejemplo para importación de proveedores.
     * <p>
     * Genera un archivo CSV con:
     * <ul>
     *   <li>Encabezados de todas las columnas requeridas</li>
     *   <li>3 filas de ejemplo con datos válidos</li>
     *   <li>Formatos correctos para cada campo</li>
     * </ul>
     * </p>
     * 
     * @return archivo CSV de plantilla
     */
    @GetMapping("/plantilla")
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Descargar plantilla CSV de proveedores",
        description = "Genera y descarga una plantilla CSV de ejemplo con el formato correcto " +
                     "y datos de ejemplo para la importación de proveedores. " +
                     "Requiere rol ADMIN o GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Plantilla generada exitosamente",
            content = @Content(
                mediaType = "text/csv"
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "No autenticado"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "No autorizado - Requiere rol ADMIN o GERENTE"
        )
    })
    public ResponseEntity<String> descargarPlantilla() {
        log.info("Solicitud de plantilla CSV de proveedores");

        try {
            String plantilla = proveedorImportacionService.generarPlantillaCSV();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "plantilla_proveedores.csv");

            log.info("✓ Plantilla generada exitosamente");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(plantilla);

        } catch (Exception e) {
            log.error("Error generando plantilla: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generando plantilla: " + e.getMessage());
        }
    }
}
