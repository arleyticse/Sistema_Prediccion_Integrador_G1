package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums;

/**
 * Enumeracion que define los estados posibles de una alerta de inventario.
 * Representa el ciclo de vida de una alerta desde su creacion hasta su resolucion.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public enum EstadoAlerta {
    
    /**
     * Estado inicial - La alerta ha sido generada pero no ha sido atendida.
     */
    PENDIENTE,
    
    /**
     * Estado en proceso - La alerta esta siendo atendida por un usuario.
     */
    EN_PROCESO,
    
    /**
     * Estado resuelto - La alerta ha sido atendida satisfactoriamente.
     * Por ejemplo, se genero una orden de compra o se ajusto el inventario.
     */
    RESUELTA,
    
    /**
     * Estado ignorado - La alerta fue revisada pero se decidio no tomar accion.
     */
    IGNORADA,
    
    /**
     * Estado escalado - La alerta requiere atencion de un nivel superior.
     */
    ESCALADA
}
