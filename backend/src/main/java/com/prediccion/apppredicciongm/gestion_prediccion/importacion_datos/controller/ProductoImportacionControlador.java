package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProductoImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service.IProductoImportacionService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para importación de productos desde archivos CSV
 */
@Slf4j
@RestController
@RequestMapping("/api/importacion/productos")
@RequiredArgsConstructor
@Tag(name = "Importación de Productos", description = "Endpoints para importación masiva de productos desde CSV")
public class ProductoImportacionControlador {

    private final IProductoImportacionService productoImportacionService;

    /**
     * Importa productos desde un archivo CSV
     * 
     * @param archivo Archivo CSV con los datos de productos
     * @param usuarioId ID del usuario que realiza la importación
     * @return Resultado de la importación
     */
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERARIO')")
    @Operation(summary = "Importar productos desde CSV", 
               description = "Permite importar múltiples productos desde un archivo CSV")
    public ResponseEntity<ProductoImportacionResponse> importarProductos(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "usuarioId", required = false) Integer usuarioId) {
        
        try {
            log.info("Solicitud de importación recibida - Archivo: {}, Usuario: {}", 
                    archivo.getOriginalFilename(), usuarioId);

            ProductoImportacionResponse response = 
                    productoImportacionService.importarProductosDesdeCSV(archivo, usuarioId);

            log.info("Importación completada - Exitosos: {}, Fallidos: {}", 
                    response.getRegistrosExitosos(), response.getRegistrosFallidos());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación en la importación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error de I/O durante la importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado durante la importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Valida el formato de un archivo CSV sin procesarlo
     * 
     * @param archivo Archivo CSV a validar
     * @return Lista de errores encontrados
     */
    @PostMapping(value = "/validar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Validar formato CSV", 
               description = "Valida que el archivo CSV tenga el formato correcto antes de importar")
    public ResponseEntity<Map<String, Object>> validarFormatoCSV(@RequestParam("archivo") MultipartFile archivo) {
        try {
            log.info("Solicitud de validación de CSV productos recibida: {}", archivo.getOriginalFilename());
            
            List<String> errores = productoImportacionService.validarFormatoCSV(archivo);
            boolean esValido = errores.isEmpty();
            
            log.info("Validación completada: {} - {} errores encontrados", 
                    esValido ? "VÁLIDO" : "INVÁLIDO", errores.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("valido", esValido);
            response.put("archivo", archivo.getOriginalFilename());
            response.put("errores", errores);
            response.put("mensaje", esValido 
                ? "El archivo CSV es válido y está listo para importar" 
                : "Se encontraron errores en el archivo CSV");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error validando archivo CSV: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valido", false);
            errorResponse.put("errores", List.of("Error procesando el archivo: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Descarga una plantilla CSV de ejemplo para importación
     * 
     * @return Archivo CSV de plantilla
     */
    @GetMapping(value = "/plantilla", produces = "text/csv")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Descargar plantilla CSV", 
               description = "Descarga un archivo CSV de plantilla con el formato correcto para importación")
    public ResponseEntity<String> descargarPlantilla() {
        try {
            String plantilla = productoImportacionService.generarPlantillaCSV();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=plantilla_productos.csv");
            headers.setContentType(MediaType.parseMediaType("text/csv"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(plantilla);
        } catch (Exception e) {
            log.error("Error generando plantilla CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
