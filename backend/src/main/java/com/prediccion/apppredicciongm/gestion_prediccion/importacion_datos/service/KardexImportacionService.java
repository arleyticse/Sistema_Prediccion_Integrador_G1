package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;
import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.KardexImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.KardexImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;

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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para importación masiva de movimientos de Kardex desde archivos CSV.
 * 
 * <p>Implementa la lógica de negocio para procesar archivos CSV de kardex,
 * realizar validaciones exhaustivas, y crear registros de movimientos de inventario
 * con trazabilidad completa.</p>
 * 
 * <h3>Características principales:</h3>
 * <ul>
 *   <li>Validación de formato CSV y estructura de datos</li>
 *   <li>Verificación de productos y proveedores existentes</li>
 *   <li>Validación de reglas de negocio (movimientos, saldos, fechas)</li>
 *   <li>Soporte para múltiples formatos de fecha</li>
 *   <li>Registro de auditoría completo</li>
 *   <li>Manejo transaccional de errores</li>
 *   <li>Caché de entidades para optimización</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KardexImportacionService implements IKardexImportacionService {

    private final IKardexRepositorio kardexRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IProveedorRepositorio proveedorRepositorio;
    private final IImportacionRepositorio importacionRepositorio;
    private final IUsuarioRepository usuarioRepository;
    private final Validator validator;

    private static final String CSV_SEPARADOR = ",";
    private static final int COLUMNAS_ESPERADAS = 18;
    
    /**
     * Formatos de fecha soportados para parsing
     */
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE
    };
    
    /**
     * Headers esperados en el archivo CSV
     */
    private static final String[] HEADERS_ESPERADOS = {
        "nombre_producto", "tipo_movimiento", "cantidad", "saldo_cantidad",
        "costo_unitario", "fecha_movimiento", "fecha_vencimiento", "nombre_proveedor",
        "lote", "tipo_documento", "numero_documento", "referencia",
        "motivo", "ubicacion", "observaciones", "anulado", "fecha_registro", "numero_fila"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public KardexImportacionResponse importarKardexDesdeCSV(
            MultipartFile archivo, Integer usuarioId) throws IOException {
        
        long tiempoInicio = System.currentTimeMillis();
        log.info("Iniciando importación de kardex desde archivo: {}", archivo.getOriginalFilename());

        // Validar archivo
        validarArchivo(archivo);

        // Registrar la importación
        ImportacionDatos importacion = registrarImportacion(archivo, usuarioId);

        KardexImportacionResponse response = KardexImportacionResponse.builder()
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
            List<KardexImportacionRequest> kardexRequests = leerCSV(archivo);
            response.setTotalRegistros(kardexRequests.size());

            // Cachear entidades para optimizar consultas
            Map<String, Producto> cacheProductos = new HashMap<>();
            Map<String, Proveedor> cacheProveedores = new HashMap<>();

            // Obtener usuario si está autenticado
            Usuario usuario = null;
            if (usuarioId != null) {
                usuario = usuarioRepository.findById(usuarioId).orElse(null);
            }

            int exitosos = 0;
            int fallidos = 0;

            for (KardexImportacionRequest request : kardexRequests) {
                try {
                    // Validar request con Bean Validation
                    Set<ConstraintViolation<KardexImportacionRequest>> violations = 
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

                    // Obtener proveedor si se especifica (con cache)
                    Proveedor proveedor = null;
                    if (request.getNombreProveedor() != null && !request.getNombreProveedor().trim().isEmpty()) {
                        proveedor = obtenerProveedor(request.getNombreProveedor(), cacheProveedores);
                        if (proveedor == null) {
                            response.agregarError(request.getNumeroFila(), request.getNombreProducto(), 
                                    "Proveedor no encontrado: " + request.getNombreProveedor());
                            fallidos++;
                            continue;
                        }
                    }

                    // Crear y guardar kardex
                    Kardex kardex = crearKardex(request, producto, proveedor, usuario);
                    kardexRepositorio.save(kardex);
                    exitosos++;

                    log.debug("Kardex creado exitosamente para producto: {} (Fila {})", 
                            producto.getNombre(), request.getNumeroFila());

                } catch (Exception e) {
                    log.error("Error procesando kardex en fila {}: {}", 
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

            log.info("Importación de kardex completada: {} exitosos, {} fallidos de {} total en {} ms",
                    exitosos, fallidos, response.getTotalRegistros(), tiempoProcesamiento);

            return response;

        } catch (Exception e) {
            log.error("Error crítico durante la importación de kardex: {}", e.getMessage(), e);
            actualizarImportacion(importacion, 0, 0, 0, EstadoImportacion.FALLIDA, 
                    System.currentTimeMillis() - tiempoInicio, e.getMessage());
            throw new RuntimeException("Error durante la importación de kardex: " + e.getMessage(), e);
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
        csv.append("Laptop Dell XPS 15,ENTRADA,10,110,1250.50,2025-01-15 10:00:00,2026-12-31,Proveedor Tech S.A.,LOTE-2025-001,Factura,F001-00123,REF-001,Compra mensual,A-01-01,Llegó en buen estado,false,2025-01-15 10:05:00,2\n");
        csv.append("Mouse Inalámbrico Logitech,SALIDA,5,205,,2025-01-16 14:30:00,,,,,V001-00045,REF-002,Venta cliente,B-02-05,Venta regular,false,2025-01-16 14:35:00,3\n");
        csv.append("Cable HDMI 2m,ENTRADA,50,200,8.75,2025-01-17 09:00:00,2027-06-30,Cables & Accesorios,LOTE-2025-002,Guía,GR001-123,REF-003,Reposición stock,C-03-10,Stock normal,false,2025-01-17 09:10:00,4\n");
        
        return csv.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KardexImportacionRequest parsearLineaCSV(String linea, int numeroFila) {
        String[] valores = linea.split(CSV_SEPARADOR, -1);
        
        if (valores.length != COLUMNAS_ESPERADAS) {
            throw new IllegalArgumentException(
                    String.format("Fila %d: Se esperaban %d columnas, se encontraron %d", 
                            numeroFila, COLUMNAS_ESPERADAS, valores.length));
        }

        try {
            return KardexImportacionRequest.builder()
                    .numeroFila(numeroFila)
                    .nombreProducto(valores[0].trim())
                    .tipoMovimiento(valores[1].trim())
                    .cantidad(parseInt(valores[2]))
                    .saldoCantidad(parseInt(valores[3]))
                    .costoUnitario(parseBigDecimalOrNull(valores[4]))
                    .fechaMovimiento(parseDateTime(valores[5]))
                    .fechaVencimiento(parseDateTimeOrNull(valores[6]))
                    .nombreProveedor(valores[7].trim().isEmpty() ? null : valores[7].trim())
                    .lote(valores[8].trim().isEmpty() ? null : valores[8].trim())
                    .tipoDocumento(valores[9].trim().isEmpty() ? null : valores[9].trim())
                    .numeroDocumento(valores[10].trim().isEmpty() ? null : valores[10].trim())
                    .referencia(valores[11].trim().isEmpty() ? null : valores[11].trim())
                    .motivo(valores[12].trim().isEmpty() ? null : valores[12].trim())
                    .ubicacion(valores[13].trim().isEmpty() ? null : valores[13].trim())
                    .observaciones(valores[14].trim().isEmpty() ? null : valores[14].trim())
                    .anulado(parseBoolean(valores[15]))
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
                .tipoDatos(TipoDatosImportacion.KARDEX)
                .nombreArchivo(archivo.getOriginalFilename())
                .rutaArchivo("/uploads/kardex/" + archivo.getOriginalFilename())
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
    private List<KardexImportacionRequest> leerCSV(MultipartFile archivo) throws IOException {
        List<KardexImportacionRequest> kardexList = new ArrayList<>();

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
                    KardexImportacionRequest request = parsearCSVRecord(record, numeroFila);
                    kardexList.add(request);
                } catch (Exception e) {
                    log.warn("Error parseando fila {}: {}", numeroFila, e.getMessage());
                    KardexImportacionRequest errorRequest = new KardexImportacionRequest();
                    errorRequest.setNumeroFila(numeroFila);
                    errorRequest.setNombreProducto("Error en fila");
                    errorRequest.agregarError(e.getMessage());
                    kardexList.add(errorRequest);
                }
            }

        } catch (Exception e) {
            throw new IOException("Error leyendo el archivo CSV: " + e.getMessage(), e);
        }

        return kardexList;
    }

    /**
     * Parsea un registro CSV en un DTO
     */
    private KardexImportacionRequest parsearCSVRecord(CSVRecord record, int numeroFila) {
        return KardexImportacionRequest.builder()
                .numeroFila(numeroFila)
                .nombreProducto(record.get("nombre_producto"))
                .tipoMovimiento(record.get("tipo_movimiento"))
                .cantidad(parseInt(record.get("cantidad")))
                .saldoCantidad(parseInt(record.get("saldo_cantidad")))
                .costoUnitario(parseBigDecimalOrNull(record.get("costo_unitario")))
                .fechaMovimiento(parseDateTime(record.get("fecha_movimiento")))
                .fechaVencimiento(parseDateTimeOrNull(record.get("fecha_vencimiento")))
                .nombreProveedor(record.get("nombre_proveedor").isEmpty() ? null : record.get("nombre_proveedor"))
                .lote(record.get("lote").isEmpty() ? null : record.get("lote"))
                .tipoDocumento(record.get("tipo_documento").isEmpty() ? null : record.get("tipo_documento"))
                .numeroDocumento(record.get("numero_documento").isEmpty() ? null : record.get("numero_documento"))
                .referencia(record.get("referencia").isEmpty() ? null : record.get("referencia"))
                .motivo(record.get("motivo").isEmpty() ? null : record.get("motivo"))
                .ubicacion(record.get("ubicacion").isEmpty() ? null : record.get("ubicacion"))
                .observaciones(record.get("observaciones").isEmpty() ? null : record.get("observaciones"))
                .anulado(parseBoolean(record.get("anulado")))
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
     * Parsea un string a BigDecimal (opcional)
     */
    private BigDecimal parseBigDecimalOrNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(valor.trim());
    }

    /**
     * Parsea un string a Boolean
     */
    private Boolean parseBoolean(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        String v = valor.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("si") || v.equals("yes");
    }

    /**
     * Parsea un string a LocalDateTime con múltiples formatos
     */
    private LocalDateTime parseDateTime(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }

        String v = valor.trim();
        
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(v, formatter);
            } catch (DateTimeParseException e) {
                // Intentar siguiente formato
            }
        }

        throw new IllegalArgumentException("Formato de fecha inválido: " + valor);
    }

    /**
     * Parsea un string a LocalDateTime (opcional)
     */
    private LocalDateTime parseDateTimeOrNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        
        try {
            return parseDateTime(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
     * Obtiene un proveedor por nombre comercial con caché
     */
    private Proveedor obtenerProveedor(String nombreProveedor, Map<String, Proveedor> cache) {
        String key = nombreProveedor.toLowerCase();
        
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Optional<Proveedor> proveedor = proveedorRepositorio.findByNombreComercialIgnoreCase(nombreProveedor);
        proveedor.ifPresent(p -> cache.put(key, p));
        
        return proveedor.orElse(null);
    }

    /**
     * Crea una entidad Kardex desde el request
     */
    private Kardex crearKardex(KardexImportacionRequest request, Producto producto, 
                               Proveedor proveedor, Usuario usuario) {
        Kardex kardex = new Kardex();
        kardex.setProducto(producto);
        
        // Mapear tipo de movimiento simplificado a enum completo
        TipoMovimiento tipoMovimiento;
        String tipoRequest = request.getTipoMovimiento().toUpperCase();
        if ("ENTRADA".equals(tipoRequest)) {
            // Si hay proveedor, es una compra; si no, es un ajuste
            tipoMovimiento = (proveedor != null) ? TipoMovimiento.ENTRADA_COMPRA : TipoMovimiento.ENTRADA_AJUSTE;
        } else if ("SALIDA".equals(tipoRequest)) {
            // Para salidas, asumimos que es venta (el tipo más común)
            tipoMovimiento = TipoMovimiento.SALIDA_VENTA;
        } else {
            // Si ya viene con el formato completo del enum, usarlo directamente
            tipoMovimiento = TipoMovimiento.valueOf(tipoRequest);
        }
        
        kardex.setTipoMovimiento(tipoMovimiento);
        kardex.setCantidad(request.getCantidad());
        kardex.setSaldoCantidad(request.getSaldoCantidad());
        kardex.setCostoUnitario(request.getCostoUnitario());
        kardex.setFechaMovimiento(request.getFechaMovimiento());
        kardex.setFechaVencimiento(request.getFechaVencimiento());
        kardex.setProveedor(proveedor);
        kardex.setLote(request.getLote());
        kardex.setTipoDocumento(request.getTipoDocumento());
        kardex.setNumeroDocumento(request.getNumeroDocumento());
        kardex.setReferencia(request.getReferencia());
        kardex.setMotivo(request.getMotivo());
        kardex.setUbicacion(request.getUbicacion());
        kardex.setObservaciones(request.getObservaciones());
        kardex.setAnulado(request.getAnulado() != null ? request.getAnulado() : false);
        kardex.setFechaRegistro(LocalDateTime.now());
        kardex.setUsuario(usuario);
        
        return kardex;
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
    private String generarResumenErrores(KardexImportacionResponse response) {
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
