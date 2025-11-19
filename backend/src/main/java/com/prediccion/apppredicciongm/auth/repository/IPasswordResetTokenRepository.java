package com.prediccion.apppredicciongm.auth.repository;

import com.prediccion.apppredicciongm.auth.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para gestionar tokens de recuperación de contraseña.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Repository
public interface IPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token activo por email y código
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.code = :code " +
           "AND t.used = false AND t.expiryDate > :now ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findValidToken(
            @Param("email") String email,
            @Param("code") String code,
            @Param("now") LocalDateTime now
    );

    /**
     * Busca el último token generado para un email (usado o no)
     */
    Optional<PasswordResetToken> findFirstByEmailOrderByCreatedAtDesc(String email);

    /**
     * Elimina todos los tokens expirados o usados de un email
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.email = :email " +
           "AND (t.used = true OR t.expiryDate < :now)")
    void deleteExpiredOrUsedTokens(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * Elimina todos los tokens de un email
     */
    void deleteByEmail(String email);

    /**
     * Verifica si existe un token válido para un email
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM PasswordResetToken t " +
           "WHERE t.email = :email AND t.used = false AND t.expiryDate > :now")
    boolean existsValidToken(@Param("email") String email, @Param("now") LocalDateTime now);
}
