package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.EstacionalidadImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.EstacionalidadImportacionResponse;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interfaz de servicio para la importación masiva de datos de estacionalidad desde archivos CSV.
 * <p>
 * Define operaciones para procesar archivos CSV conteniendo información de patrones
 * estacionales de productos, validar formato y datos, generar plantillas de ejemplo
 * y parsear registros individuales.
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
 * @see EstacionalidadImportacionRequest
 * @see EstacionalidadImportacionResponse
 */
public interface IEstacionalidadImportacionService {

    /**
     * Importa datos de estacionalidad desde un archivo CSV.
     * <p>
     * Procesa el archivo línea por línea, valida los datos, crea o actualiza
     * los registros de estacionalidad en la base de datos y registra la operación
     * de importación.
     * </p>
     * 
     * <h4>Proceso de importación:</h4>
     * <ol>
     *   <li>Valida formato del archivo CSV</li>
     *   <li>Lee y parsea cada registro</li>
     *   <li>Valida datos de negocio (coherencia de demandas, factor estacional)</li>
     *   <li>Verifica existencia del producto</li>
     *   <li>Detecta duplicados por producto-mes (actualiza si existe)</li>
     *   <li>Crea o actualiza estacionalidad</li>
     *   <li>Registra estadísticas y errores</li>
     * </ol>
     * 
     * @param archivo      archivo CSV con datos de estacionalidad
     * @param nombreArchivo nombre del archivo para registro
     * @return respuesta con estadísticas y errores del proceso
     * @throws IOException si hay error leyendo el archivo
     */
    EstacionalidadImportacionResponse importarEstacionalidadDesdeCSV(
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
    EstacionalidadImportacionResponse validarFormatoCSV(MultipartFile archivo) throws IOException;

    /**
     * Genera un archivo CSV plantilla con ejemplos de estacionalidad.
     * <p>
     * El archivo incluye:
     * <ul>
     *   <li>Encabezados de todas las columnas</li>
     *   <li>12 filas de ejemplo (una por cada mes del año)</li>
     *   <li>Factores estacionales variados según temporada</li>
     *   <li>Descripciones de temporadas típicas</li>
     * </ul>
     * </p>
     * 
     * @return contenido del archivo CSV como String
     */
    String generarPlantillaCSV();

    /**
     * Parsea una línea individual del CSV a un DTO de importación.
     * <p>
     * Convierte un registro CSV en un objeto EstacionalidadImportacionRequest,
     * manejando valores nulos, espacios en blanco y formatos incorrectos.
     * </p>
     * 
     * @param record       registro CSV a parsear
     * @param numeroFila   número de fila para seguimiento de errores
     * @return objeto DTO con datos de estacionalidad
     */
    EstacionalidadImportacionRequest parsearLineaCSV(CSVRecord record, int numeroFila);
}
