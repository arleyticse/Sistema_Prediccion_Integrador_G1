package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información completa de la empresa emisora de la orden.
 * Incluye datos legales, fiscales y de contacto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosEmpresaDTO {
    
    private String razonSocial;
    private String nombreComercial;
    private String ruc;
    
    private String direccion;
    private String ciudad;
    private String pais;
    private String codigoPostal;
    
    private String telefono;
    private String email;
    private String sitioWeb;
    
    private String actividadEconomica;
    private String regimenTributario;
}
