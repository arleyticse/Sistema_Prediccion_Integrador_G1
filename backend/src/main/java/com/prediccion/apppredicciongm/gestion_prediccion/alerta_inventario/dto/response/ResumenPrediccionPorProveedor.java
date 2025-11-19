package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para resumen de predicciones agrupadas por proveedor.
 * 
 * Estructura las predicciones y sus métricas organizadas por proveedor,
 * facilitando la visualización y análisis por proveedor.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPrediccionPorProveedor {
    
    /** ID del proveedor */
    private Long proveedorId;
    
    /** Nombre del proveedor */
    private String nombreProveedor;
    
    /** RUC del proveedor */
    private String rucProveedor;
    
    /** Contacto principal del proveedor */
    private String contactoProveedor;
    
    /** Email del proveedor */
    private String emailProveedor;
    
    /** Teléfono del proveedor */
    private String telefonoProveedor;
    
    /** Lista de predicciones de productos para este proveedor */
    private List<PrediccionProductoDTO> predicciones;
    
    /** Métricas agregadas del conjunto de predicciones */
    private MetricasAgregadasDTO metricas;
    
    /** Cantidad total de alertas procesadas para este proveedor */
    private Integer totalAlertas;
    
    /** Cantidad de predicciones exitosas */
    private Integer prediccionesExitosas;
    
    /** Cantidad de predicciones fallidas */
    private Integer prediccionesFallidas;
}
