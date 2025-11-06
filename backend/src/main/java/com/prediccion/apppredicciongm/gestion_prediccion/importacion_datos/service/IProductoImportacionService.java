package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.service;

import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request.ProductoImportacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response.ProductoImportacionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Interface para el servicio de importación de productos desde CSV
 */
public interface IProductoImportacionService {

    /**
     * Importa productos desde un archivo CSV
     * 
     * @param archivo Archivo CSV con los datos de productos
     * @param usuarioId ID del usuario que realiza la importación
     * @return Respuesta con el resultado de la importación
     * @throws IOException Si hay error leyendo el archivo
     */
    ProductoImportacionResponse importarProductosDesdeCSV(MultipartFile archivo, Integer usuarioId) throws IOException;

    /**
     * Valida el formato del archivo CSV sin procesarlo
     * 
     * @param archivo Archivo CSV a validar
     * @return Lista de errores encontrados (vacía si no hay errores)
     * @throws IOException Si hay error leyendo el archivo
     */
    List<String> validarFormatoCSV(MultipartFile archivo) throws IOException;

    /**
     * Genera una plantilla CSV de ejemplo para importación
     * 
     * @return Contenido de la plantilla en formato CSV
     */
    String generarPlantillaCSV();

    /**
     * Parsea una línea CSV en un ProductoImportacionRequest
     * 
     * @param linea Línea del CSV
     * @param numeroFila Número de fila (para reporte de errores)
     * @return DTO del producto
     */
    ProductoImportacionRequest parsearLineaCSV(String linea, int numeroFila);
}
