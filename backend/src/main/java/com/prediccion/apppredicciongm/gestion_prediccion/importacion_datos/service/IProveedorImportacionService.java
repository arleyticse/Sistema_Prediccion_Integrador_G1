package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ProveedorImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProveedorImportacionResponse;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interfaz de servicio para la importación masiva de proveedores desde archivos CSV.
 * <p>
 * Define operaciones para procesar archivos CSV conteniendo información de proveedores,
 * validar formato y datos, generar plantillas de ejemplo y parsear registros individuales.
 * </p>
 * 
 * <h3>Operaciones Principales:</h3>
 * <ul>
 *   <li>Importación completa desde archivo CSV</li>
 *   <li>Validación de formato sin persistir datos</li>
 *   <li>Generación de plantilla CSV de ejemplo</li>
 *   <li>Parseo de líneas individuales del CSV</li>
 * </ul>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 * @see ProveedorImportacionRequest
 * @see ProveedorImportacionResponse
 */
public interface IProveedorImportacionService {

    /**
     * Importa proveedores desde un archivo CSV.
     * <p>
     * Procesa el archivo línea por línea, valida los datos, crea o actualiza
     * los registros de proveedores en la base de datos y registra la operación
     * de importación.
     * </p>
     * 
     * <h4>Proceso de importación:</h4>
     * <ol>
     *   <li>Valida formato del archivo CSV</li>
     *   <li>Lee y parsea cada registro</li>
     *   <li>Valida datos de negocio</li>
     *   <li>Verifica duplicados por RUC/NIT o razón social</li>
     *   <li>Crea o actualiza proveedores</li>
     *   <li>Registra estadísticas y errores</li>
     * </ol>
     * 
     * @param archivo      archivo CSV con datos de proveedores
     * @param nombreArchivo nombre del archivo para registro
     * @return respuesta con estadísticas y errores del proceso
     * @throws IOException si hay error leyendo el archivo
     */
    ProveedorImportacionResponse importarProveedoresDesdeCSV(
        MultipartFile archivo, 
        String nombreArchivo
    ) throws IOException;

    /**
     * Valida el formato y contenido de un archivo CSV sin persistir datos.
     * <p>
     * Útil para pre-validar archivos antes de una importación real,
     * identificando errores de formato o datos sin modificar la base de datos.
     * </p>
     * 
     * @param archivo archivo CSV a validar
     * @return respuesta con resultados de validación y errores encontrados
     * @throws IOException si hay error leyendo el archivo
     */
    ProveedorImportacionResponse validarFormatoCSV(MultipartFile archivo) throws IOException;

    /**
     * Genera un archivo CSV plantilla con ejemplos de proveedores.
     * <p>
     * El archivo incluye:
     * <ul>
     *   <li>Encabezados de todas las columnas</li>
     *   <li>3-5 filas de ejemplo con datos válidos</li>
     *   <li>Comentarios explicativos en la primera fila</li>
     * </ul>
     * </p>
     * 
     * @return contenido del archivo CSV como String
     */
    String generarPlantillaCSV();

    /**
     * Parsea una línea individual del CSV a un DTO de importación.
     * <p>
     * Convierte un registro CSV en un objeto ProveedorImportacionRequest,
     * manejando valores nulos, espacios en blanco y formatos incorrectos.
     * </p>
     * 
     * @param record       registro CSV a parsear
     * @param numeroFila   número de fila para seguimiento de errores
     * @return objeto DTO con datos del proveedor
     */
    ProveedorImportacionRequest parsearLineaCSV(CSVRecord record, int numeroFila);
}
