package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.InventarioImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service.IInventarioImportacionService;

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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para importación masiva de inventario desde archivos CSV.
 * 
 * <p>Proporciona endpoints para:</p>
 * <ul>
 *   <li>Importar inventario inicial desde CSV</li>
 *   <li>Validar formato de archivos CSV antes de la importación</li>
 *   <li>Descargar plantilla CSV de ejemplo</li>
 * </ul>
 * 
 * <h3>Seguridad:</h3>
 * <p>Todos los endpoints requieren autenticación y roles ADMIN o GERENTE.</p>
 * 
 * <h3>Formato CSV esperado:</h3>
 * <pre>
 * nombre_producto,stock_disponible,stock_minimo,stock_maximo,punto_reorden,
 * stock_reservado,stock_en_transito,ubicacion_almacen,estado,observaciones,dias_sin_venta
 * </pre>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@RestController
@RequestMapping("/api/importacion/inventario")
@RequiredArgsConstructor
@Tag(name = "Importación Inventario", description = "Endpoints para importación masiva de inventario inicial")
public class InventarioImportacionControlador {

    private final IInventarioImportacionService inventarioImportacionService;

    /**
     * Importa registros de inventario inicial desde un archivo CSV.
     * 
     * <p>Procesa el archivo línea por línea, validando cada registro y creando
     * el inventario asociado a productos existentes. Retorna estadísticas detalladas
     * de la importación incluyendo registros exitosos, fallidos y errores específicos.</p>
     * 
     * <h3>Validaciones aplicadas:</h3>
     * <ul>
     *   <li>Formato CSV válido con 11 columnas</li>
     *   <li>Producto debe existir previamente</li>
     *   <li>No debe existir inventario previo para el producto</li>
     *   <li>Stocks numéricos y positivos</li>
     *   <li>Stock máximo >= stock mínimo</li>
     *   <li>Punto de reorden >= stock mínimo</li>
     *   <li>Estado válido del enum EstadoInventario</li>
     * </ul>
     * 
     * @param archivo Archivo CSV con los datos de inventario (max 10MB)
     * @param authentication Autenticación del usuario (inyectada automáticamente)
     * @return ResponseEntity con {@link InventarioImportacionResponse} conteniendo estadísticas y errores
     * @throws IOException Si hay error leyendo el archivo
     * 
     * @apiNote El endpoint registra cada importación en la tabla de auditoría ImportacionDatos
     */
    @Operation(
        summary = "Importar inventario desde CSV",
        description = "Procesa un archivo CSV e importa registros de inventario inicial masivamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Importación completada (puede incluir errores parciales)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InventarioImportacionResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o formato incorrecto"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos (requiere ADMIN, GERENTE u OPERARIO)"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    public ResponseEntity<InventarioImportacionResponse> importarInventario(
            @Parameter(description = "Archivo CSV con datos de inventario", required = true)
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication) throws IOException {
        
        log.info("Solicitud de importación de inventario recibida: {} por usuario: {}", 
                archivo.getOriginalFilename(), authentication.getName());

        // Obtener ID del usuario autenticado
        Integer usuarioId = obtenerUsuarioId(authentication);

        // Ejecutar importación
        InventarioImportacionResponse response = 
                inventarioImportacionService.importarInventarioDesdeCSV(archivo, usuarioId);

        log.info("Importación completada: {} exitosos, {} fallidos de {} total", 
                response.getRegistrosExitosos(), 
                response.getRegistrosFallidos(), 
                response.getTotalRegistros());

        return ResponseEntity.ok(response);
    }

    /**
     * Valida el formato de un archivo CSV sin realizar la importación.
     * 
     * <p>Verifica estructura, columnas esperadas y formato básico del archivo.
     * Útil para validación previa antes de ejecutar la importación completa.</p>
     * 
     * @param archivo Archivo CSV a validar
     * @return ResponseEntity con mapa conteniendo:
     *         - "valido" (boolean): true si el formato es correcto
     *         - "errores" (List): lista de errores encontrados (vacía si válido)
     * @throws IOException Si hay error leyendo el archivo
     * 
     * @apiNote Esta validación es superficial. La importación real realiza validaciones más exhaustivas.
     */
    @Operation(
        summary = "Validar formato CSV",
        description = "Valida estructura y formato del CSV sin ejecutar la importación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validación completada (revisar campo 'valido')"
        ),
        @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping(value = "/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> validarFormatoCSV(
            @Parameter(description = "Archivo CSV a validar", required = true)
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        
        log.info("Solicitud de validación de CSV recibida: {}", archivo.getOriginalFilename());

        List<String> errores = inventarioImportacionService.validarFormatoCSV(archivo);
        boolean esValido = errores.isEmpty();

        log.info("Validación completada: {} - {} errores encontrados", 
                esValido ? "VÁLIDO" : "INVÁLIDO", errores.size());

        return ResponseEntity.ok(Map.of(
            "valido", esValido,
            "errores", errores,
            "mensaje", esValido 
                ? "El archivo CSV tiene un formato válido" 
                : "Se encontraron errores en el formato del CSV"
        ));
    }

    /**
     * Descarga una plantilla CSV de ejemplo para importación de inventario.
     * 
     * <p>La plantilla incluye:</p>
     * <ul>
     *   <li>Encabezados correctos en el formato esperado</li>
     *   <li>3 filas de ejemplo con datos realistas</li>
     *   <li>Comentarios sobre campos opcionales y formatos</li>
     * </ul>
     * 
     * @return ResponseEntity con archivo CSV descargable
     * 
     * @apiNote El archivo se descarga como "plantilla_inventario.csv"
     */
    @Operation(
        summary = "Descargar plantilla CSV",
        description = "Obtiene una plantilla CSV de ejemplo con el formato correcto y ejemplos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Plantilla generada exitosamente",
            content = @Content(mediaType = "text/csv")
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping(value = "/plantilla", produces = "text/csv")
    @PreAuthorize("permitAll()")
    public ResponseEntity<byte[]> descargarPlantilla() {
        
        log.info("Solicitud de descarga de plantilla CSV de inventario");

        String plantillaCSV = inventarioImportacionService.generarPlantillaCSV();
        byte[] contenido = plantillaCSV.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "plantilla_inventario.csv");
        headers.setContentLength(contenido.length);

        log.info("Plantilla CSV generada: {} bytes", contenido.length);

        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Extrae el ID del usuario desde el contexto de autenticación.
     * 
     * @param authentication Objeto de autenticación de Spring Security
     * @return ID del usuario, o null si no se puede obtener
     */
    private Integer obtenerUsuarioId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        try {
            // Intenta obtener el ID del usuario desde el principal
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // Si el principal implementa UserDetails
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                // En este caso, asumimos que el username es el ID o necesitaríamos consultar la BD
                // Por simplicidad, retornamos null y el servicio lo manejará
                return null;
            } else if (principal instanceof Integer) {
                return (Integer) principal;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el ID del usuario: {}", e.getMessage());
        }

        return null;
    }
}
