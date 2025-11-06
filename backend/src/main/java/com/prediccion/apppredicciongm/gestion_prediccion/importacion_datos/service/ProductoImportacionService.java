package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ProductoImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProductoImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
import com.prediccion.apppredicciongm.repository.ICategoriaRepositorio;
import com.prediccion.apppredicciongm.repository.IUnidadMedidaRepositorio;

import com.prediccion.apppredicciongm.models.ImportacionDatos;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.models.Inventario.Categoria;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;

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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para importación masiva de productos desde archivos CSV
 * Soporta validaciones, manejo de errores y registro de auditoría
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoImportacionService implements IProductoImportacionService {

    private final IProductoRepositorio productoRepositorio;
    private final ICategoriaRepositorio categoriaRepositorio;
    private final IUnidadMedidaRepositorio unidadMedidaRepositorio;
    private final IImportacionRepositorio importacionRepositorio;
    private final IUsuarioRepository usuarioRepository;
    private final Validator validator;

    private static final String CSV_SEPARADOR = ",";
    private static final int COLUMNAS_ESPERADAS = 8;
    
    // Headers esperados en el CSV
    private static final String[] HEADERS_ESPERADOS = {
        "nombre", "costo_adquisicion", "costo_mantenimiento", "costo_mantenimiento_anual",
        "costo_pedido", "dias_lead_time", "categoria", "unidad_medida"
    };

    /**
     * Importa productos desde un archivo CSV
     */
    @Override
    @Transactional
    public ProductoImportacionResponse importarProductosDesdeCSV(MultipartFile archivo, Integer usuarioId) 
            throws IOException {
        
        long tiempoInicio = System.currentTimeMillis();
        log.info("Iniciando importación de productos desde archivo: {}", archivo.getOriginalFilename());

        // Validar archivo
        validarArchivo(archivo);

        // Registrar la importación
        ImportacionDatos importacion = registrarImportacion(archivo, usuarioId);

        ProductoImportacionResponse response = ProductoImportacionResponse.builder()
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
            List<ProductoImportacionRequest> productosRequest = leerCSV(archivo);
            response.setTotalRegistros(productosRequest.size());

            // Cachear categorías y unidades de medida para optimizar consultas
            Map<String, Categoria> cacheCategorias = new HashMap<>();
            Map<String, UnidadMedida> cacheUnidadesMedida = new HashMap<>();

            int exitosos = 0;
            int fallidos = 0;

            for (ProductoImportacionRequest request : productosRequest) {
                try {
                    // Validar request
                    Set<ConstraintViolation<ProductoImportacionRequest>> violations = 
                            validator.validate(request);
                    
                    if (!violations.isEmpty()) {
                        String errores = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        response.agregarError(request.getNumeroFila(), request.getNombre(), errores);
                        fallidos++;
                        continue;
                    }

                    // Verificar si el producto ya existe
                    if (productoRepositorio.existsByNombreIgnoreCase(request.getNombre())) {
                        response.agregarError(request.getNumeroFila(), request.getNombre(), 
                                "El producto ya existe en la base de datos");
                        fallidos++;
                        continue;
                    }

                    // Obtener o buscar categoría (con cache)
                    Categoria categoria = obtenerCategoria(request.getNombreCategoria(), cacheCategorias);
                    if (categoria == null) {
                        response.agregarError(request.getNumeroFila(), request.getNombre(), 
                                "Categoría no encontrada: " + request.getNombreCategoria());
                        fallidos++;
                        continue;
                    }

                    // Obtener o buscar unidad de medida (con cache)
                    UnidadMedida unidadMedida = obtenerUnidadMedida(
                            request.getAbreviaturaUnidadMedida(), cacheUnidadesMedida);
                    if (unidadMedida == null) {
                        response.agregarError(request.getNumeroFila(), request.getNombre(), 
                                "Unidad de medida no encontrada: " + request.getAbreviaturaUnidadMedida());
                        fallidos++;
                        continue;
                    }

                    // Crear y guardar producto
                    Producto producto = crearProducto(request, categoria, unidadMedida);
                    productoRepositorio.save(producto);
                    exitosos++;

                    log.debug("Producto importado exitosamente: {} (Fila {})", 
                            producto.getNombre(), request.getNumeroFila());

                } catch (Exception e) {
                    log.error("Error procesando producto en fila {}: {}", 
                            request.getNumeroFila(), e.getMessage(), e);
                    response.agregarError(request.getNumeroFila(), request.getNombre(), 
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

            log.info("Importación completada: {} exitosos, {} fallidos de {} total en {} ms",
                    exitosos, fallidos, response.getTotalRegistros(), tiempoProcesamiento);

            return response;

        } catch (Exception e) {
            log.error("Error crítico durante la importación: {}", e.getMessage(), e);
            actualizarImportacion(importacion, 0, 0, 0, EstadoImportacion.FALLIDA, 
                    System.currentTimeMillis() - tiempoInicio, e.getMessage());
            throw new RuntimeException("Error durante la importación: " + e.getMessage(), e);
        }
    }

    /**
     * Valida el formato del CSV sin procesarlo
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
     * Genera plantilla CSV de ejemplo
     */
    @Override
    public String generarPlantillaCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Headers
        csv.append(String.join(CSV_SEPARADOR, HEADERS_ESPERADOS)).append("\n");
        
        // Ejemplos
        csv.append("Laptop Dell XPS 15,1200.50,15.00,180.00,25.00,5,Electrónica,UND\n");
        csv.append("Mouse Inalámbrico Logitech,35.99,0.50,6.00,5.00,3,Periféricos,UND\n");
        csv.append("Cable HDMI 2m,12.50,0.20,2.40,3.00,2,Cables,UND\n");
        
        return csv.toString();
    }

    /**
     * Parsea una línea CSV en ProductoImportacionRequest
     */
    @Override
    public ProductoImportacionRequest parsearLineaCSV(String linea, int numeroFila) {
        String[] valores = linea.split(CSV_SEPARADOR, -1);
        
        if (valores.length != COLUMNAS_ESPERADAS) {
            throw new IllegalArgumentException(
                    String.format("Fila %d: Se esperaban %d columnas, se encontraron %d", 
                            numeroFila, COLUMNAS_ESPERADAS, valores.length));
        }

        try {
            return ProductoImportacionRequest.builder()
                    .numeroFila(numeroFila)
                    .nombre(valores[0].trim())
                    .costoAdquisicion(new BigDecimal(valores[1].trim()))
                    .costoMantenimiento(new BigDecimal(valores[2].trim()))
                    .costoMantenimientoAnual(valores[3].trim().isEmpty() ? null : new BigDecimal(valores[3].trim()))
                    .costoPedido(new BigDecimal(valores[4].trim()))
                    .diasLeadTime(Integer.parseInt(valores[5].trim()))
                    .nombreCategoria(valores[6].trim())
                    .abreviaturaUnidadMedida(valores[7].trim())
                    .build();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Fila %d: Error de formato en valores numéricos: %s", 
                            numeroFila, e.getMessage()));
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

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

    private ImportacionDatos registrarImportacion(MultipartFile archivo, Integer usuarioId) {
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        ImportacionDatos importacion = ImportacionDatos.builder()
                .tipoDatos(TipoDatosImportacion.PRODUCTOS)
                .nombreArchivo(archivo.getOriginalFilename())
                .rutaArchivo("/uploads/productos/" + archivo.getOriginalFilename())
                .fechaImportacion(LocalDateTime.now())
                .estadoImportacion(EstadoImportacion.EN_PROCESO)
                .usuario(usuario)
                .registrosProcesados(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

        return importacionRepositorio.save(importacion);
    }

    private List<ProductoImportacionRequest> leerCSV(MultipartFile archivo) throws IOException {
        List<ProductoImportacionRequest> productos = new ArrayList<>();

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
                    ProductoImportacionRequest request = parsearCSVRecord(record, numeroFila);
                    productos.add(request);
                } catch (Exception e) {
                    log.warn("Error parseando fila {}: {}", numeroFila, e.getMessage());
                    ProductoImportacionRequest errorRequest = new ProductoImportacionRequest();
                    errorRequest.setNumeroFila(numeroFila);
                    errorRequest.setNombre("Error en fila");
                    errorRequest.agregarError(e.getMessage());
                    productos.add(errorRequest);
                }
            }

        } catch (Exception e) {
            throw new IOException("Error leyendo el archivo CSV: " + e.getMessage(), e);
        }

        return productos;
    }

    private ProductoImportacionRequest parsearCSVRecord(CSVRecord record, int numeroFila) {
        try {
            return ProductoImportacionRequest.builder()
                    .numeroFila(numeroFila)
                    .nombre(record.get("nombre"))
                    .costoAdquisicion(parseBigDecimal(record.get("costo_adquisicion")))
                    .costoMantenimiento(parseBigDecimal(record.get("costo_mantenimiento")))
                    .costoMantenimientoAnual(record.get("costo_mantenimiento_anual").isEmpty() ? 
                            BigDecimal.ZERO : parseBigDecimal(record.get("costo_mantenimiento_anual")))
                    .costoPedido(parseBigDecimal(record.get("costo_pedido")))
                    .diasLeadTime(Integer.parseInt(record.get("dias_lead_time")))
                    .nombreCategoria(record.get("categoria"))
                    .abreviaturaUnidadMedida(record.get("unidad_medida"))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error de formato en valores: " + e.getMessage());
        }
    }

    private BigDecimal parseBigDecimal(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valor.trim());
    }

    private Categoria obtenerCategoria(String nombreCategoria, Map<String, Categoria> cache) {
        String key = nombreCategoria.toLowerCase();
        
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Optional<Categoria> categoria = categoriaRepositorio.findByNombreIgnoreCase(nombreCategoria);
        categoria.ifPresent(c -> cache.put(key, c));
        
        return categoria.orElse(null);
    }

    private UnidadMedida obtenerUnidadMedida(String abreviatura, Map<String, UnidadMedida> cache) {
        String key = abreviatura.toLowerCase();
        
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Optional<UnidadMedida> unidadMedida = 
                unidadMedidaRepositorio.findByAbreviaturaIgnoreCase(abreviatura);
        unidadMedida.ifPresent(um -> cache.put(key, um));
        
        return unidadMedida.orElse(null);
    }

    private Producto crearProducto(ProductoImportacionRequest request, 
                                   Categoria categoria, 
                                   UnidadMedida unidadMedida) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setCostoAdquisicion(request.getCostoAdquisicion());
        producto.setCostoMantenimiento(request.getCostoMantenimiento());
        producto.setCostoMantenimientoAnual(request.getCostoMantenimientoAnual());
        producto.setCostoPedido(request.getCostoPedido());
        producto.setDiasLeadTime(request.getDiasLeadTime());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        producto.setFechaRegistro(LocalDateTime.now());
        
        return producto;
    }

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

    private String generarResumenErrores(ProductoImportacionResponse response) {
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
