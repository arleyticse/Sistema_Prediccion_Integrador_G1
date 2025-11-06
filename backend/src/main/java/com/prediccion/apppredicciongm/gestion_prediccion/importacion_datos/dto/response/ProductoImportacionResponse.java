package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImportacionResponse {
    private Long importacionId;
    private String nombreArchivo;
    private LocalDateTime fechaProceso;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosFallidos;
    private Double tasaExito;
    private Long tiempoProcesamiento;
    private String estado;

    @Builder.Default
    private List<DetalleErrorImportacion> erroresDetallados = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleErrorImportacion {
        private Integer numeroFila;
        private String nombreProducto;
        private String descripcionError;
    }

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

    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importacion completada:\n");
        resumen.append(String.format("- Total registros: %d\n", totalRegistros));
        resumen.append(String.format("- Exitosos: %d\n", registrosExitosos));
        resumen.append(String.format("- Fallidos: %d\n", registrosFallidos));
        resumen.append(String.format("- Tasa de exito: %.2f%%\n", tasaExito));
        resumen.append(String.format("- Tiempo: %d ms\n", tiempoProcesamiento));
        
        if (registrosFallidos > 0) {
            resumen.append("\nPrimeros errores:\n");
            erroresDetallados.stream()
                    .limit(5)
                    .forEach(error -> resumen.append(String.format("  Fila %d: %s\n", 
                            error.getNumeroFila(), error.getDescripcionError())));
        }
        
        return resumen.toString();
    }
}
