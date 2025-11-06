package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.enums.EstadoPrediccion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO de respuesta para predicciones con métricas completas.
 * Contiene información detallada de la predicción generada.
 *
 * @author Sistema de Predicción
 * @version 2.0
 * @since 2025-11-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrediccionResponse {

    @JsonProperty("prediccionId")
    private Long prediccionId;

    @JsonProperty("productoId")
    private Integer productoId;

    @JsonProperty("productoNombre")
    private String productoNombre;

    @JsonProperty("demandaPredichaTotal")
    private Integer demandaPredichaTotal;

    @JsonProperty("algoritmo")
    private String algoritmo;

    @JsonProperty("horizonteTiempo")
    private Integer horizonteTiempo;

    @JsonProperty("precision")
    private Double precision; // Deprecated - usar mae

    @JsonProperty("fechaGeneracion")
    private LocalDateTime fechaGeneracion;

    @JsonProperty("vigenciaHasta")
    private LocalDateTime vigenciaHasta;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("detallePronostico")
    private String detallePronostico;

    // ===== NUEVOS CAMPOS EXTENDIDOS =====
    
    @JsonProperty("valoresPredichos")
    @Builder.Default
    private List<Double> valoresPredichos = new ArrayList<>();

    @JsonProperty("datosHistoricos")
    @Builder.Default
    private List<Double> datosHistoricos = new ArrayList<>();

    @JsonProperty("mae")
    private Double mae; // Mean Absolute Error

    @JsonProperty("mape")
    private Double mape; // Mean Absolute Percentage Error

    @JsonProperty("rmse")
    private Double rmse; // Root Mean Square Error

    @JsonProperty("calidadPrediccion")
    private String calidadPrediccion; // EXCELENTE, BUENA, ACEPTABLE, POBRE

    @JsonProperty("advertencias")
    @Builder.Default
    private List<String> advertencias = new ArrayList<>();

    @JsonProperty("recomendaciones")
    @Builder.Default
    private List<String> recomendaciones = new ArrayList<>();

    @JsonProperty("tieneTendencia")
    private Boolean tieneTendencia;

    @JsonProperty("tieneEstacionalidad")
    private Boolean tieneEstacionalidad;

    @JsonProperty("estado")
    private EstadoPrediccion estado;

    @JsonProperty("descripcionEstado")
    private String descripcionEstado;

    @JsonProperty("producto")
    private ProductoBasicoDTO producto; // Datos del producto para optimización

    /**
     * DTO interno con información básica del producto para optimización.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoBasicoDTO {
        private Integer productoId;
        private String nombre;
        private Double costoAdquisicion;
        private Double costoPedido;
        private Double costoMantenimientoAnual;
        private Integer diasLeadTime;
    }

    /**
     * Constructor simplificado para casos básicos (backward compatibility).
     */
    public PrediccionResponse(Long prediccionId, Integer productoId, String productoNombre,
                            Integer demandaPredichaTotal, String algoritmo, Double precision) {
        this.prediccionId = prediccionId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.demandaPredichaTotal = demandaPredichaTotal;
        this.algoritmo = algoritmo;
        this.precision = precision;
        this.mae = precision; // Backward compatibility
        this.fechaGeneracion = LocalDateTime.now();
        this.vigenciaHasta = LocalDateTime.now().plusDays(30);
        this.valoresPredichos = new ArrayList<>();
        this.datosHistoricos = new ArrayList<>();
        this.advertencias = new ArrayList<>();
        this.recomendaciones = new ArrayList<>();
        
        // Establecer estado por defecto
        this.estado = EstadoPrediccion.ACTIVA;
        this.descripcionEstado = EstadoPrediccion.ACTIVA.getDescripcion();
    }

    /**
     * Método helper para establecer el estado de la predicción automáticamente.
     * Se ejecuta después de mapear desde la entidad.
     */
    public void establecerEstado(boolean esFallida) {
        if (this.fechaGeneracion == null || this.horizonteTiempo == null) {
            this.estado = EstadoPrediccion.FALLIDA;
        } else {
            this.estado = EstadoPrediccion.determinar(this.fechaGeneracion, this.horizonteTiempo, esFallida);
        }
        this.descripcionEstado = this.estado.getDescripcion();
    }

    /**
     * Método helper para establecer el estado sin marcar como fallida.
     */
    public void establecerEstado() {
        establecerEstado(false);
    }
}
