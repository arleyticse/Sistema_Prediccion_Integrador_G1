package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para el proceso de importación de Kardex.
 * 
 * <p>Contiene estadísticas completas del proceso de importación incluyendo:</p>
 * <ul>
 *   <li>Identificador único de la importación</li>
 *   <li>Información del archivo procesado</li>
 *   <li>Estadísticas de éxito y errores</li>
 *   <li>Detalles específicos de cada error encontrado</li>
 *   <li>Métricas de rendimiento</li>
 * </ul>
 * 
 * <h3>Uso típico:</h3>
 * <pre>
 * KardexImportacionResponse response = kardexImportacionService.importarKardexDesdeCSV(archivo, usuarioId);
 * System.out.println("Tasa de éxito: " + response.getTasaExito() + "%");
 * response.getErroresDetallados().forEach(error -&gt; 
 *     System.out.println("Fila " + error.getNumeroFila() + ": " + error.getDescripcionError())
 * );
 * </pre>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KardexImportacionResponse {

    /**
     * ID único del registro de importación en la tabla de auditoría
     */
    private Long importacionId;

    /**
     * Nombre del archivo CSV procesado
     */
    private String nombreArchivo;

    /**
     * Fecha y hora en que se procesó la importación
     */
    private LocalDateTime fechaProceso;

    /**
     * Total de registros encontrados en el CSV (sin contar header)
     */
    private Integer totalRegistros;

    /**
     * Cantidad de registros importados exitosamente
     */
    private Integer registrosExitosos;

    /**
     * Cantidad de registros que fallaron en la importación
     */
    private Integer registrosFallidos;

    /**
     * Porcentaje de éxito de la importación (0-100)
     * Calculado como: (registrosExitosos / totalRegistros) * 100
     */
    private Double tasaExito;

    /**
     * Tiempo total de procesamiento en milisegundos
     */
    private Long tiempoProcesamiento;

    /**
     * Estado final de la importación:
     * - COMPLETADA: Todos los registros importados sin errores
     * - COMPLETADA_CON_ERRORES: Algunos registros fallaron
     * - FALLIDA: Todos los registros fallaron o error crítico
     */
    private String estado;

    /**
     * Lista detallada de errores encontrados durante la importación
     */
    @Builder.Default
    private List<DetalleErrorImportacion> erroresDetallados = new ArrayList<>();

    /**
     * Clase interna para representar un error específico de importación.
     * 
     * <p>Contiene la información necesaria para identificar y corregir el error:</p>
     * <ul>
     *   <li>Número de fila donde ocurrió el error</li>
     *   <li>Nombre del producto afectado</li>
     *   <li>Descripción detallada del error</li>
     * </ul>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleErrorImportacion {
        /**
         * Número de fila en el CSV (línea del archivo, comenzando en 2 después del header)
         */
        private Integer numeroFila;

        /**
         * Nombre del producto relacionado con el error
         */
        private String nombreProducto;

        /**
         * Descripción detallada del error encontrado
         */
        private String descripcionError;
    }

    /**
     * Agrega un nuevo error a la lista de errores detallados.
     * 
     * @param fila Número de fila donde ocurrió el error
     * @param nombreProducto Nombre del producto relacionado
     * @param error Descripción del error
     */
    public void agregarError(Integer fila, String nombreProducto, String error) {
        if (erroresDetallados == null) {
            erroresDetallados = new ArrayList<>();
        }
        erroresDetallados.add(DetalleErrorImportacion.builder()
                .numeroFila(fila)
                .nombreProducto(nombreProducto)
                .descripcionError(error)
                .build());
    }

    /**
     * Genera un resumen legible en texto de la importación.
     * 
     * <p>Útil para logs, reportes o notificaciones al usuario.</p>
     * 
     * @return String con resumen formateado de la importación
     */
    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importación de Kardex completada:\n");
        resumen.append(String.format("- Total registros: %d\n", totalRegistros));
        resumen.append(String.format("- Exitosos: %d\n", registrosExitosos));
        resumen.append(String.format("- Fallidos: %d\n", registrosFallidos));
        resumen.append(String.format("- Tasa de éxito: %.2f%%\n", tasaExito));
        resumen.append(String.format("- Tiempo: %d ms\n", tiempoProcesamiento));
        resumen.append(String.format("- Estado: %s\n", estado));

        if (registrosFallidos > 0) {
            resumen.append("\nPrimeros errores:\n");
            erroresDetallados.stream()
                    .limit(5)
                    .forEach(error -> resumen.append(String.format(
                            "  Fila %d (%s): %s\n",
                            error.getNumeroFila(),
                            error.getNombreProducto(),
                            error.getDescripcionError()
                    )));
        }

        return resumen.toString();
    }
}
