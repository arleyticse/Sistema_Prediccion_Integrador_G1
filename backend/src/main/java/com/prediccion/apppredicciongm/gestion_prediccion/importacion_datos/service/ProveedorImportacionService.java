package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ProveedorImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProveedorImportacionResponse;
import com.prediccion.apppredicciongm.models.ImportacionDatos;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.repository.IImportacionRepositorio;
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
import java.util.*;

/**
 * Implementación del servicio de importación de proveedores desde archivos CSV.
 * <p>
 * Maneja la lógica completa de procesamiento de archivos CSV de proveedores,
 * incluyendo validación de datos, detección de duplicados, persistencia en
 * base de datos y registro de auditoría.
 * </p>
 * 
 * <h3>Características Principales:</h3>
 * <ul>
 *   <li>Validación exhaustiva de datos (Bean Validation + reglas de negocio)</li>
 *   <li>Detección de duplicados por RUC/NIT y razón social</li>
 *   <li>Actualización automática de proveedores existentes</li>
 *   <li>Registro de auditoría de todas las importaciones</li>
 *   <li>Manejo robusto de errores con seguimiento por fila</li>
 *   <li>Soporte para múltiples formatos de estado (SI/NO, ACTIVO/INACTIVO, etc.)</li>
 * </ul>
 * 
 * <h3>Formato CSV Esperado:</h3>
 * <pre>
 * razonSocial,nombreComercial,rucNit,telefono,email,direccion,ciudad,pais,personaContacto,tiempoEntregaDias,diasCredito,calificacion,estado,observaciones
 * </pre>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorImportacionService implements IProveedorImportacionService {

    private final IProveedorRepositorio proveedorRepositorio;
    private final IImportacionRepositorio importacionRepositorio;
    private final Validator validator;

    /**
     * Encabezados estándar del archivo CSV para importación de proveedores.
     */
    private static final String[] CSV_HEADERS = {
        "razonSocial", "nombreComercial", "rucNit", "telefono", "email",
        "direccion", "ciudad", "pais", "personaContacto", "tiempoEntregaDias",
        "diasCredito", "calificacion", "estado", "observaciones"
    };

    /**
     * {@inheritDoc}
     * 
     * <h4>Proceso Detallado:</h4>
     * <ol>
     *   <li>Registra inicio de importación en base de datos</li>
     *   <li>Parsea el archivo CSV con Apache Commons CSV</li>
     *   <li>Procesa cada registro:
     *     <ul>
     *       <li>Valida formato y datos de negocio</li>
     *       <li>Busca duplicados por RUC/NIT o razón social</li>
     *       <li>Crea nuevo proveedor o actualiza existente</li>
     *       <li>Registra errores de validación</li>
     *     </ul>
     *   </li>
     *   <li>Actualiza estadísticas de importación</li>
     *   <li>Retorna respuesta con resumen y errores</li>
     * </ol>
     */
    @Override
    @Transactional
    public ProveedorImportacionResponse importarProveedoresDesdeCSV(
            MultipartFile archivo, 
            String nombreArchivo) throws IOException {
        
        log.info("Iniciando importación de proveedores desde: {}", nombreArchivo);
        long startTime = System.currentTimeMillis();

        // Registrar importación
        ImportacionDatos importacion = registrarInicioImportacion(nombreArchivo);

        ProveedorImportacionResponse response = ProveedorImportacionResponse.builder()
                .importacionId(Long.valueOf(importacion.getImportacionId()))
                .nombreArchivo(nombreArchivo)
                .fechaProceso(LocalDateTime.now())
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

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

            log.info("Procesando {} registros de proveedores", records.size());

            int exitosos = 0;
            int fallidos = 0;
            int numeroFila = 2; // Inicia en 2 (fila 1 es encabezado)

            for (CSVRecord record : records) {
                try {
                    ProveedorImportacionRequest request = parsearLineaCSV(record, numeroFila);
                    
                    // Validar anotaciones
                    var violaciones = validator.validate(request);
                    if (!violaciones.isEmpty()) {
                        String errores = violaciones.stream()
                                .map(v -> v.getMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Error de validación");
                        response.agregarError(numeroFila, request.getRazonSocial(), errores);
                        fallidos++;
                        numeroFila++;
                        continue;
                    }

                    // Validar reglas de negocio
                    List<String> erroresNegocio = request.validarReglas();
                    if (!erroresNegocio.isEmpty()) {
                        String errores = String.join("; ", erroresNegocio);
                        response.agregarError(numeroFila, request.getRazonSocial(), errores);
                        fallidos++;
                        numeroFila++;
                        continue;
                    }

                    // Buscar proveedor existente (por RUC/NIT o razón social)
                    Proveedor proveedor = buscarProveedorExistente(request);
                    
                    if (proveedor == null) {
                        proveedor = new Proveedor();
                        proveedor.setFechaRegistro(LocalDateTime.now());
                        log.debug("Creando nuevo proveedor: {}", request.getRazonSocial());
                    } else {
                        log.debug("Actualizando proveedor existente: {}", request.getRazonSocial());
                    }

                    // Mapear datos del request al entity
                    mapearDatosProveedor(request, proveedor);
                    proveedorRepositorio.save(proveedor);

                    exitosos++;
                    log.debug("✓ Proveedor procesado exitosamente: {}", request.getRazonSocial());

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

            log.info("✅ Importación de proveedores completada. Exitosos: {}, Fallidos: {}", 
                exitosos, fallidos);
            log.info(response.generarResumen());

            return response;

        } catch (Exception e) {
            log.error("Error crítico en importación de proveedores: {}", e.getMessage(), e);
            actualizarImportacion(importacion, EstadoImportacion.FALLIDA, 0, 
                response.getTotalRegistros());
            throw new IOException("Error procesando archivo CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Realiza una validación completa sin modificar la base de datos.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public ProveedorImportacionResponse validarFormatoCSV(MultipartFile archivo) throws IOException {
        log.info("Validando formato CSV de proveedores");
        long startTime = System.currentTimeMillis();

        ProveedorImportacionResponse response = ProveedorImportacionResponse.builder()
                .nombreArchivo(archivo.getOriginalFilename())
                .fechaProceso(LocalDateTime.now())
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosFallidos(0)
                .build();

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
                    ProveedorImportacionRequest request = parsearLineaCSV(record, numeroFila);
                    
                    // Validar anotaciones
                    var violaciones = validator.validate(request);
                    if (!violaciones.isEmpty()) {
                        String errores = violaciones.stream()
                                .map(v -> v.getMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Error de validación");
                        response.agregarError(numeroFila, request.getRazonSocial(), errores);
                        invalidos++;
                        numeroFila++;
                        continue;
                    }

                    // Validar reglas de negocio
                    List<String> erroresNegocio = request.validarReglas();
                    if (!erroresNegocio.isEmpty()) {
                        String errores = String.join("; ", erroresNegocio);
                        response.agregarError(numeroFila, request.getRazonSocial(), errores);
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
     * 
     * <p>Genera plantilla con 3 ejemplos de proveedores reales.</p>
     */
    @Override
    public String generarPlantillaCSV() {
        log.info("Generando plantilla CSV de proveedores");

        try (StringWriter writer = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, 
                CSVFormat.DEFAULT.builder()
                    .setHeader(CSV_HEADERS)
                    .build())) {

            // Ejemplo 1: Proveedor nacional completo
            csvPrinter.printRecord(
                "Distribuidora ABC S.A.C.",
                "ABC Distribuciones",
                "20123456789",
                "+51 987654321",
                "ventas@abc.com.pe",
                "Av. Industrial 123",
                "Lima",
                "Perú",
                "Juan Pérez Gonzales",
                "7",
                "30",
                "8.5",
                "SI",
                "Proveedor principal de productos de limpieza"
            );

            // Ejemplo 2: Proveedor internacional
            csvPrinter.printRecord(
                "Global Supplies Inc.",
                "Global Supplies",
                "98765432101",
                "+1 555-123-4567",
                "contact@globalsupplies.com",
                "123 Main Street",
                "New York",
                "USA",
                "Mary Johnson",
                "30",
                "60",
                "9.0",
                "ACTIVO",
                "Importador de productos especializados"
            );

            // Ejemplo 3: Proveedor local básico
            csvPrinter.printRecord(
                "Comercial La Esperanza E.I.R.L.",
                "La Esperanza",
                "10456789123",
                "987123456",
                "laesperanza@gmail.com",
                "Jr. Los Pinos 456",
                "Arequipa",
                "Perú",
                "Carlos Ramírez",
                "3",
                "15",
                "7.0",
                "1",
                "Proveedor local de insumos varios"
            );

            csvPrinter.flush();
            String plantilla = writer.toString();
            
            log.info("✓ Plantilla CSV generada exitosamente");
            return plantilla;

        } catch (IOException e) {
            log.error("Error generando plantilla CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando plantilla", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Maneja valores nulos, vacíos y tipos de datos incorrectos.</p>
     */
    @Override
    public ProveedorImportacionRequest parsearLineaCSV(CSVRecord record, int numeroFila) {
        return ProveedorImportacionRequest.builder()
                .razonSocial(obtenerValorSeguro(record, "razonSocial"))
                .nombreComercial(obtenerValorSeguro(record, "nombreComercial"))
                .rucNit(obtenerValorSeguro(record, "rucNit"))
                .telefono(obtenerValorSeguro(record, "telefono"))
                .email(obtenerValorSeguro(record, "email"))
                .direccion(obtenerValorSeguro(record, "direccion"))
                .ciudad(obtenerValorSeguro(record, "ciudad"))
                .pais(obtenerValorSeguro(record, "pais"))
                .personaContacto(obtenerValorSeguro(record, "personaContacto"))
                .tiempoEntregaDias(parsearEntero(record, "tiempoEntregaDias"))
                .diasCredito(parsearEntero(record, "diasCredito"))
                .calificacion(parsearDecimal(record, "calificacion"))
                .estado(obtenerValorSeguro(record, "estado"))
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
        importacion.setTipoDatos(TipoDatosImportacion.PROVEEDORES);
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
     * Busca un proveedor existente por RUC/NIT o razón social.
     */
    private Proveedor buscarProveedorExistente(ProveedorImportacionRequest request) {
        // Primero buscar por RUC/NIT (identificador único)
        if (request.getRucNit() != null && !request.getRucNit().trim().isEmpty()) {
            Optional<Proveedor> porRuc = proveedorRepositorio.findByRucNit(request.getRucNit());
            if (porRuc.isPresent()) {
                return porRuc.get();
            }
        }

        // Luego buscar por razón social
        Optional<Proveedor> porRazon = proveedorRepositorio
            .findByRazonSocialIgnoreCase(request.getRazonSocial());
        return porRazon.orElse(null);
    }

    /**
     * Mapea los datos del request al entity Proveedor.
     */
    private void mapearDatosProveedor(ProveedorImportacionRequest request, Proveedor proveedor) {
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setNombreComercial(request.getNombreComercial());
        proveedor.setRucNit(request.getRucNit());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setCiudad(request.getCiudad());
        proveedor.setPais(request.getPais());
        proveedor.setPersonaContacto(request.getPersonaContacto());
        proveedor.setTiempoEntregaDias(request.getTiempoEntregaDias());
        proveedor.setDiasCredito(request.getDiasCredito());
        proveedor.setCalificacion(request.getCalificacion());
        proveedor.setEstado(request.convertirEstado());
        proveedor.setObservaciones(request.getObservaciones());
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
