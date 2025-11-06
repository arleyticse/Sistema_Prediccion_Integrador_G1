package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.EstadoInventario;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.InventarioImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.InventarioImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Usuario;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para importación masiva de inventario desde archivos CSV.
 * 
 * <p>Implementa la lógica de negocio para procesar archivos CSV de inventario inicial,
 * realizar validaciones exhaustivas, y crear registros de inventario asociados a productos.</p>
 * 
 * <h3>Características principales:</h3>
 * <ul>
 *   <li>Validación de formato CSV y estructura de datos</li>
 *   <li>Verificación de productos existentes</li>
 *   <li>Validación de reglas de negocio (stocks, puntos de reorden)</li>
 *   <li>Prevención de duplicados (un producto = un inventario)</li>
 *   <li>Registro de auditoría completo</li>
 *   <li>Manejo transaccional de errores</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioImportacionService implements IInventarioImportacionService {

    private final IInventarioRepositorio inventarioRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IImportacionRepositorio importacionRepositorio;
    private final IUsuarioRepository usuarioRepository;
    private final Validator validator;

    private static final String CSV_SEPARADOR = ",";
    private static final int COLUMNAS_ESPERADAS = 11;
    
    /**
     * Headers esperados en el archivo CSV
     */
    private static final String[] HEADERS_ESPERADOS = {
        "nombre_producto", "stock_disponible", "stock_minimo", "stock_maximo",
        "punto_reorden", "stock_reservado", "stock_en_transito", 
        "ubicacion_almacen", "estado", "observaciones", "dias_sin_venta"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public InventarioImportacionResponse importarInventarioDesdeCSV(
            MultipartFile archivo, Integer usuarioId) throws IOException {
        
        long tiempoInicio = System.currentTimeMillis();
        log.info("Iniciando importación de inventario desde archivo: {}", archivo.getOriginalFilename());

        // Validar archivo
        validarArchivo(archivo);

        // Registrar la importación
        ImportacionDatos importacion = registrarImportacion(archivo, usuarioId);

        InventarioImportacionResponse response = InventarioImportacionResponse.builder()
                .importacionId(importacion.getImportacionId())
                .nombreArchivo(archivo.getOriginalFilename())
                .fechaProceso(LocalDateTime.now())
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .erroresDetallados(new ArrayList<>())
                .build();

        try {
            // Leer y procesar el CSV
            List<InventarioImportacionRequest> inventariosRequest = leerCSV(archivo);
            response.setTotalRegistros(inventariosRequest.size());

            // Cachear productos para optimizar consultas
            Map<String, Producto> cacheProductos = new HashMap<>();

            int exitosos = 0;
            int fallidos = 0;

            for (InventarioImportacionRequest request : inventariosRequest) {
                try {
                    // Validar request con Bean Validation
                    Set<ConstraintViolation<InventarioImportacionRequest>> violations = 
                            validator.validate(request);
                    
                    if (!violations.isEmpty()) {
                        String errores = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        response.agregarError(request.getNumeroFila(), request.getNombreProducto(), errores);
                        fallidos++;
                        continue;
                    }

                    // Validar reglas de negocio
                    if (!request.validarReglas()) {
                        String errores = String.join(", ", request.getErrores());
                        response.agregarError(request.getNumeroFila(), request.getNombreProducto(), errores);
                        fallidos++;
                        continue;
                    }

                    // Obtener producto (con cache)
                    Producto producto = obtenerProducto(request.getNombreProducto(), cacheProductos);
                    if (producto == null) {
                        response.agregarError(request.getNumeroFila(), request.getNombreProducto(), 
                                "Producto no encontrado: " + request.getNombreProducto());
                        fallidos++;
                        continue;
                    }

                    // Verificar que no exista inventario para este producto
                    if (inventarioRepositorio.existsByProducto(producto)) {
                        response.agregarError(request.getNumeroFila(), request.getNombreProducto(), 
                                "Ya existe un inventario para este producto");
                        fallidos++;
                        continue;
                    }

                    // Crear y guardar inventario
                    Inventario inventario = crearInventario(request, producto);
                    inventarioRepositorio.save(inventario);
                    exitosos++;

                    log.debug("Inventario creado exitosamente para producto: {} (Fila {})", 
                            producto.getNombre(), request.getNumeroFila());

                } catch (Exception e) {
                    log.error("Error procesando inventario en fila {}: {}", 
                            request.getNumeroFila(), e.getMessage(), e);
                    response.agregarError(request.getNumeroFila(), request.getNombreProducto(), 
                            "Error inesperado: " + e.getMessage());
                    fallidos++;
                }
            }

            response.setRegistrosExitosos(exitosos);
            response.setRegistrosFallidos(fallidos);

            // Calcular tasa de éxito
            if (response.getTotalRegistros() > 0) {
                double tasaExito = (exitosos * 100.0) / response.getTotalRegistros();
                response.setTasaExito(tasaExito);
            } else {
                response.setTasaExito(0.0);
            }

            // Determinar estado final
            EstadoImportacion estadoFinal;
            if (fallidos == 0) {
                estadoFinal = EstadoImportacion.COMPLETADA;
                response.setEstado("COMPLETADA");
            } else if (exitosos > 0) {
                estadoFinal = EstadoImportacion.COMPLETADA_CON_ERRORES;
                response.setEstado("COMPLETADA_CON_ERRORES");
            } else {
                estadoFinal = EstadoImportacion.FALLIDA;
                response.setEstado("FALLIDA");
            }

            // Actualizar registro de importación
            long tiempoFin = System.currentTimeMillis();
            long tiempoProcesamiento = tiempoFin - tiempoInicio;
            response.setTiempoProcesamiento(tiempoProcesamiento);

            actualizarImportacion(importacion, exitosos, fallidos, 
                    response.getTotalRegistros(), estadoFinal, tiempoProcesamiento, 
                    generarResumenErrores(response));

            log.info("Importación de inventario completada: {} exitosos, {} fallidos de {} total en {} ms",
                    exitosos, fallidos, response.getTotalRegistros(), tiempoProcesamiento);

            return response;

        } catch (Exception e) {
            log.error("Error crítico durante la importación de inventario: {}", e.getMessage(), e);
            actualizarImportacion(importacion, 0, 0, 0, EstadoImportacion.FALLIDA, 
                    System.currentTimeMillis() - tiempoInicio, e.getMessage());
            throw new RuntimeException("Error durante la importación de inventario: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> validarFormatoCSV(MultipartFile archivo) throws IOException {
        List<String> errores = new ArrayList<>();

        if (archivo.isEmpty()) {
            errores.add("El archivo está vacío");
            return errores;
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".csv")) {
            errores.add("El archivo debe ser formato CSV");
            return errores;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
            
            List<CSVRecord> records = parser.getRecords();
            
            if (records.isEmpty()) {
                errores.add("El archivo no contiene filas de datos");
            }

            // Validar que tenga las columnas esperadas
            if (parser.getHeaderNames().size() != COLUMNAS_ESPERADAS) {
                errores.add(String.format("Se esperaban %d columnas, se encontraron %d", 
                        COLUMNAS_ESPERADAS, parser.getHeaderNames().size()));
            }

        } catch (Exception e) {
            errores.add("Error leyendo el CSV: " + e.getMessage());
        }

        return errores;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generarPlantillaCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Headers
        csv.append(String.join(CSV_SEPARADOR, HEADERS_ESPERADOS)).append("\n");
        
        // Ejemplos
        csv.append("Laptop Dell XPS 15,50,10,100,20,5,0,A-01-01,ACTIVO,Stock inicial sistema,0\n");
        csv.append("Mouse Inalámbrico Logitech,200,50,500,80,10,20,B-02-05,ACTIVO,,0\n");
        csv.append("Cable HDMI 2m,150,30,300,50,0,0,C-03-10,ACTIVO,Nuevo producto,0\n");
        
        return csv.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InventarioImportacionRequest parsearLineaCSV(String linea, int numeroFila) {
        String[] valores = linea.split(CSV_SEPARADOR, -1);
        
        if (valores.length != COLUMNAS_ESPERADAS) {
            throw new IllegalArgumentException(
                    String.format("Fila %d: Se esperaban %d columnas, se encontraron %d", 
                            numeroFila, COLUMNAS_ESPERADAS, valores.length));
        }

        try {
            return InventarioImportacionRequest.builder()
                    .numeroFila(numeroFila)
                    .nombreProducto(valores[0].trim())
                    .stockDisponible(parseInt(valores[1]))
                    .stockMinimo(parseInt(valores[2]))
                    .stockMaximo(parseIntOrNull(valores[3]))
                    .puntoReorden(parseIntOrNull(valores[4]))
                    .stockReservado(parseIntOrNull(valores[5]))
                    .stockEnTransito(parseIntOrNull(valores[6]))
                    .ubicacionAlmacen(valores[7].trim().isEmpty() ? null : valores[7].trim())
                    .estado(valores[8].trim().isEmpty() ? "ACTIVO" : valores[8].trim())
                    .observaciones(valores[9].trim().isEmpty() ? null : valores[9].trim())
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Fila %d: Error de formato en valores: %s", 
                            numeroFila, e.getMessage()));
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida que el archivo sea CSV y tenga un tamaño válido
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("El archivo debe ser formato CSV");
        }

        // Validar tamaño máximo (10MB)
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 10MB");
        }
    }

    /**
     * Registra una nueva importación en la tabla de auditoría
     */
    private ImportacionDatos registrarImportacion(MultipartFile archivo, Integer usuarioId) {
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        ImportacionDatos importacion = ImportacionDatos.builder()
                .tipoDatos(TipoDatosImportacion.INVENTARIO)
                .nombreArchivo(archivo.getOriginalFilename())
                .rutaArchivo("/uploads/inventario/" + archivo.getOriginalFilename())
                .fechaImportacion(LocalDateTime.now())
                .estadoImportacion(EstadoImportacion.EN_PROCESO)
                .usuario(usuario)
                .registrosProcesados(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

        return importacionRepositorio.save(importacion);
    }

    /**
     * Lee el archivo CSV y parsea los registros
     */
    private List<InventarioImportacionRequest> leerCSV(MultipartFile archivo) throws IOException {
        List<InventarioImportacionRequest> inventarios = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader(HEADERS_ESPERADOS)
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            int numeroFila = 1; // Comienza en 1 (después del header)
            
            for (CSVRecord record : csvParser) {
                numeroFila++;
                
                try {
                    InventarioImportacionRequest request = parsearCSVRecord(record, numeroFila);
                    inventarios.add(request);
                } catch (Exception e) {
                    log.warn("Error parseando fila {}: {}", numeroFila, e.getMessage());
                    InventarioImportacionRequest errorRequest = new InventarioImportacionRequest();
                    errorRequest.setNumeroFila(numeroFila);
                    errorRequest.setNombreProducto("Error en fila");
                    errorRequest.agregarError(e.getMessage());
                    inventarios.add(errorRequest);
                }
            }

        } catch (Exception e) {
            throw new IOException("Error leyendo el archivo CSV: " + e.getMessage(), e);
        }

        return inventarios;
    }

    /**
     * Parsea un registro CSV en un DTO
     */
    private InventarioImportacionRequest parsearCSVRecord(CSVRecord record, int numeroFila) {
        return InventarioImportacionRequest.builder()
                .numeroFila(numeroFila)
                .nombreProducto(record.get("nombre_producto"))
                .stockDisponible(parseInt(record.get("stock_disponible")))
                .stockMinimo(parseInt(record.get("stock_minimo")))
                .stockMaximo(parseIntOrNull(record.get("stock_maximo")))
                .puntoReorden(parseIntOrNull(record.get("punto_reorden")))
                .stockReservado(parseIntOrNull(record.get("stock_reservado")))
                .stockEnTransito(parseIntOrNull(record.get("stock_en_transito")))
                .ubicacionAlmacen(record.get("ubicacion_almacen").isEmpty() ? null : record.get("ubicacion_almacen"))
                .estado(record.get("estado").isEmpty() ? "ACTIVO" : record.get("estado"))
                .observaciones(record.get("observaciones").isEmpty() ? null : record.get("observaciones"))
                .build();
    }

    /**
     * Parsea un string a Integer (obligatorio)
     */
    private Integer parseInt(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(valor.trim());
    }

    /**
     * Parsea un string a Integer (opcional)
     */
    private Integer parseIntOrNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(valor.trim());
    }

    /**
     * Obtiene un producto por nombre con caché
     */
    private Producto obtenerProducto(String nombreProducto, Map<String, Producto> cache) {
        String key = nombreProducto.toLowerCase();
        
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Optional<Producto> producto = productoRepositorio.findByNombreIgnoreCase(nombreProducto);
        producto.ifPresent(p -> cache.put(key, p));
        
        return producto.orElse(null);
    }

    /**
     * Crea una entidad Inventario desde el request
     */
    private Inventario crearInventario(InventarioImportacionRequest request, Producto producto) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setStockDisponible(request.getStockDisponible());
        inventario.setStockMinimo(request.getStockMinimo());
        inventario.setStockMaximo(request.getStockMaximo());
        inventario.setPuntoReorden(request.getPuntoReorden());
        inventario.setStockReservado(request.getStockReservado() != null ? request.getStockReservado() : 0);
        inventario.setStockEnTransito(request.getStockEnTransito() != null ? request.getStockEnTransito() : 0);
        inventario.setUbicacionAlmacen(request.getUbicacionAlmacen());
        
        // Convertir estado String a EstadoInventario enum
        EstadoInventario estadoEnum = convertirEstado(request.getEstado(), request.getStockDisponible(), 
                request.getStockMinimo(), request.getStockMaximo(), request.getPuntoReorden());
        inventario.setEstado(estadoEnum);
        
        inventario.setObservaciones(request.getObservaciones());
        inventario.setDiasSinVenta(0);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        
        return inventario;
    }
    
    /**
     * Convierte el estado del CSV a EstadoInventario y calcula automáticamente según stocks
     */
    private EstadoInventario convertirEstado(String estadoCSV, Integer stockDisponible, 
                                            Integer stockMinimo, Integer stockMaximo, Integer puntoReorden) {
        // Si el CSV especifica un estado válido, intentar usarlo
        if (estadoCSV != null && !estadoCSV.trim().isEmpty()) {
            try {
                return EstadoInventario.valueOf(estadoCSV.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es válido, calcular automáticamente
                log.debug("Estado inválido en CSV: {}, calculando automáticamente", estadoCSV);
            }
        }
        
        // Calcular estado según niveles de stock
        if (stockDisponible == 0) {
            return EstadoInventario.CRITICO;
        } else if (stockDisponible < stockMinimo) {
            return EstadoInventario.CRITICO;
        } else if (puntoReorden != null && stockDisponible <= puntoReorden) {
            return EstadoInventario.BAJO;
        } else if (stockMaximo != null && stockDisponible > stockMaximo) {
            return EstadoInventario.EXCESO;
        } else {
            return EstadoInventario.NORMAL;
        }
    }

    /**
     * Actualiza el registro de importación con los resultados
     */
    private void actualizarImportacion(ImportacionDatos importacion, 
                                      int exitosos, 
                                      int fallidos, 
                                      int total,
                                      EstadoImportacion estado, 
                                      long tiempoProcesamiento,
                                      String errores) {
        importacion.setRegistrosExitosos(exitosos);
        importacion.setRegistrosFallidos(fallidos);
        importacion.setRegistrosProcesados(total);
        importacion.setEstadoImportacion(estado);
        importacion.setTiempoProcesamiento(tiempoProcesamiento);
        importacion.setFechaActualizacion(LocalDateTime.now());
        
        if (errores != null && !errores.isEmpty()) {
            // Limitar a 2000 caracteres
            String erroresLimitados = errores.length() > 2000 ? 
                    errores.substring(0, 1997) + "..." : errores;
            importacion.setErrores(erroresLimitados);
        }

        importacionRepositorio.save(importacion);
    }

    /**
     * Genera un resumen de errores para el registro de auditoría
     */
    private String generarResumenErrores(InventarioImportacionResponse response) {
        if (response.getErroresDetallados().isEmpty()) {
            return null;
        }

        return response.getErroresDetallados().stream()
                .limit(10) // Primeros 10 errores
                .map(error -> String.format("Fila %d (%s): %s", 
                        error.getNumeroFila(), 
                        error.getNombreProducto(), 
                        error.getDescripcionError()))
                .collect(Collectors.joining(" | "));
    }
}
