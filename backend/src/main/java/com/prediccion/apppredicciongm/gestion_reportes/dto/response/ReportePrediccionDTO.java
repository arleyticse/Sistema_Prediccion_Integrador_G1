package com.prediccion.apppredicciongm.gestion_reportes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportePrediccionDTO {

    private ResumenGeneralPrediccion resumenGeneral;
    private List<PrediccionDetalle> predicciones;
    private EstadisticasPrediccion estadisticas;
    private List<ProductoConPrediccion> topProductos;
    private LocalDateTime fechaGeneracion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenGeneralPrediccion {
        private Integer totalPredicciones;
        private Integer prediccionesExcelentes;
        private Integer prediccionesBuenas;
        private Integer prediccionesRegulares;
        private Integer prediccionesMalas;
        private Double porcentajeExito;
        private String periodoAnalisis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrediccionDetalle {
        private Integer prediccionId;
        private String nombreProducto;
        private String codigoProducto;
        private String categoria;
        private String algoritmoUsado;
        private LocalDateTime fechaEjecucion;
        private Integer horizonteTiempo;
        private Integer demandaPredichaTotal;
        private BigDecimal mape;
        private BigDecimal rmse;
        private BigDecimal mae;
        private BigDecimal r2;
        private String nivelPrecision;
        private String nombreUsuario;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasPrediccion {
        private Double mapePromedio;
        private Double rmsePromedio;
        private Double maePromedio;
        private Double r2Promedio;
        private String algoritmoMasUsado;
        private Integer cantidadPorAlgoritmo;
        private Integer demandaTotalPredicha;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoConPrediccion {
        private String nombreProducto;
        private String codigoProducto;
        private Integer cantidadPredicciones;
        private BigDecimal mapePromedio;
        private Integer demandaTotalPredicha;
        private String categoria;
    }
}
