package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información completa del proveedor destinatario de la orden.
 * Incluye datos de contacto, condiciones comerciales y plazos de entrega.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosProveedorDTO {

    private Long proveedorId;
    private String razonSocial;
    private String nombreComercial;
    private String rucNit;

    private String direccion;
    private String ciudad;
    private String pais;

    private String telefono;
    private String email;
    private String personaContacto;
    private String cargoContacto;

    private Integer tiempoEntregaDias;
    private Integer diasCredito;
    private String condicionPago;

    private Double calificacion;
    private String observaciones;
}
