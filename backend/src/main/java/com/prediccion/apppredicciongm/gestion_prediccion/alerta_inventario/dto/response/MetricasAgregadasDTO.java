package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para métricas agregadas de un conjunto de predicciones.
 * 
 * Proporciona un resumen estadístico de la calidad de las predicciones
 * para un proveedor o grupo de productos.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasAgregadasDTO {
    
    /** Error Absoluto Medio promedio de todas las predicciones */
    private Double maePromedio;
    
    /** Error Porcentual Absoluto Medio promedio */
    private Double mapePromedio;
    
    /** Raíz del Error Cuadrático Medio promedio */
    private Double rmsePromedio;
    
    /** Calidad general del conjunto de predicciones */
    private String calidadGeneral;
    
    /** Total de productos procesados */
    private Integer totalProductos;
    
    /** Cantidad de predicciones excelentes (MAPE < 10%) */
    private Integer prediccionesExcelentes;
    
    /** Cantidad de predicciones buenas (MAPE 10-20%) */
    private Integer prediccionesBuenas;
    
    /** Cantidad de predicciones regulares (MAPE 20-50%) */
    private Integer prediccionesRegulares;
    
    /** Cantidad de predicciones malas (MAPE > 50%) */
    private Integer prediccionesMalas;
    
    /** Porcentaje de predicciones con calidad aceptable (≤20%) */
    private Double porcentajeAceptable;
}
