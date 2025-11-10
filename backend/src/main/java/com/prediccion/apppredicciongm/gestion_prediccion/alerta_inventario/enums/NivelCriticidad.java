package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums;

/**
 * Enumeracion que define los niveles de criticidad para las alertas de inventario.
 * Permite priorizar las alertas segun su urgencia e impacto en las operaciones.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public enum NivelCriticidad {
    
    /**
     * Nivel de criticidad baja - Situacion que requiere atencion pero no es urgente.
     * Tiempo de respuesta sugerido: 7 dias.
     */
    BAJA,
    
    /**
     * Nivel de criticidad media - Situacion que requiere atencion en el corto plazo.
     * Tiempo de respuesta sugerido: 3 dias.
     */
    MEDIA,
    
    /**
     * Nivel de criticidad alta - Situacion que requiere atencion prioritaria.
     * Tiempo de respuesta sugerido: 1 dia.
     */
    ALTA,
    
    /**
     * Nivel de criticidad critica - Situacion que requiere atencion inmediata.
     * Tiempo de respuesta sugerido: Inmediato (mismo dia).
     */
    CRITICA
}
