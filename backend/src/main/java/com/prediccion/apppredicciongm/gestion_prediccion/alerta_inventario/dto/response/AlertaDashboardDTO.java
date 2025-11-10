package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO optimizado para el dashboard de alertas.
 * 
 * Contiene solo los campos necesarios para mostrar alertas agrupadas
 * por proveedor en el frontend.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDashboardDTO {

    /**
     * ID unico de la alerta.
     */
    private Long alertaId;

    /**
     * Tipo de alerta.
     */
    private String tipoAlerta;

    /**
     * Nivel de criticidad.
     */
    private String nivelCriticidad;

    /**
     * Mensaje de la alerta.
     */
    private String mensaje;

    /**
     * ID del producto.
     */
    private Integer productoId;

    /**
     * Nombre del producto.
     */
    private String productoNombre;

    /**
     * Categoria del producto.
     */
    private String productoCategoria;

    /**
     * Stock actual.
     */
    private Integer stockActual;

    /**
     * Stock minimo.
     */
    private Integer stockMinimo;

    /**
     * Cantidad sugerida para reorden.
     */
    private Integer cantidadSugerida;

    /**
     * Fecha de generacion de la alerta.
     */
    private LocalDateTime fechaGeneracion;

    /**
     * ID del proveedor principal.
     */
    private Integer proveedorId;

    /**
     * Nombre comercial del proveedor.
     */
    private String proveedorNombreComercial;

    /**
     * Tiempo de entrega del proveedor en dias.
     */
    private Integer proveedorTiempoEntrega;
    
    /**
     * Costo de adquisicion del producto.
     * Usado para calcular el total estimado por proveedor.
     */
    private java.math.BigDecimal costoAdquisicion;
    
    /**
     * Estado de la alerta.
     */
    private String estado;
}
