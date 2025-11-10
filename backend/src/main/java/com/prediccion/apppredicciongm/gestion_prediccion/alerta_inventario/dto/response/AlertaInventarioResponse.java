package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para AlertaInventario.
 * 
 * Contiene la informacion de una alerta de inventario que se envia al frontend,
 * incluyendo datos basicos del producto y proveedor asociados.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaInventarioResponse {

    /**
     * ID unico de la alerta.
     */
    private Long alertaId;

    /**
     * Tipo de alerta generada.
     */
    private String tipoAlerta;

    /**
     * Nivel de criticidad de la alerta.
     */
    private String nivelCriticidad;

    /**
     * Mensaje descriptivo de la alerta.
     */
    private String mensaje;

    /**
     * Informacion basica del producto asociado.
     */
    private ProductoBasicoDTO producto;

    /**
     * Stock actual del producto en el momento de generar la alerta.
     */
    private Integer stockActual;

    /**
     * Stock minimo configurado para el producto.
     */
    private Integer stockMinimo;

    /**
     * Cantidad sugerida para reorden.
     */
    private Integer cantidadSugerida;

    /**
     * Estado actual de la alerta.
     */
    private String estado;

    /**
     * Fecha y hora en que se genero la alerta.
     */
    private LocalDateTime fechaGeneracion;

    /**
     * Fecha y hora en que se resolvio la alerta.
     */
    private LocalDateTime fechaResolucion;

    /**
     * Accion tomada para resolver la alerta.
     */
    private String accionTomada;

    /**
     * Observaciones adicionales sobre la alerta.
     */
    private String observaciones;

    /**
     * ID del usuario asignado a la alerta.
     */
    private Integer usuarioAsignadoId;

    /**
     * Nombre del usuario asignado a la alerta.
     */
    private String usuarioAsignadoNombre;
}
