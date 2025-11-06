package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.InventarioImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.InventarioImportacionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz de servicio para la importación masiva de inventario desde archivos CSV.
 * 
 * <p>Define los métodos necesarios para procesar archivos CSV de inventario inicial,
 * realizar validaciones, y generar reportes de la importación.</p>
 * 
 * <h3>Operaciones soportadas:</h3>
 * <ul>
 *   <li>Importación completa de inventario desde CSV</li>
 *   <li>Validación de formato y estructura del CSV</li>
 *   <li>Generación de plantilla CSV de ejemplo</li>
 *   <li>Parseo y validación de registros individuales</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
public interface IInventarioImportacionService {

    /**
     * Importa registros de inventario desde un archivo CSV.
     * 
     * <p>Procesa el archivo completo, valida cada registro, busca productos existentes,
     * verifica que no exista ya inventario, y crea los registros de inventario.
     * Registra la operación en la tabla de auditoría.</p>
     * 
     * @param archivo Archivo CSV con los datos de inventario
     * @param usuarioId ID del usuario que realiza la importación (opcional)
     * @return Respuesta con estadísticas y errores de la importación
     * @throws IOException Si hay error al leer el archivo
     */
    InventarioImportacionResponse importarInventarioDesdeCSV(MultipartFile archivo, Integer usuarioId) 
            throws IOException;

    /**
     * Valida solo el formato y estructura del archivo CSV sin procesarlo.
     * 
     * <p>Verifica que el archivo tenga el formato correcto, las columnas esperadas,
     * y que los datos básicos sean válidos. No crea registros en la base de datos.</p>
     * 
     * @param archivo Archivo CSV a validar
     * @return Lista de errores encontrados (vacía si el archivo es válido)
     * @throws IOException Si hay error al leer el archivo
     */
    List<String> validarFormatoCSV(MultipartFile archivo) throws IOException;

    /**
     * Genera una plantilla CSV de ejemplo para la importación de inventario.
     * 
     * <p>La plantilla incluye todas las columnas requeridas y opcionales,
     * con filas de ejemplo que muestran el formato correcto de los datos.</p>
     * 
     * @return String con el contenido CSV de la plantilla
     */
    String generarPlantillaCSV();

    /**
     * Parsea una línea del CSV en un objeto InventarioImportacionRequest.
     * 
     * <p>Convierte una línea de texto CSV en un DTO validado.
     * Útil para procesamiento línea por línea y validaciones individuales.</p>
     * 
     * @param linea Línea de texto del CSV
     * @param numeroFila Número de fila para reporte de errores
     * @return DTO con los datos parseados
     * @throws IllegalArgumentException Si el formato de la línea es inválido
     */
    InventarioImportacionRequest parsearLineaCSV(String linea, int numeroFila);
}
