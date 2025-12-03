package com.prediccion.apppredicciongm.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar códigos OTP de recuperación de contraseña.
 * 
 * Los códigos son de 6 dígitos y tienen una validez de 10 minutos por defecto.
 * Solo puede haber un código activo por usuario a la vez.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sin constraint UNIQUE - permite múltiples tokens por email (se limpian al crear uno nuevo)
    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    /**
     * Verifica si el código ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Verifica si el código ya fue utilizado
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Incrementa el contador de intentos fallidos
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Verifica si se alcanzó el máximo de intentos permitidos (3)
     */
    public boolean hasExceededMaxAttempts() {
        return attempts >= 3;
    }

    /**
     * Marca el código como utilizado
     */
    public void markAsUsed() {
        this.used = true;
    }
}
