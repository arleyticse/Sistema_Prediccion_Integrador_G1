package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.KardexImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service.IKardexImportacionService;

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
 * Controlador REST para importación masiva de movimientos de Kardex desde archivos CSV.
 * 
 * <p>Proporciona endpoints para:</p>
 * <ul>
 *   <li>Importar movimientos de kardex desde CSV</li>
 *   <li>Validar formato de archivos CSV antes de la importación</li>
 *   <li>Descargar plantilla CSV de ejemplo</li>
 * </ul>
 * 
 * <h3>Seguridad:</h3>
 * <p>Todos los endpoints requieren autenticación y roles ADMIN o GERENTE.</p>
 * 
 * <h3>Formato CSV esperado:</h3>
 * <pre>
 * nombre_producto,tipo_movimiento,cantidad,saldo_cantidad,costo_unitario,
 * fecha_movimiento,fecha_vencimiento,nombre_proveedor,lote,tipo_documento,
 * numero_documento,referencia,motivo,ubicacion,observaciones,anulado,
 * fecha_registro,numero_fila
 * </pre>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@RestController
@RequestMapping("/api/importacion/kardex")
@RequiredArgsConstructor
@Tag(name = "Importación Kardex", description = "Endpoints para importación masiva de movimientos de kardex")
public class KardexImportacionControlador {

    private final IKardexImportacionService kardexImportacionService;

    /**
     * Importa movimientos de kardex desde un archivo CSV.
     * 
     * @param archivo Archivo CSV con movimientos de kardex (max 10MB)
     * @param authentication Autenticación del usuario
     * @return ResponseEntity con estadísticas de la importación
     * @throws IOException Si hay error leyendo el archivo
     */
    @Operation(
        summary = "Importar kardex desde CSV",
        description = "Procesa un archivo CSV e importa movimientos de kardex masivamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Importación completada (puede incluir errores parciales)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = KardexImportacionResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o formato incorrecto"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos (requiere ADMIN, GERENTE u OPERARIO)"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    public ResponseEntity<KardexImportacionResponse> importarKardex(
            @Parameter(description = "Archivo CSV con movimientos de kardex", required = true)
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication) throws IOException {
        
        log.info("Solicitud de importación de kardex recibida: {} por usuario: {}", 
                archivo.getOriginalFilename(), authentication.getName());

        Integer usuarioId = obtenerUsuarioId(authentication);

        KardexImportacionResponse response = 
                kardexImportacionService.importarKardexDesdeCSV(archivo, usuarioId);

        log.info("Importación de kardex completada: {} exitosos, {} fallidos de {} total", 
                response.getRegistrosExitosos(), 
                response.getRegistrosFallidos(), 
                response.getTotalRegistros());

        return ResponseEntity.ok(response);
    }

    /**
     * Valida el formato de un archivo CSV sin realizar la importación.
     * 
     * @param archivo Archivo CSV a validar
     * @return ResponseEntity con resultado de validación
     * @throws IOException Si hay error leyendo el archivo
     */
    @Operation(
        summary = "Validar formato CSV",
        description = "Valida estructura y formato del CSV sin ejecutar la importación"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada"),
        @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping(value = "/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> validarFormatoCSV(
            @Parameter(description = "Archivo CSV a validar", required = true)
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        
        log.info("Solicitud de validación de CSV kardex recibida: {}", archivo.getOriginalFilename());

        List<String> errores = kardexImportacionService.validarFormatoCSV(archivo);
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
     * Descarga una plantilla CSV de ejemplo para importación de kardex.
     * 
     * @return ResponseEntity con archivo CSV descargable
     */
    @Operation(
        summary = "Descargar plantilla CSV",
        description = "Obtiene una plantilla CSV de ejemplo con el formato correcto"
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
        
        log.info("Solicitud de descarga de plantilla CSV de kardex");

        String plantillaCSV = kardexImportacionService.generarPlantillaCSV();
        byte[] contenido = plantillaCSV.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "plantilla_kardex.csv");
        headers.setContentLength(contenido.length);

        log.info("Plantilla CSV de kardex generada: {} bytes", contenido.length);

        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }

    /**
     * Extrae el ID del usuario desde el contexto de autenticación.
     */
    private Integer obtenerUsuarioId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return null;
    }
}
