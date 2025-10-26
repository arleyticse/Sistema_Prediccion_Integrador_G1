package com.prediccion.apppredicciongm.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de autenticación.
 * 
 * Encapsula el token JWT y los datos del usuario autenticado.
 * Se envía como respuesta después de un login o registro exitoso.
 * 
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    
    /** Token JWT para autenticación en peticiones subsecuentes */
    private String token;
    
    /** Nombre completo del usuario autenticado */
    private String nombreCompleto;
    
    /** Email del usuario autenticado */
    private String email;
    
    /** Rol asignado al usuario */
    private String rol;
}
