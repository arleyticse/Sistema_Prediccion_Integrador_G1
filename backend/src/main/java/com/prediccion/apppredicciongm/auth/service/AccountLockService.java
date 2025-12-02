package com.prediccion.apppredicciongm.auth.service;

import com.prediccion.apppredicciongm.auth.dto.ForgotPasswordRequest;
import com.prediccion.apppredicciongm.auth.dto.VerifyOtpRequest;
import com.prediccion.apppredicciongm.auth.models.PasswordResetToken;
import com.prediccion.apppredicciongm.auth.repository.IPasswordResetTokenRepository;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.models.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar el bloqueo y desbloqueo de cuentas de usuario.
 * 
 * Funcionalidades:
 * - Registrar intentos fallidos de login
 * - Bloquear cuentas después de N intentos fallidos
 * - Desbloquear cuentas mediante código OTP enviado por email
 * - Expirar sesiones inactivas automáticamente
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockService {

    private final IUsuarioRepository usuarioRepository;
    private final IPasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${security.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    @Value("${security.sesion-timeout-minutos:30}")
    private int sesionTimeoutMinutos;

    @Value("${otp.expiration.minutes:10}")
    private int otpExpirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Registra un intento fallido de login.
     * Si se alcanza el máximo de intentos, bloquea la cuenta.
     * 
     * @param email Email del usuario
     * @return true si la cuenta fue bloqueada, false si aún tiene intentos
     */
    @Transactional
    public boolean registrarIntentoFallido(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    usuario.incrementarIntentosFallidos();
                    
                    int intentosRestantes = maxIntentosFallidos - usuario.getIntentosFallidos();
                    
                    if (usuario.getIntentosFallidos() >= maxIntentosFallidos) {
                        usuario.bloquearCuenta();
                        usuarioRepository.save(usuario);
                        log.warn("[SEGURIDAD] Cuenta bloqueada por exceso de intentos: {}", email);
                        return true;
                    }
                    
                    usuarioRepository.save(usuario);
                    log.warn("[SEGURIDAD] Intento fallido #{} para: {} ({} restantes)", 
                            usuario.getIntentosFallidos(), email, intentosRestantes);
                    return false;
                })
                .orElse(false);
    }

    /**
     * Obtiene el número de intentos restantes antes del bloqueo.
     * 
     * @param email Email del usuario
     * @return Intentos restantes o -1 si usuario no existe
     */
    public int obtenerIntentosRestantes(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> maxIntentosFallidos - 
                        (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()))
                .orElse(-1);
    }

    /**
     * Reinicia el contador de intentos fallidos después de login exitoso.
     * 
     * @param email Email del usuario
     */
    @Transactional
    public void reiniciarIntentosFallidos(String email) {
        usuarioRepository.findByEmail(email)
                .ifPresent(usuario -> {
                    usuario.reiniciarIntentosFallidos();
                    usuario.actualizarActividad();
                    usuarioRepository.save(usuario);
                    log.debug("[SEGURIDAD] Intentos reiniciados para: {}", email);
                });
    }

    /**
     * Verifica si una cuenta está bloqueada.
     * 
     * @param email Email del usuario
     * @return true si está bloqueada, false en caso contrario
     */
    public boolean estaCuentaBloqueada(String email) {
        return usuarioRepository.findByEmail(email)
                .map(Usuario::estaBloqueada)
                .orElse(false);
    }

    /**
     * Genera y envía un código OTP para desbloquear la cuenta.
     * 
     * @param request Solicitud con el email del usuario
     * @return Mapa con resultado de la operación
     */
    @Transactional
    public Map<String, Object> solicitarDesbloqueo(ForgotPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail().toLowerCase().trim();

        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            if (!usuario.estaBloqueada()) {
                response.put("success", false);
                response.put("message", "La cuenta no está bloqueada.");
                return response;
            }

            // Verificar si ya existe un código válido reciente
            if (tokenRepository.existsValidToken(email, LocalDateTime.now())) {
                PasswordResetToken existingToken = tokenRepository
                        .findFirstByEmailOrderByCreatedAtDesc(email)
                        .orElse(null);

                if (existingToken != null &&
                        existingToken.getCreatedAt().plusMinutes(1).isAfter(LocalDateTime.now())) {
                    response.put("success", false);
                    response.put("message", "Ya se envió un código. Espera 1 minuto.");
                    return response;
                }
            }

            // Eliminar tokens antiguos
            tokenRepository.deleteExpiredOrUsedTokens(email, LocalDateTime.now());

            // Generar código OTP
            String code = generateOtpCode();

            PasswordResetToken token = PasswordResetToken.builder()
                    .email(email)
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .expiryDate(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                    .used(false)
                    .attempts(0)
                    .build();

            tokenRepository.save(token);

            // Enviar email de desbloqueo
            boolean emailSent = emailService.sendAccountUnlockEmail(
                    email, code, usuario.getNombre());

            if (!emailSent) {
                response.put("success", false);
                response.put("message", "Error al enviar el correo.");
                return response;
            }

            log.info("[SEGURIDAD] Código de desbloqueo enviado a: {}", email);

            response.put("success", true);
            response.put("message", "Código de desbloqueo enviado al correo.");
            response.put("expiresIn", otpExpirationMinutes + " minutos");

        } catch (IllegalArgumentException e) {
            response.put("success", true);
            response.put("message", "Si el email existe, recibirás un código.");
        } catch (Exception e) {
            log.error("[SEGURIDAD] Error al solicitar desbloqueo: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al procesar la solicitud.");
        }

        return response;
    }

    /**
     * Verifica el código OTP y desbloquea la cuenta.
     * 
     * @param request Solicitud con email y código OTP
     * @return Mapa con resultado de la operación
     */
    @Transactional
    public Map<String, Object> desbloquearCuenta(VerifyOtpRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.getEmail().toLowerCase().trim();
        String code = request.getCode().trim();

        try {
            PasswordResetToken token = tokenRepository
                    .findValidToken(email, code, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));

            if (token.hasExceededMaxAttempts()) {
                response.put("success", false);
                response.put("message", "Máximo de intentos alcanzado. Solicita nuevo código.");
                tokenRepository.delete(token);
                return response;
            }

            if (token.isUsed()) {
                response.put("success", false);
                response.put("message", "Este código ya fue utilizado.");
                return response;
            }

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Desbloquear cuenta
            usuario.desbloquearCuenta();
            usuarioRepository.save(usuario);

            // Marcar token como usado
            token.setUsed(true);
            tokenRepository.save(token);

            log.info("[SEGURIDAD] Cuenta desbloqueada exitosamente: {}", email);

            response.put("success", true);
            response.put("message", "Cuenta desbloqueada exitosamente. Ya puedes iniciar sesión.");

        } catch (IllegalArgumentException e) {
            tokenRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                    .ifPresent(token -> {
                        token.incrementAttempts();
                        tokenRepository.save(token);
                    });

            log.warn("[SEGURIDAD] Código de desbloqueo inválido para: {}", email);
            response.put("success", false);
            response.put("message", "Código inválido o expirado.");

        } catch (Exception e) {
            log.error("[SEGURIDAD] Error al desbloquear cuenta: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al procesar la solicitud.");
        }

        return response;
    }

    /**
     * Actualiza la última actividad del usuario (para gestión de sesiones).
     * 
     * @param email Email del usuario
     */
    @Transactional
    public void actualizarActividad(String email) {
        usuarioRepository.findByEmail(email)
                .ifPresent(usuario -> {
                    usuario.actualizarActividad();
                    usuarioRepository.save(usuario);
                });
    }

    /**
     * Job programado: Expira sesiones inactivas cada 5 minutos.
     * Libera el flag "activo" de usuarios sin actividad reciente.
     */
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    @Transactional
    public void expirarSesionesInactivas() {
        LocalDateTime umbralInactividad = LocalDateTime.now().minusMinutes(sesionTimeoutMinutos);

        List<Usuario> usuariosInactivos = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> u.getUltimaActividad() == null || 
                            u.getUltimaActividad().isBefore(umbralInactividad))
                .toList();

        if (!usuariosInactivos.isEmpty()) {
            for (Usuario usuario : usuariosInactivos) {
                usuario.setActivo(false);
                usuarioRepository.save(usuario);
                log.info("[SESION] Sesión expirada por inactividad: {}", usuario.getEmail());
            }
            log.info("[SESION] {} sesiones expiradas por inactividad", usuariosInactivos.size());
        }
    }

        /**
         * Verifica si el usuario tiene una sesión activa reciente (no expirada por inactividad).
         * Se considera activa si el flag `activo` es true y la `ultimaActividad` es posterior
         * al umbral de inactividad configurado. Si `ultimaActividad` es null pero `activo` es true,
         * se asume activa para evitar logins concurrentes.
         *
         * @param email Email del usuario
         * @return true si la sesión sigue activa, false si no
         */
        public boolean tieneSesionActiva(String email) {
        LocalDateTime umbralInactividad = LocalDateTime.now().minusMinutes(sesionTimeoutMinutos);
        return usuarioRepository.findByEmail(email)
            .map(u -> Boolean.TRUE.equals(u.getActivo()) &&
                (u.getUltimaActividad() == null || !u.getUltimaActividad().isBefore(umbralInactividad)))
            .orElse(false);
        }

    /**
     * Genera un código OTP de 6 dígitos.
     */
    private String generateOtpCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
