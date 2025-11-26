package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario del sistema.
 * 
 * Almacena la información de autenticación y autorización de los usuarios
 * que acceden a la aplicación. La contraseña se almacena hasheada usando BCrypt.
 * 
 * Incluye campos de seguridad para:
 * - Gestión de sesiones activas
 * - Control de intentos fallidos de login
 * - Bloqueo temporal de cuentas
 * 
 * @version 1.1
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
    
    /** Última vez que el usuario tuvo actividad (para expiración de sesión) */
    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;
    
    /** Número de intentos fallidos de login consecutivos */
    @Column(name = "intentos_fallidos", columnDefinition = "integer default 0")
    private Integer intentosFallidos = 0;
    
    /** Fecha/hora en que la cuenta fue bloqueada */
    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;
    
    /** Indica si la cuenta está bloqueada por exceso de intentos */
    @Column(name = "cuenta_bloqueada", columnDefinition = "boolean default false")
    private Boolean cuentaBloqueada = false;
    
    /**
     * Verifica si la cuenta está actualmente bloqueada
     */
    public boolean estaBloqueada() {
        return Boolean.TRUE.equals(cuentaBloqueada);
    }
    
    /**
     * Incrementa el contador de intentos fallidos
     */
    public void incrementarIntentosFallidos() {
        this.intentosFallidos = (this.intentosFallidos == null ? 0 : this.intentosFallidos) + 1;
    }
    
    /**
     * Reinicia el contador de intentos fallidos (después de login exitoso)
     */
    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
    }
    
    /**
     * Bloquea la cuenta del usuario
     */
    public void bloquearCuenta() {
        this.cuentaBloqueada = true;
        this.fechaBloqueo = LocalDateTime.now();
    }
    
    /**
     * Desbloquea la cuenta del usuario
     */
    public void desbloquearCuenta() {
        this.cuentaBloqueada = false;
        this.fechaBloqueo = null;
        this.intentosFallidos = 0;
    }
    
    /**
     * Actualiza la última actividad del usuario
     */
    public void actualizarActividad() {
        this.ultimaActividad = LocalDateTime.now();
    }
}
