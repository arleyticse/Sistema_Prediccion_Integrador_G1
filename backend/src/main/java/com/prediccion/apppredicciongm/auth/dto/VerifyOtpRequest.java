package com.prediccion.apppredicciongm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para verificar código OTP sin cambiar contraseña.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "El código es requerido")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe ser de 6 dígitos")
    private String code;
}
