package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para el proceso de importación de inventario.
 * 
 * <p>Contiene estadísticas detalladas del proceso de importación,
 * incluyendo registros exitosos, fallidos, errores específicos
 * y métricas de rendimiento.</p>
 * 
 * <h3>Información incluida:</h3>
 * <ul>
 *   <li>Estadísticas generales (total, exitosos, fallidos)</li>
 *   <li>Tasa de éxito del proceso</li>
 *   <li>Tiempo de procesamiento en milisegundos</li>
 *   <li>Detalle de errores por fila</li>
 *   <li>Estado final de la importación</li>
 * </ul>
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioImportacionResponse {

    /**
     * ID único de la importación registrada
     */
    private Long importacionId;

    /**
     * Nombre del archivo CSV importado
     */
    private String nombreArchivo;

    /**
     * Fecha y hora del proceso de importación
     */
    private LocalDateTime fechaProceso;

    /**
     * Total de registros procesados del CSV
     */
    private Integer totalRegistros;

    /**
     * Cantidad de registros importados exitosamente
     */
    private Integer registrosExitosos;

    /**
     * Cantidad de registros que fallaron
     */
    private Integer registrosFallidos;

    /**
     * Tasa de éxito del proceso (0-100%)
     */
    private Double tasaExito;

    /**
     * Tiempo total de procesamiento en milisegundos
     */
    private Long tiempoProcesamiento;

    /**
     * Estado final de la importación (COMPLETADA, COMPLETADA_CON_ERRORES, FALLIDA)
     */
    private String estado;

    /**
     * Lista detallada de errores por registro
     */
    @Builder.Default
    private List<DetalleErrorImportacion> erroresDetallados = new ArrayList<>();

    /**
     * Clase interna para representar errores individuales
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleErrorImportacion {
        /**
         * Número de fila en el CSV donde ocurrió el error
         */
        private Integer numeroFila;

        /**
         * Nombre del producto con error
         */
        private String nombreProducto;

        /**
         * Descripción detallada del error
         */
        private String descripcionError;
    }

    /**
     * Agrega un error de importación a la lista
     * 
     * @param fila Número de fila del CSV
     * @param nombreProducto Nombre del producto
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
     * Genera un resumen legible del proceso de importación
     * 
     * @return String con el resumen formateado
     */
    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importación de Inventario completada:\n");
        resumen.append(String.format("- Total registros: %d\n", totalRegistros));
        resumen.append(String.format("- Exitosos: %d\n", registrosExitosos));
        resumen.append(String.format("- Fallidos: %d\n", registrosFallidos));
        resumen.append(String.format("- Tasa de éxito: %.2f%%\n", tasaExito));
        resumen.append(String.format("- Tiempo: %d ms\n", tiempoProcesamiento));

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
