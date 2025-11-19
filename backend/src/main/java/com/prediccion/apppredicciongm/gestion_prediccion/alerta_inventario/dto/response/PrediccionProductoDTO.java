package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para representar la predicción de un producto individual.
 * 
 * Incluye datos históricos, valores predichos, métricas de calidad
 * y el horizonte usado para la predicción.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionProductoDTO {
    
    /** ID del producto */
    private Integer productoId;
    
    /** Nombre del producto */
    private String nombreProducto;
    
    /** Código del producto */
    private String codigoProducto;
    
    /** Código SKU generado dinámicamente (Formato: [INICIAL_CATEGORIA][0000][ID]) */
    private String codigoSKU;
    
    /** ID de la predicción generada */
    private Integer prediccionId;
    
    /** Valores históricos de demanda */
    private List<Double> valoresHistoricos;
    
    /** Valores predichos (demanda futura) */
    private List<Double> valoresPredichos;
    
    /** Fechas correspondientes a los valores históricos */
    private List<String> fechasHistoricas;
    
    /** Fechas correspondientes a los valores predichos */
    private List<String> fechasPredichas;
    
    /** Error Absoluto Medio (MAE) */
    private Double mae;
    
    /** Error Porcentual Absoluto Medio (MAPE) */
    private Double mape;
    
    /** Raíz del Error Cuadrático Medio (RMSE) */
    private Double rmse;
    
    /** Calidad de la predicción (EXCELENTE, BUENA, REGULAR, MALA) */
    private String calidadPrediccion;
    
    /** Horizonte de predicción usado (en días) */
    private Integer horizonteUsado;
    
    /** Algoritmo usado para la predicción */
    private String algoritmoUsado;
    
    /** Indica si la serie tiene tendencia detectada */
    private Boolean tieneTendencia;
    
    /** Indica si la serie tiene estacionalidad detectada */
    private Boolean tieneEstacionalidad;
    
    /** Cantidad óptima de pedido (EOQ) calculada */
    private Integer cantidadOptimaPedido;
    
    /** Punto de reorden (ROP) calculado */
    private Integer puntoReorden;
    
    /** Advertencias sobre la predicción */
    private List<String> advertencias;
    
    /** Recomendaciones basadas en la predicción */
    private List<String> recomendaciones;
}
