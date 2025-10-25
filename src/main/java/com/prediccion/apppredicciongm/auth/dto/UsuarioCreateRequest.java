package com.prediccion.apppredicciongm.auth.dto;

import com.prediccion.apppredicciongm.enums.Roles;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsuarioCreateRequest {
    private Integer usuarioId;
    private String nombre;
    private String claveHash;
    @Enumerated(EnumType.STRING)
    private Roles rol;
    @Email(message = "El correo electrónico no es válido")
    private String email;
}
