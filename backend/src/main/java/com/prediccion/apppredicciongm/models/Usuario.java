package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad que representa un usuario del sistema.
 * 
 * Almacena la información de autenticación y autorización de los usuarios
 * que acceden a la aplicación. La contraseña se almacena hasheada usando BCrypt.
 * 
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Usuario implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** ID único del usuario (generado automáticamente) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer usuarioId;

    /** Nombre completo del usuario */
    private String nombre;

    /** Contraseña hasheada usando BCrypt */
    @Column(name = "clave_hash")
    private String claveHash;

    /** Rol del usuario (GERENTE o OPERARIO) */
    private String rol;

    /** Email único del usuario (usado para autenticación) */
    private String email;

    /** Indica si el usuario tiene una sesión activa */
    @Column(columnDefinition = "boolean default false")
    private Boolean activo = false;
}
