package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository.IEstacionalidadRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.EstacionalidadImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.EstacionalidadImportacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
import com.prediccion.apppredicciongm.models.EstacionalidadProducto;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de importación de estacionalidad desde archivos CSV.
 * <p>
 * Maneja la lógica completa de procesamiento de archivos CSV de estacionalidad,
 * incluyendo validación de datos, detección de duplicados producto-mes,
 * persistencia en base de datos y registro de auditoría.
 * </p>
 * 
 * <h3>Características Principales:</h3>
 * <ul>
 *   <li>Validación exhaustiva de datos (Bean Validation + reglas de negocio)</li>
 *   <li>Detección de duplicados por producto + mes</li>
 *   <li>Actualización automática de registros existentes</li>
 *   <li>Registro de auditoría de todas las importaciones</li>
 *   <li>Manejo robusto de errores con seguimiento por fila</li>
 *   <li>Cache de productos para optimizar búsquedas</li>
 * </ul>
 * 
 * <h3>Formato CSV Esperado:</h3>
 * <pre>
 * nombreProducto,mes,factorEstacional,demandaPromedioHistorica,demandaMaxima,demandaMinima,anioReferencia,descripcionTemporada,observaciones
 * </pre>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EstacionalidadImportacionService implements IEstacionalidadImportacionService {

    private final IEstacionalidadRepositorio estacionalidadRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IImportacionRepositorio importacionRepositorio;
    private final Validator validator;

    /**
     * Encabezados estándar del archivo CSV para importación de estacionalidad.
     */
    private static final String[] CSV_HEADERS = {
        "nombreProducto", "mes", "factorEstacional", "demandaPromedioHistorica",
        "demandaMaxima", "demandaMinima", "anioReferencia", "descripcionTemporada",
        "observaciones"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EstacionalidadImportacionResponse importarEstacionalidadDesdeCSV(
            MultipartFile archivo,
            String nombreArchivo) throws IOException {

        log.info("Iniciando importación de estacionalidad desde: {}", nombreArchivo);
        long startTime = System.currentTimeMillis();

        // Registrar importación
        ImportacionDatos importacion = registrarInicioImportacion(nombreArchivo);

        EstacionalidadImportacionResponse response = EstacionalidadImportacionResponse.builder()
                .importacionId(Long.valueOf(importacion.getImportacionId()))
                .nombreArchivo(nombreArchivo)
                .fechaProceso(LocalDateTime.now())
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

        // Cache de productos para evitar consultas repetidas
        Map<String, Producto> cacheProductos = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream()));
             CSVParser csvParser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder()
                    .setHeader(CSV_HEADERS)
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build())) {

            List<CSVRecord> records = csvParser.getRecords();
            response.setTotalRegistros(records.size());

            log.info("Procesando {} registros de estacionalidad", records.size());

            int exitosos = 0;
            int fallidos = 0;
            int numeroFila = 2; // Inicia en 2 (fila 1 es encabezado)

            for (CSVRecord record : records) {
                try {
                    EstacionalidadImportacionRequest request = parsearLineaCSV(record, numeroFila);

                    // Validar anotaciones
                    var violaciones = validator.validate(request);
                    if (!violaciones.isEmpty()) {
                        String errores = violaciones.stream()
                                .map(v -> v.getMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Error de validación");
                        response.agregarError(numeroFila, 
                            request.getNombreProducto() + " - " + request.getNombreMes(), errores);
                        fallidos++;
                        numeroFila++;
                        continue;
                    }

                    // Validar reglas de negocio
                    List<String> erroresNegocio = request.validarReglas();
                    if (!erroresNegocio.isEmpty()) {
                        String errores = String.join("; ", erroresNegocio);
                        response.agregarError(numeroFila, 
                            request.getNombreProducto() + " - " + request.getNombreMes(), errores);
                        fallidos++;
                        numeroFila++;
                        continue;
                    }

                    // Obtener producto (con cache)
                    Producto producto = obtenerProducto(request.getNombreProducto(), cacheProductos);
                    if (producto == null) {
                        response.agregarError(numeroFila,
                            request.getNombreProducto() + " - " + request.getNombreMes(),
                            "Producto no encontrado: " + request.getNombreProducto());
                        fallidos++;
                        numeroFila++;
                        continue;
                    }

                    // Buscar estacionalidad existente (producto + mes)
                    Optional<EstacionalidadProducto> existente = estacionalidadRepositorio
                            .findByProductoAndMes(producto, request.getMes());

                    EstacionalidadProducto estacionalidad;
                    if (existente.isPresent()) {
                        estacionalidad = existente.get();
                        log.debug("Actualizando estacionalidad existente: {} - Mes {}", 
                                producto.getNombre(), request.getMes());
                    } else {
                        estacionalidad = new EstacionalidadProducto();
                        estacionalidad.setProducto(producto);
                        estacionalidad.setMes(request.getMes());
                        log.debug("Creando nueva estacionalidad: {} - Mes {}", 
                                producto.getNombre(), request.getMes());
                    }

                    // Mapear datos del request al entity
                    mapearDatosEstacionalidad(request, estacionalidad);
                    estacionalidadRepositorio.save(estacionalidad);

                    exitosos++;
                    log.debug("✓ Estacionalidad procesada: {} - {} ({}%)", 
                            producto.getNombre(), request.getNombreMes(), 
                            request.getPorcentajeFactorEstacional());

                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", numeroFila, e.getMessage(), e);
                    response.agregarError(numeroFila, "Error",
                        "Error inesperado: " + e.getMessage());
                    fallidos++;
                }
                numeroFila++;
            }

            response.setRegistrosExitosos(exitosos);
            response.setRegistrosFallidos(fallidos);
            response.setTasaExito(
                records.size() > 0 ? (exitosos * 100.0) / records.size() : 0.0
            );

            long endTime = System.currentTimeMillis();
            response.setTiempoProcesamiento(endTime - startTime);

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
            actualizarImportacion(importacion, estadoFinal, exitosos, fallidos);

            log.info("Importación de estacionalidad completada. Exitosos: {}, Fallidos: {}",
                    exitosos, fallidos);
            log.info(response.generarResumen());

            return response;

        } catch (Exception e) {
            log.error("Error crítico en importación de estacionalidad: {}", e.getMessage(), e);
            actualizarImportacion(importacion, EstadoImportacion.FALLIDA, 0,
                    response.getTotalRegistros());
            throw new IOException("Error procesando archivo CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EstacionalidadImportacionResponse validarFormatoCSV(MultipartFile archivo) throws IOException {
        log.info("Validando formato CSV de estacionalidad");
        long startTime = System.currentTimeMillis();

        EstacionalidadImportacionResponse response = EstacionalidadImportacionResponse.builder()
                .nombreArchivo(archivo.getOriginalFilename())
                .fechaProceso(LocalDateTime.now())
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

        Map<String, Producto> cacheProductos = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream()));
             CSVParser csvParser = new CSVParser(reader,
                CSVFormat.DEFAULT.builder()
                    .setHeader(CSV_HEADERS)
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build())) {

            List<CSVRecord> records = csvParser.getRecords();
            response.setTotalRegistros(records.size());

            int validos = 0;
            int invalidos = 0;
            int numeroFila = 2;

            for (CSVRecord record : records) {
                try {
                    EstacionalidadImportacionRequest request = parsearLineaCSV(record, numeroFila);

                    // Validar anotaciones
                    var violaciones = validator.validate(request);
                    if (!violaciones.isEmpty()) {
                        String errores = violaciones.stream()
                                .map(v -> v.getMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Error de validación");
                        response.agregarError(numeroFila,
                            request.getNombreProducto() + " - " + request.getNombreMes(), errores);
                        invalidos++;
                        numeroFila++;
                        continue;
                    }

                    // Validar reglas de negocio
                    List<String> erroresNegocio = request.validarReglas();
                    if (!erroresNegocio.isEmpty()) {
                        String errores = String.join("; ", erroresNegocio);
                        response.agregarError(numeroFila,
                            request.getNombreProducto() + " - " + request.getNombreMes(), errores);
                        invalidos++;
                        numeroFila++;
                        continue;
                    }

                    // Verificar que el producto existe
                    Producto producto = obtenerProducto(request.getNombreProducto(), cacheProductos);
                    if (producto == null) {
                        response.agregarError(numeroFila,
                            request.getNombreProducto() + " - " + request.getNombreMes(),
                            "Producto no encontrado");
                        invalidos++;
                        numeroFila++;
                        continue;
                    }

                    validos++;

                } catch (Exception e) {
                    response.agregarError(numeroFila, "Error",
                        "Error de formato: " + e.getMessage());
                    invalidos++;
                }
                numeroFila++;
            }

            response.setRegistrosExitosos(validos);
            response.setRegistrosFallidos(invalidos);
            response.setTasaExito(
                records.size() > 0 ? (validos * 100.0) / records.size() : 0.0
            );
            response.setTiempoProcesamiento(System.currentTimeMillis() - startTime);
            response.setEstado(invalidos == 0 ? "VALIDO" : "INVALIDO");

            log.info("Validación completada. Válidos: {}, Inválidos: {}", validos, invalidos);
            return response;

        } catch (Exception e) {
            log.error("Error validando CSV: {}", e.getMessage(), e);
            throw new IOException("Error validando archivo: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generarPlantillaCSV() {
        log.info("Generando plantilla CSV de estacionalidad");

        try (StringWriter writer = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                CSVFormat.DEFAULT.builder()
                    .setHeader(CSV_HEADERS)
                    .build())) {

            int anioActual = Year.now().getValue();

            // Ejemplo para Detergente Industrial - 12 meses con diferentes patrones
            String producto = "Detergente Industrial";

            // Enero - Baja demanda post-navidad
            csvPrinter.printRecord(producto, "1", "0.8", "400", "500", "300", anioActual, 
                "Temporada Baja", "Disminución post-fiestas");

            // Febrero - Demanda normal
            csvPrinter.printRecord(producto, "2", "1.0", "500", "550", "450", anioActual, 
                "Normal", "Sin eventos especiales");

            // Marzo - Inicio de temporada alta
            csvPrinter.printRecord(producto, "3", "1.2", "600", "700", "550", anioActual, 
                "Pre-Semana Santa", "Incremento por limpieza profunda");

            // Abril - Semana Santa
            csvPrinter.printRecord(producto, "4", "1.5", "750", "900", "600", anioActual, 
                "Semana Santa", "Alta demanda por feriados");

            // Mayo - Día de la Madre
            csvPrinter.printRecord(producto, "5", "1.3", "650", "750", "550", anioActual, 
                "Día de la Madre", "Incremento por regalos corporativos");

            // Junio - Temporada normal
            csvPrinter.printRecord(producto, "6", "1.0", "500", "600", "450", anioActual, 
                "Normal", "Sin eventos especiales");

            // Julio - Fiestas Patrias
            csvPrinter.printRecord(producto, "7", "1.4", "700", "850", "600", anioActual, 
                "Fiestas Patrias", "Alta demanda por celebraciones");

            // Agosto - Post-fiestas
            csvPrinter.printRecord(producto, "8", "0.9", "450", "550", "400", anioActual, 
                "Temporada Baja", "Disminución post-fiestas");

            // Septiembre - Inicio de primavera
            csvPrinter.printRecord(producto, "9", "1.1", "550", "650", "500", anioActual, 
                "Limpieza Primavera", "Incremento por limpieza estacional");

            // Octubre - Preparación fin de año
            csvPrinter.printRecord(producto, "10", "1.2", "600", "700", "550", anioActual, 
                "Pre-Navidad", "Inicio de compras empresariales");

            // Noviembre - Black Friday
            csvPrinter.printRecord(producto, "11", "1.6", "800", "950", "650", anioActual, 
                "Black Friday/Navidad", "Máxima demanda anual");

            // Diciembre - Navidad
            csvPrinter.printRecord(producto, "12", "1.8", "900", "1100", "750", anioActual, 
                "Navidad/Año Nuevo", "Pico máximo de demanda");

            csvPrinter.flush();
            String plantilla = writer.toString();

            log.info("✓ Plantilla CSV generada exitosamente con 12 meses");
            return plantilla;

        } catch (IOException e) {
            log.error("Error generando plantilla CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando plantilla", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EstacionalidadImportacionRequest parsearLineaCSV(CSVRecord record, int numeroFila) {
        return EstacionalidadImportacionRequest.builder()
                .nombreProducto(obtenerValorSeguro(record, "nombreProducto"))
                .mes(parsearEntero(record, "mes"))
                .factorEstacional(parsearDecimal(record, "factorEstacional"))
                .demandaPromedioHistorica(parsearEntero(record, "demandaPromedioHistorica"))
                .demandaMaxima(parsearEntero(record, "demandaMaxima"))
                .demandaMinima(parsearEntero(record, "demandaMinima"))
                .anioReferencia(parsearEntero(record, "anioReferencia"))
                .descripcionTemporada(obtenerValorSeguro(record, "descripcionTemporada"))
                .observaciones(obtenerValorSeguro(record, "observaciones"))
                .numeroFila(numeroFila)
                .build();
    }

    // ==================== MÉTODOS PRIVADOS DE UTILIDAD ====================

    /**
     * Registra el inicio de una importación en la base de datos.
     */
    private ImportacionDatos registrarInicioImportacion(String nombreArchivo) {
        ImportacionDatos importacion = new ImportacionDatos();
        importacion.setNombreArchivo(nombreArchivo);
        importacion.setTipoDatos(TipoDatosImportacion.ESTACIONALIDAD);
        importacion.setFechaImportacion(LocalDateTime.now());
        importacion.setEstadoImportacion(EstadoImportacion.EN_PROCESO);
        importacion.setRegistrosProcesados(0);
        importacion.setRegistrosExitosos(0);
        importacion.setRegistrosFallidos(0);

        return importacionRepositorio.save(importacion);
    }

    /**
     * Actualiza el estado final de una importación.
     */
    private void actualizarImportacion(ImportacionDatos importacion,
                                      EstadoImportacion estado,
                                      int exitosos, int fallidos) {
        importacion.setEstadoImportacion(estado);
        importacion.setRegistrosExitosos(exitosos);
        importacion.setRegistrosFallidos(fallidos);
        importacion.setRegistrosProcesados(exitosos + fallidos);
        importacionRepositorio.save(importacion);
    }

    /**
     * Obtiene un producto por nombre, usando cache para optimizar.
     */
    private Producto obtenerProducto(String nombreProducto, Map<String, Producto> cache) {
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            return null;
        }

        // Buscar en cache primero
        if (cache.containsKey(nombreProducto)) {
            return cache.get(nombreProducto);
        }

        // Buscar en BD
        Optional<Producto> producto = productoRepositorio.findByNombreIgnoreCase(nombreProducto);
        if (producto.isPresent()) {
            cache.put(nombreProducto, producto.get());
            return producto.get();
        }

        cache.put(nombreProducto, null); // Marcar como no encontrado
        return null;
    }

    /**
     * Mapea los datos del request al entity EstacionalidadProducto.
     */
    private void mapearDatosEstacionalidad(EstacionalidadImportacionRequest request,
                                           EstacionalidadProducto estacionalidad) {
        estacionalidad.setFactorEstacional(request.getFactorEstacional());
        estacionalidad.setDemandaPromedioHistorica(request.getDemandaPromedioHistorica());
        estacionalidad.setDemandaMaxima(request.getDemandaMaxima());
        estacionalidad.setDemandaMinima(request.getDemandaMinima());
        estacionalidad.setAnioReferencia(request.getAnioReferencia() != null ? 
            request.getAnioReferencia() : Year.now().getValue());
        estacionalidad.setDescripcionTemporada(request.getDescripcionTemporada());
        estacionalidad.setObservaciones(request.getObservaciones());
    }

    /**
     * Obtiene un valor string del CSV de forma segura.
     */
    private String obtenerValorSeguro(CSVRecord record, String columna) {
        try {
            String valor = record.get(columna);
            return (valor != null && !valor.trim().isEmpty()) ? valor.trim() : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parsea un valor entero del CSV de forma segura.
     */
    private Integer parsearEntero(CSVRecord record, String columna) {
        String valor = obtenerValorSeguro(record, columna);
        if (valor == null) return null;

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear entero en columna {}: {}", columna, valor);
            return null;
        }
    }

    /**
     * Parsea un valor decimal del CSV de forma segura.
     */
    private BigDecimal parsearDecimal(CSVRecord record, String columna) {
        String valor = obtenerValorSeguro(record, columna);
        if (valor == null) return null;

        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear decimal en columna {}: {}", columna, valor);
            return null;
        }
    }
}
