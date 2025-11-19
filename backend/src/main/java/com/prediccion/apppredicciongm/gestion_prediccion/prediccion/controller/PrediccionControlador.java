package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.controller;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.GenerarPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.PrediccionServiceImpl;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.AnalisisAutomaticoService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.AnalisisAutomaticoService.RecomendacionAlgoritmo;
import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar las predicciones de demanda.
 * 
 * @deprecated Este controlador usa algoritmos básicos (SMA, SES, Holt-Winters).
 *             Usar en su lugar {@link SmartPrediccionController} que implementa
 *             algoritmos avanzados de Machine Learning con Smile (ARIMA, RandomForest, GradientBoosting).
 * 
 * <p>Proporciona endpoints para generar predicciones usando diferentes algoritmos
 * (SMA, SES, Holt-Winters), consultar predicciones existentes y obtener información
 * sobre los algoritmos disponibles.</p>
 * 
 * <p>Todos los endpoints requieren autenticación y roles específicos según la operación.</p>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 */
@Deprecated(since = "2.0", forRemoval = true)
@RestController
@RequestMapping("/api/predicciones")
@Tag(name = "Predicciones (DEPRECATED)", description = "DEPRECADO: Usar /api/v2/predicciones con algoritmos SMILE ML")
public class PrediccionControlador {

    private static final Logger logger = LoggerFactory.getLogger(PrediccionControlador.class);

    @Autowired
    private PrediccionServiceImpl prediccionService;
    
    @Autowired
    private AnalisisAutomaticoService analisisAutomaticoService;
    
    @Autowired
    private IProductoRepositorio productoRepositorio;
    
    @Autowired
    private IKardexRepositorio kardexRepositorio;

    /**
     * Genera una nueva predicción de demanda para un producto.
     * 
     * <p>Este endpoint ejecuta el algoritmo seleccionado con los parámetros especificados
     * para generar una predicción de la demanda futura del producto.</p>
     * 
     * @param request Datos de la solicitud (productoId, algoritmo, horizonteTiempo, parametros)
     * @return La predicción generada con métricas de error y valores predichos
     */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @Operation(summary = "Generar nueva predicción",
               description = "Genera una predicción de demanda usando el algoritmo seleccionado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Predicción generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos o datos insuficientes"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<PrediccionResponse> generarPrediccion(@Valid @RequestBody GenerarPrediccionRequest request) {
        logger.info("Solicitud de generacion de prediccion para producto ID: {}", request.getProductoId());
        logger.debug("Algoritmo: {}, Horizonte: {} dias", request.getAlgoritmo(), request.getHorizonteTiempo());
        
        try {
            // Usar el nuevo método que devuelve PrediccionResponse completo
            PrediccionResponse response = prediccionService.generarPrediccionCompleta(
                request.getProductoId(),
                request.getAlgoritmo(),
                request.getHorizonteTiempo(),
                request.getParametros()
            );
            
            logger.info("Prediccion generada exitosamente con ID: {}", response.getPrediccionId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validacion: {}", e.getMessage());
            // Crear una respuesta con estado FALLIDA
            PrediccionResponse errorResponse = new PrediccionResponse();
            errorResponse.establecerEstado(true); // Marcar como fallida
            errorResponse.getAdvertencias().add("Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IllegalStateException e) {
            logger.error("Datos insuficientes: {}", e.getMessage());
            // Crear una respuesta con estado FALLIDA
            PrediccionResponse errorResponse = new PrediccionResponse();
            errorResponse.establecerEstado(true); // Marcar como fallida
            errorResponse.getAdvertencias().add("Datos insuficientes: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error inesperado al generar prediccion", e);
            // Crear una respuesta con estado FALLIDA
            PrediccionResponse errorResponse = new PrediccionResponse();
            errorResponse.establecerEstado(true); // Marcar como fallida
            errorResponse.getAdvertencias().add("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todas las predicciones con paginación.
     * 
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @return Página de predicciones
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
    @Operation(summary = "Listar todas las predicciones",
               description = "Retorna todas las predicciones generadas con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Predicciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Page<PrediccionResponse>> obtenerTodasLasPredicciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Obteniendo todas las predicciones (page={}, size={})", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Prediccion> prediccionesPage = prediccionService.obtenerTodasLasPredicciones(pageable);
        
        // Reconstruir cada predicción con datos completos
        Page<PrediccionResponse> responsePage = prediccionesPage.map(prediccion -> 
            prediccionService.reconstruirPrediccionCompleta(prediccion)
        );
        
        logger.info("Se obtuvieron {} predicciones de {} total", 
                   responsePage.getNumberOfElements(), responsePage.getTotalElements());
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Obtiene todas las predicciones de un producto específico.
     * 
     * @param productoId ID del producto
     * @param page Número de página
     * @param size Tamaño de página
     * @return Lista de predicciones del producto
     */
    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
    @Operation(summary = "Listar predicciones por producto",
               description = "Retorna todas las predicciones generadas para un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Predicciones del producto obtenidas exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<PrediccionResponse>> obtenerPrediccionesPorProducto(
            @PathVariable Integer productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Obteniendo predicciones para producto ID: {}", productoId);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Prediccion> predicciones = prediccionService.obtenerPrediccionesByProducto(productoId, pageable);
            
            // Reconstruir cada predicción con datos completos
            List<PrediccionResponse> responses = predicciones.stream()
                .map(prediccion -> prediccionService.reconstruirPrediccionCompleta(prediccion))
                .collect(Collectors.toList());
            
            logger.info("Se obtuvieron {} predicciones para producto {}", responses.size(), productoId);
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            logger.error("Producto no encontrado: {}", productoId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene la última predicción generada para un producto.
     * 
     * @param productoId ID del producto
     * @return Última predicción del producto
     */
    @GetMapping("/ultima/{productoId}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtener última predicción de un producto",
               description = "Retorna la predicción más reciente generada para un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Última predicción obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado o sin predicciones"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<PrediccionResponse> obtenerUltimaPrediccion(@PathVariable Integer productoId) {
        logger.debug("Obteniendo ultima prediccion para producto ID: {}", productoId);
        
        try {
            Prediccion prediccion = prediccionService.obtenerUltimaPrediccion(productoId);
            
            if (prediccion == null) {
                logger.warn("No se encontraron predicciones para producto: {}", productoId);
                return ResponseEntity.notFound().build();
            }
            
            // Reconstruir predicción con datos completos
            PrediccionResponse response = prediccionService.reconstruirPrediccionCompleta(prediccion);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Producto no encontrado: {}", productoId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una predicción de la base de datos.
     * 
     * @param prediccionId ID de la predicción a eliminar
     * @return Respuesta sin contenido si fue exitoso
     */
    @DeleteMapping("/{prediccionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar predicción",
               description = "Elimina permanentemente una predicción de la base de datos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Predicción eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Predicción no encontrada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - se requiere rol ADMIN")
    })
    public ResponseEntity<Void> eliminarPrediccion(@PathVariable Long prediccionId) {
        logger.info("Solicitud de eliminacion para prediccion ID: {}", prediccionId);
        
        try {
            prediccionService.eliminarPrediccion(prediccionId);
            logger.info("Prediccion eliminada exitosamente: {}", prediccionId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Prediccion no encontrada: {}", prediccionId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene la lista de algoritmos disponibles en el sistema.
     * 
     * @return Mapa con código y nombre de cada algoritmo
     */
    @GetMapping("/algoritmos")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
    @Operation(summary = "Listar algoritmos disponibles",
               description = "Retorna la lista de algoritmos de predicción disponibles en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Algoritmos obtenidos exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Map<String, String>> obtenerAlgoritmosDisponibles() {
        logger.debug("Obteniendo lista de algoritmos disponibles");
        
        Map<String, String> algoritmos = prediccionService.obtenerAlgoritmosDisponibles();
        
        logger.info("Algoritmos disponibles: {}", algoritmos.keySet());
        return ResponseEntity.ok(algoritmos);
    }

    /**
     * Obtiene información detallada sobre los parámetros de cada algoritmo.
     * 
     * @return Información sobre parámetros y rangos válidos
     */
    @GetMapping("/algoritmos/info")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
    @Operation(summary = "Obtener información de algoritmos",
               description = "Retorna información detallada sobre parámetros y configuración de cada algoritmo")
    public ResponseEntity<Map<String, Object>> obtenerInfoAlgoritmos() {
        logger.debug("Obteniendo informacion detallada de algoritmos");
        
        Map<String, Object> info = new HashMap<>();
        
        Map<String, Object> smaInfo = new HashMap<>();
        smaInfo.put("codigo", "SMA");
        smaInfo.put("nombre", "Promedio Movil Simple");
        smaInfo.put("parametros", Map.of("ventana", Map.of(
            "descripcion", "Tamano de la ventana movil",
            "tipo", "integer",
            "rango", "3-100",
            "default", 14
        )));
        smaInfo.put("minimosDatos", 7);
        smaInfo.put("usoCaso", "Productos con demanda estable sin tendencia ni estacionalidad");
        
        Map<String, Object> sesInfo = new HashMap<>();
        sesInfo.put("codigo", "SES");
        sesInfo.put("nombre", "Suavizado Exponencial Simple");
        sesInfo.put("parametros", Map.of("alpha", Map.of(
            "descripcion", "Factor de suavizado",
            "tipo", "double",
            "rango", "0.01-0.99",
            "default", 0.3
        )));
        sesInfo.put("minimosDatos", 5);
        sesInfo.put("usoCaso", "Productos sin tendencia ni estacionalidad marcada");
        
        Map<String, Object> hwInfo = new HashMap<>();
        hwInfo.put("codigo", "HOLT_WINTERS");
        hwInfo.put("nombre", "Holt-Winters (Triple Exponencial)");
        hwInfo.put("parametros", Map.of(
            "alpha", Map.of("descripcion", "Factor de nivel", "rango", "0.01-0.99", "default", 0.4),
            "beta", Map.of("descripcion", "Factor de tendencia", "rango", "0.01-0.99", "default", 0.2),
            "gamma", Map.of("descripcion", "Factor de estacionalidad", "rango", "0.01-0.99", "default", 0.3),
            "periodo", Map.of("descripcion", "Periodo estacional", "rango", "2-52", "default", 7)
        ));
        hwInfo.put("minimosDatos", 14);
        hwInfo.put("usoCaso", "Productos con patrones estacionales claros");
        
        info.put("SMA", smaInfo);
        info.put("SES", sesInfo);
        info.put("HOLT_WINTERS", hwInfo);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Obtiene una recomendación automática de algoritmo basada en análisis de datos históricos.
     * 
     * <p>Este endpoint analiza los patrones en los datos de ventas del producto (tendencia,
     * estacionalidad, volatilidad) y recomienda el algoritmo más apropiado con parámetros optimizados.</p>
     * 
     * @param productoId ID del producto a analizar
     * @return Recomendación con algoritmo, parámetros, justificación y nivel de confianza
     */
    @GetMapping("/recomendar/{productoId}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'USUARIO')")
    @Operation(summary = "Obtener recomendación automática de algoritmo",
               description = "Analiza datos históricos y recomienda el algoritmo óptimo con parámetros configurados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recomendación generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos insuficientes para análisis (mínimo 7 registros)"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<?> obtenerRecomendacion(@PathVariable Integer productoId) {
        logger.info("Solicitud de recomendación automática para producto ID: {}", productoId);
        
        try {
            // Validar que el producto existe
            Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
            
            // Obtener datos históricos de ventas (usar paginación para obtener todos)
            Pageable pageable = PageRequest.of(0, 1000); // Obtener hasta 1000 registros
            Page<Kardex> kardexPage = kardexRepositorio.findByProductoAndTipo(
                productoId, 
                TipoMovimiento.SALIDA_VENTA, 
                pageable
            );
            
            List<Double> datosHistoricos = kardexPage.getContent().stream()
                .map(k -> k.getCantidad().doubleValue())
                .collect(Collectors.toList());
            
            // Revertir el orden (findByProductoAndTipo viene descendente)
            java.util.Collections.reverse(datosHistoricos);
            
            if (datosHistoricos.size() < 7) {
                logger.warn("Datos insuficientes para producto ID {}: solo {} registros", 
                           productoId, datosHistoricos.size());
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Datos insuficientes",
                        "mensaje", "Se necesitan al menos 7 registros de venta para el análisis automático",
                        "registrosActuales", datosHistoricos.size()
                    ));
            }
            
            // Generar recomendación
            RecomendacionAlgoritmo recomendacion = analisisAutomaticoService.analizarYRecomendar(datosHistoricos);
            
            logger.info("Recomendación generada para producto {}: {} con {}% de confianza",
                       productoId, 
                       recomendacion.getAlgoritmo(),
                       (int)(recomendacion.getConfianza() * 100));
            
            return ResponseEntity.ok(recomendacion);
            
        } catch (IllegalArgumentException e) {
            logger.error("Error en validación de datos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Validación fallida", "mensaje", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al generar recomendación para producto {}: {}", productoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "mensaje", "No se pudo analizar el producto"));
        }
    }
}

