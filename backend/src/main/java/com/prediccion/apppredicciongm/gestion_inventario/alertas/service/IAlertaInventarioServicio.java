package com.prediccion.apppredicciongm.gestion_inventario.alertas.service;

import com.prediccion.apppredicciongm.models.AlertaInventarioResponse;

import java.util.List;

/**
 * Interfaz para el servicio de alertas de inventario
 */
public interface IAlertaInventarioServicio {
    
    /**
     * Obtiene todas las alertas críticas de inventario
     * @return Lista de alertas críticas
     */
    List<AlertaInventarioResponse> obtenerAlertasCriticas();
    
    /**
     * Obtiene alertas de inventario por proveedor
     * @param proveedor Nombre del proveedor
     * @return Lista de alertas del proveedor
     */
    List<AlertaInventarioResponse> obtenerAlertasPorProveedor(String proveedor);
    
    /**
     * Obtiene todas las alertas de inventario
     * @return Lista completa de alertas
     */
    List<AlertaInventarioResponse> obtenerTodasLasAlertas();
    
    /**
     * Verifica si un producto necesita reposición
     * @param productoId ID del producto
     * @return true si necesita reposición
     */
    boolean necesitaReposicion(Integer productoId);
}