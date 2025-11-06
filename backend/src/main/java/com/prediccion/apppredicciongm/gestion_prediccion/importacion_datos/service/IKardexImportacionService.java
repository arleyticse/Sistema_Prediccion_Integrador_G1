package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.KardexImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.KardexImportacionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz de servicio para importación masiva de movimientos de Kardex desde archivos CSV.
 * 
 * <p>Define los contratos para la importación, validación y generación de plantillas
 * de registros de kardex (movimientos de inventario con trazabilidad completa).</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Importar movimientos de kardex desde CSV</li>
 *   <li>Validar formato y estructura de archivos CSV</li>
 *   <li>Generar plantillas CSV de ejemplo</li>
 *   <li>Parsear líneas CSV individuales</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
public interface IKardexImportacionService {

    /**
     * Importa registros de kardex desde un archivo CSV.
     * 
     * <p>Procesa el archivo línea por línea, validando cada movimiento y creando
     * los registros correspondientes en la base de datos. Mantiene trazabilidad
     * completa de cada movimiento con información de documentos, lotes y proveedores.</p>
     * 
     * <h3>Validaciones realizadas:</h3>
     * <ul>
     *   <li>Formato CSV válido con 18 columnas</li>
     *   <li>Producto debe existir en el sistema</li>
     *   <li>Tipo de movimiento válido (ENTRADA/SALIDA)</li>
     *   <li>Cantidad positiva</li>
     *   <li>Saldo no negativo</li>
     *   <li>Fechas válidas y consistentes</li>
     *   <li>Proveedor existe (si se especifica)</li>
     *   <li>Validaciones de reglas de negocio complejas</li>
     * </ul>
     * 
     * <h3>Proceso de importación:</h3>
     * <ol>
     *   <li>Validar archivo (formato, tamaño, extensión)</li>
     *   <li>Registrar importación en tabla de auditoría</li>
     *   <li>Leer y parsear CSV línea por línea</li>
     *   <li>Validar cada registro (Bean Validation + reglas de negocio)</li>
     *   <li>Buscar entidades relacionadas (Producto, Proveedor)</li>
     *   <li>Crear y guardar movimiento de kardex</li>
     *   <li>Actualizar estadísticas de importación</li>
     *   <li>Retornar respuesta con resultados y errores</li>
     * </ol>
     * 
     * @param archivo Archivo CSV con movimientos de kardex (máx 10MB)
     * @param usuarioId ID del usuario que ejecuta la importación (puede ser null)
     * @return {@link KardexImportacionResponse} con estadísticas y errores detallados
     * @throws IOException Si hay error leyendo el archivo
     * @throws IllegalArgumentException Si el archivo es inválido
     */
    KardexImportacionResponse importarKardexDesdeCSV(MultipartFile archivo, Integer usuarioId) throws IOException;

    /**
     * Valida el formato y estructura de un archivo CSV sin ejecutar la importación.
     * 
     * <p>Realiza validaciones superficiales de formato:</p>
     * <ul>
     *   <li>Archivo no vacío</li>
     *   <li>Extensión .csv</li>
     *   <li>Cantidad de columnas correcta (18)</li>
     *   <li>Al menos una fila de datos</li>
     *   <li>Headers correctos</li>
     * </ul>
     * 
     * @param archivo Archivo CSV a validar
     * @return Lista de errores encontrados (vacía si el formato es válido)
     * @throws IOException Si hay error leyendo el archivo
     */
    List<String> validarFormatoCSV(MultipartFile archivo) throws IOException;

    /**
     * Genera una plantilla CSV de ejemplo para importación de kardex.
     * 
     * <p>La plantilla incluye:</p>
     * <ul>
     *   <li>Fila de headers con nombres de columnas</li>
     *   <li>3 filas de ejemplo con datos realistas</li>
     *   <li>Ejemplos de movimientos de ENTRADA y SALIDA</li>
     *   <li>Diferentes escenarios (con/sin proveedor, con/sin lote, etc.)</li>
     * </ul>
     * 
     * @return String con contenido CSV listo para descargar
     */
    String generarPlantillaCSV();

    /**
     * Parsea una línea CSV y convierte sus valores en un DTO de request.
     * 
     * <p>Realiza conversiones de tipos básicas:</p>
     * <ul>
     *   <li>String → Integer/BigDecimal</li>
     *   <li>String → LocalDateTime (múltiples formatos soportados)</li>
     *   <li>String → Boolean</li>
     *   <li>Campos vacíos → null</li>
     * </ul>
     * 
     * @param linea Línea CSV a parsear (separada por comas)
     * @param numeroFila Número de fila en el archivo (para reportar errores)
     * @return {@link KardexImportacionRequest} con los datos parseados
     * @throws IllegalArgumentException Si la línea no tiene el formato esperado
     */
    KardexImportacionRequest parsearLineaCSV(String linea, int numeroFila);
}
