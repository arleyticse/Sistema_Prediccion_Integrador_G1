package com.prediccion.apppredicciongm.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de autenticación.
 * 
 * Encapsula las credenciales de un usuario que desea iniciar sesión.
 * 
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    
    /** Email del usuario */
    private String email;
    
    /** Contraseña en texto plano */
    private String clave;
}
