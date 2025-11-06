package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para operaciones de importación de estacionalidad.
 * <p>
 * Contiene estadísticas del proceso de importación, información sobre
 * registros procesados exitosamente, errores encontrados y métricas de rendimiento.
 * </p>
 * 
 * <h3>Información Incluida:</h3>
 * <ul>
 *   <li>Identificador de la importación</li>
 *   <li>Nombre del archivo procesado</li>
 *   <li>Fecha y hora del proceso</li>
 *   <li>Estadísticas de registros (total, exitosos, fallidos)</li>
 *   <li>Tasa de éxito del proceso</li>
 *   <li>Tiempo de procesamiento</li>
 *   <li>Estado final de la importación</li>
 *   <li>Detalle de errores por registro</li>
 * </ul>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionalidadImportacionResponse {

    /**
     * Identificador único de la importación en base de datos.
     */
    private Long importacionId;

    /**
     * Nombre del archivo CSV importado.
     */
    private String nombreArchivo;

    /**
     * Fecha y hora en que se procesó la importación.
     */
    private LocalDateTime fechaProceso;

    /**
     * Total de registros encontrados en el archivo CSV.
     */
    private Integer totalRegistros;

    /**
     * Cantidad de registros procesados exitosamente.
     */
    private Integer registrosExitosos;

    /**
     * Cantidad de registros que fallaron en el proceso.
     */
    private Integer registrosFallidos;

    /**
     * Porcentaje de éxito del proceso de importación.
     * <p>Calculado como: (registrosExitosos / totalRegistros) * 100</p>
     */
    private Double tasaExito;

    /**
     * Tiempo total de procesamiento en milisegundos.
     */
    private Long tiempoProcesamiento;

    /**
     * Estado final de la importación.
     * <p>Valores posibles: COMPLETADA, COMPLETADA_CON_ERRORES, FALLIDA</p>
     */
    private String estado;

    /**
     * Lista detallada de errores encontrados durante la importación.
     * <p>Cada error incluye número de fila, producto-mes y descripción del error.</p>
     */
    @Builder.Default
    private List<DetalleErrorImportacion> erroresDetallados = new ArrayList<>();

    /**
     * Clase interna que representa el detalle de un error en un registro específico.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleErrorImportacion {
        /**
         * Número de fila en el CSV donde ocurrió el error.
         */
        private Integer numeroFila;

        /**
         * Identificador del producto y mes que causó el error.
         * <p>Formato: "Producto - Mes"</p>
         */
        private String productoMes;

        /**
         * Descripción detallada del error encontrado.
         */
        private String descripcionError;
    }

    /**
     * Agrega un error a la lista de errores detallados.
     * <p>
     * Útil para construir la respuesta incrementalmente durante el proceso
     * de importación.
     * </p>
     * 
     * @param fila         número de fila donde ocurrió el error
     * @param productoMes  identificador producto-mes con error
     * @param error        descripción del error
     */
    public void agregarError(Integer fila, String productoMes, String error) {
        if (erroresDetallados == null) {
            erroresDetallados = new ArrayList<>();
        }
        erroresDetallados.add(
            DetalleErrorImportacion.builder()
                .numeroFila(fila)
                .productoMes(productoMes)
                .descripcionError(error)
                .build()
        );
    }

    /**
     * Genera un resumen legible del proceso de importación.
     * <p>
     * Incluye estadísticas principales y muestra hasta 5 primeros errores
     * si los hay.
     * </p>
     * 
     * @return String con el resumen formateado
     */
    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== Importación de Estacionalidad Completada ===\n");
        resumen.append(String.format("Archivo: %s\n", nombreArchivo));
        resumen.append(String.format("Fecha: %s\n", fechaProceso));
        resumen.append("\n--- Estadísticas ---\n");
        resumen.append(String.format("Total registros: %d\n", totalRegistros));
        resumen.append(String.format("✓ Exitosos: %d\n", registrosExitosos));
        resumen.append(String.format("✗ Fallidos: %d\n", registrosFallidos));
        resumen.append(String.format("Tasa de éxito: %.2f%%\n", tasaExito));
        resumen.append(String.format("Tiempo de proceso: %d ms\n", tiempoProcesamiento));
        resumen.append(String.format("Estado final: %s\n", estado));

        if (registrosFallidos > 0 && erroresDetallados != null && !erroresDetallados.isEmpty()) {
            resumen.append("\n--- Primeros Errores ---\n");
            erroresDetallados.stream()
                .limit(5)
                .forEach(error -> resumen.append(String.format(
                    "  Fila %d (%s): %s\n", 
                    error.getNumeroFila(), 
                    error.getProductoMes() != null ? error.getProductoMes() : "N/A",
                    error.getDescripcionError()
                )));
            
            if (erroresDetallados.size() > 5) {
                resumen.append(String.format("  ... y %d errores más\n", 
                    erroresDetallados.size() - 5));
            }
        }

        return resumen.toString();
    }
}
