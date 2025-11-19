package com.prediccion.apppredicciongm.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Respuesta para alertas de inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaInventarioResponse {
    
    private Integer productoId;
    private String nombreProducto;
    private String sku;
    private String categoria;
    private String proveedor;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private Integer stockSugerido;
    private TipoAlerta tipoAlerta;
    private Integer diasCobertura;
    private Integer cantidadSugerida;
    private Double valorSugerido;
    private LocalDate fechaUltimaActualizacion;
    private String descripcionAlerta;
    private PrioridadAlerta prioridad;

    /**
     * Tipo de alerta de inventario
     */
    public enum TipoAlerta {
        STOCK_CRITICO,
        STOCK_BAJO,
        STOCK_AGOTADO,
        STOCK_EXCESIVO,
        REVISION_NECESARIA
    }

    /**
     * Prioridad de la alerta
     */
    public enum PrioridadAlerta {
        BAJA,
        MEDIA,
        ALTA,
        CRITICA
    }

    /**
     * Constructor para alertas básicas
     */
    public AlertaInventarioResponse(Integer productoId, String nombreProducto, 
                                  String sku, String proveedor, 
                                  Integer stockActual, Integer stockMinimo) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.sku = sku;
        this.proveedor = proveedor;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.fechaUltimaActualizacion = LocalDate.now();
        
        // Determinar tipo de alerta automáticamente
        if (stockActual <= 0) {
            this.tipoAlerta = TipoAlerta.STOCK_AGOTADO;
            this.prioridad = PrioridadAlerta.CRITICA;
        } else if (stockActual <= stockMinimo * 0.5) {
            this.tipoAlerta = TipoAlerta.STOCK_CRITICO;
            this.prioridad = PrioridadAlerta.ALTA;
        } else if (stockActual <= stockMinimo) {
            this.tipoAlerta = TipoAlerta.STOCK_BAJO;
            this.prioridad = PrioridadAlerta.MEDIA;
        } else {
            this.tipoAlerta = TipoAlerta.REVISION_NECESARIA;
            this.prioridad = PrioridadAlerta.BAJA;
        }
    }

    /**
     * Calcula la cantidad sugerida de pedido
     */
    public void calcularCantidadSugerida(Integer stockMaximo, Double costoPromedio) {
        if (stockMaximo != null && stockMaximo > stockActual) {
            this.cantidadSugerida = stockMaximo - stockActual;
            
            if (costoPromedio != null) {
                this.valorSugerido = this.cantidadSugerida * costoPromedio;
            }
        }
    }

    /**
     * Genera descripción automática de la alerta
     */
    public void generarDescripcion() {
        switch (this.tipoAlerta) {
            case STOCK_AGOTADO:
                this.descripcionAlerta = String.format("Producto %s (%s) SIN STOCK. Reabastecer inmediatamente.", 
                    nombreProducto, sku);
                break;
            case STOCK_CRITICO:
                this.descripcionAlerta = String.format("Producto %s (%s) en stock crítico: %d unidades. Mínimo: %d", 
                    nombreProducto, sku, stockActual, stockMinimo);
                break;
            case STOCK_BAJO:
                this.descripcionAlerta = String.format("Producto %s (%s) con stock bajo: %d unidades. Considerar reposición.", 
                    nombreProducto, sku, stockActual);
                break;
            default:
                this.descripcionAlerta = String.format("Producto %s (%s) requiere revisión de inventario.", 
                    nombreProducto, sku);
        }
    }
}