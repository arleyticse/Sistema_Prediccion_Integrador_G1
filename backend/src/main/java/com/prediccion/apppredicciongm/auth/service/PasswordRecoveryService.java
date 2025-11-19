package com.prediccion.apppredicciongm.auth.service;

import com.prediccion.apppredicciongm.auth.dto.ForgotPasswordRequest;
import com.prediccion.apppredicciongm.auth.dto.ResetPasswordRequest;
import com.prediccion.apppredicciongm.auth.dto.VerifyOtpRequest;
import com.prediccion.apppredicciongm.auth.models.PasswordResetToken;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.auth.repository.IPasswordResetTokenRepository;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar recuperación de contraseñas mediante OTP.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryService {

    private final IPasswordResetTokenRepository tokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${resend.otp.expiration:10}")
    private int otpExpirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera y envía un código OTP al email del usuario
     */
    @Transactional
    public Map<String, Object> sendPasswordResetCode(ForgotPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail().toLowerCase().trim();

        try {
            // Verificar si el usuario existe
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese email"));

            // Verificar si ya existe un código válido reciente (prevenir spam)
            if (tokenRepository.existsValidToken(email, LocalDateTime.now())) {
                PasswordResetToken existingToken = tokenRepository
                        .findFirstByEmailOrderByCreatedAtDesc(email)
                        .orElse(null);
                
                if (existingToken != null && 
                    existingToken.getCreatedAt().plusMinutes(1).isAfter(LocalDateTime.now())) {
                    response.put("success", false);
                    response.put("message", "Ya se envió un código recientemente. Espera 1 minuto antes de solicitar otro.");
                    return response;
                }
            }

            // Eliminar tokens antiguos del email
            tokenRepository.deleteExpiredOrUsedTokens(email, LocalDateTime.now());

            // Generar nuevo código OTP
            String code = generateOtpCode();

            // Crear y guardar token
            PasswordResetToken token = PasswordResetToken.builder()
                    .email(email)
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .expiryDate(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                    .used(false)
                    .attempts(0)
                    .build();

            tokenRepository.save(token);

            // Enviar email
            boolean emailSent = emailService.sendPasswordResetEmail(
                    email, 
                    code, 
                    usuario.getNombre()
            );

            if (!emailSent) {
                response.put("success", false);
                response.put("message", "Error al enviar el correo. Intenta nuevamente.");
                return response;
            }

            log.info("Código de recuperación generado para: {}", email);

            response.put("success", true);
            response.put("message", "Código de verificación enviado al correo electrónico.");
            response.put("expiresIn", otpExpirationMinutes + " minutos");

        } catch (IllegalArgumentException e) {
            log.warn("Intento de recuperación para email inexistente: {}", email);
            // Por seguridad, no revelar si el email existe o no
            response.put("success", true);
            response.put("message", "Si el email existe, recibirás un código de verificación.");
            
        } catch (Exception e) {
            log.error("Error al procesar solicitud de recuperación para {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al procesar la solicitud. Intenta nuevamente.");
        }

        return response;
    }

    /**
     * Verifica el código OTP sin cambiar la contraseña
     */
    @Transactional
    public Map<String, Object> verifyOtpCode(VerifyOtpRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail().toLowerCase().trim();
        String code = request.getCode().trim();

        try {
            // Buscar token válido
            PasswordResetToken token = tokenRepository.findValidToken(email, code, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));

            // Verificar intentos
            if (token.hasExceededMaxAttempts()) {
                response.put("success", false);
                response.put("message", "Máximo de intentos alcanzado. Solicita un nuevo código.");
                tokenRepository.delete(token);
                return response;
            }

            // Verificar si ya fue usado
            if (token.isUsed()) {
                response.put("success", false);
                response.put("message", "Este código ya fue utilizado.");
                return response;
            }

            log.info("Código OTP verificado correctamente para: {}", email);

            response.put("success", true);
            response.put("message", "Código verificado correctamente.");
            response.put("valid", true);

        } catch (IllegalArgumentException e) {
            // Incrementar intentos fallidos
            tokenRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                    .ifPresent(token -> {
                        token.incrementAttempts();
                        tokenRepository.save(token);
                    });

            log.warn("Código OTP inválido para: {}", email);
            response.put("success", false);
            response.put("message", "Código inválido o expirado.");
            response.put("valid", false);

        } catch (Exception e) {
            log.error("Error al verificar código OTP para {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al verificar el código.");
        }

        return response;
    }

    /**
     * Verifica el código OTP y restablece la contraseña
     */
    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail().toLowerCase().trim();
        String code = request.getCode().trim();

        try {
            // Buscar token válido
            PasswordResetToken token = tokenRepository.findValidToken(email, code, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));

            // Verificar intentos
            if (token.hasExceededMaxAttempts()) {
                response.put("success", false);
                response.put("message", "Máximo de intentos alcanzado. Solicita un nuevo código.");
                tokenRepository.delete(token);
                return response;
            }

            // Verificar si ya fue usado
            if (token.isUsed()) {
                response.put("success", false);
                response.put("message", "Este código ya fue utilizado.");
                return response;
            }

            // Buscar usuario
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Actualizar contraseña
            usuario.setClaveHash(passwordEncoder.encode(request.getNewPassword()));
            usuarioRepository.save(usuario);

            // Marcar token como usado
            token.markAsUsed();
            tokenRepository.save(token);

            // Eliminar otros tokens del usuario
            tokenRepository.deleteExpiredOrUsedTokens(email, LocalDateTime.now().plusDays(1));

            log.info("Contraseña restablecida exitosamente para: {}", email);

            response.put("success", true);
            response.put("message", "Contraseña restablecida exitosamente.");

        } catch (IllegalArgumentException e) {
            // Incrementar intentos fallidos
            tokenRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                    .ifPresent(token -> {
                        token.incrementAttempts();
                        tokenRepository.save(token);
                    });

            log.warn("Error al restablecer contraseña para {}: {}", email, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());

        } catch (Exception e) {
            log.error("Error al restablecer contraseña para {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al restablecer la contraseña. Intenta nuevamente.");
        }

        return response;
    }

    /**
     * Genera un código OTP de 6 dígitos
     */
    private String generateOtpCode() {
        int code = RANDOM.nextInt(900000) + 100000; // Rango: 100000 - 999999
        return String.valueOf(code);
    }
}
