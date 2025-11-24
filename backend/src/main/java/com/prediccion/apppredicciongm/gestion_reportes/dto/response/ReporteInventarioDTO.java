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
public class ReporteInventarioDTO {

    private ResumenGeneralInventario resumenGeneral;
    private List<InventarioDetalle> inventarios;
    private EstadisticasInventario estadisticas;
    private List<ProductoCritico> productosCriticos;
    private ValoracionInventario valoracion;
    private LocalDateTime fechaGeneracion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenGeneralInventario {
        private Integer totalProductos;
        private Integer productosConStock;
        private Integer productosSinStock;
        private Integer productosBajoMinimo;
        private Integer productosEnReorden;
        private Integer productosObsoletos;
        private String periodoAnalisis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventarioDetalle {
        private Integer inventarioId;
        private String nombreProducto;
        private String codigoProducto;
        private String categoria;
        private Integer stockDisponible;
        private Integer stockReservado;
        private Integer stockEnTransito;
        private Integer stockTotal;
        private Integer stockMinimo;
        private Integer stockMaximo;
        private Integer puntoReorden;
        private String ubicacionAlmacen;
        private LocalDateTime fechaUltimoMovimiento;
        private Integer diasSinVenta;
        private String estado;
        private BigDecimal precioUnitario;
        private BigDecimal valorStock;
        private Double rotacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasInventario {
        private Integer stockTotalGeneral;
        private Integer stockDisponibleTotal;
        private Integer stockReservadoTotal;
        private Integer stockEnTransitoTotal;
        private Double rotacionPromedio;
        private Integer diasPromedioSinVenta;
        private String categoriaConMasStock;
        private String categoriaConMenosStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoCritico {
        private String nombreProducto;
        private String codigoProducto;
        private Integer stockDisponible;
        private Integer stockMinimo;
        private Integer diasSinVenta;
        private String nivelCriticidad;
        private String razon;
        private String categoria;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValoracionInventario {
        private BigDecimal valorTotalInventario;
        private BigDecimal valorStockDisponible;
        private BigDecimal valorStockReservado;
        private BigDecimal valorStockEnTransito;
        private BigDecimal valorPromedioPorProducto;
        private String categoriaConMayorValor;
        private BigDecimal valorCategoriaMaxima;
    }
}
