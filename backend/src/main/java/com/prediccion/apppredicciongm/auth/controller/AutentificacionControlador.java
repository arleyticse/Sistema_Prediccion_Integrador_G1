package com.prediccion.apppredicciongm.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.prediccion.apppredicciongm.auth.dto.AuthRequest;
import com.prediccion.apppredicciongm.auth.dto.AuthResponse;
import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.auth.dto.ForgotPasswordRequest;
import com.prediccion.apppredicciongm.auth.dto.VerifyOtpRequest;
import com.prediccion.apppredicciongm.auth.dto.ResetPasswordRequest;
import com.prediccion.apppredicciongm.auth.models.SeguridadUsuario;
import com.prediccion.apppredicciongm.auth.service.AccountLockService;
import com.prediccion.apppredicciongm.auth.service.IUsuarioService;
import com.prediccion.apppredicciongm.auth.service.PasswordRecoveryService;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.security.jwt.JwtTokenUtil;
import com.prediccion.apppredicciongm.auth.service.RefreshTokenService;
import com.prediccion.apppredicciongm.auth.models.RefreshToken;
import com.prediccion.apppredicciongm.auth.dto.TokenRefreshRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de autenticación y autorización.
 * 
 * Maneja las operaciones de login, registro, recuperación de contraseña
 * y actualización de contraseñas. Genera tokens JWT para las peticiones autenticadas.
 * 
 * Incluye:
 * - Control de intentos fallidos con bloqueo de cuenta
 * - Desbloqueo de cuenta mediante código OTP
 * - Expiración automática de sesiones inactivas
 * 
 * @version 1.1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación y autorización")
public class AutentificacionControlador {

    private static final Logger log = LoggerFactory.getLogger(AutentificacionControlador.class);

    private final AuthenticationManager manejadorAutenticacion;
    private final JwtTokenUtil utilitarioJwt;
    private final IUsuarioService usuarioServicio;
    private final PasswordEncoder codificadorContrasena;
    private final RefreshTokenService refreshTokenService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final AccountLockService accountLockService;

    @Value("${security.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    /**
     * Inicia sesión de un usuario.
     * 
     * Autentica al usuario con sus credenciales y devuelve un token JWT.
     * Implementa control de intentos fallidos y bloqueo de cuenta.
     * Las sesiones inactivas expiran automáticamente (no bloquean nuevos logins).
     * 
     * @param solicitud Credenciales del usuario (email y contraseña)
     * @return Token JWT y datos del usuario autenticado
     */
    @PostMapping("/iniciar-sesion")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT junto con sus datos")
    @jakarta.transaction.Transactional
    public ResponseEntity<?> iniciarSesion(@RequestBody AuthRequest solicitud) {
        String email = solicitud.getEmail().toLowerCase().trim();
        
        try {
            log.info("Intento de autenticación para usuario: {}", email);

            // Verificar si la cuenta está bloqueada
            if (accountLockService.estaCuentaBloqueada(email)) {
                log.warn("[SEGURIDAD] Intento de login en cuenta bloqueada: {}", email);
                Map<String, Object> response = new HashMap<>();
                response.put("error", "CUENTA_BLOQUEADA");
                response.put("message", "Tu cuenta ha sido bloqueada por múltiples intentos fallidos. " +
                        "Usa la opción 'Desbloquear cuenta' para recibir un código de verificación.");
                return ResponseEntity.status(423).body(response); // 423 Locked
            }

            // Intentar autenticación
            Authentication autenticacion = manejadorAutenticacion.authenticate(
                    new UsernamePasswordAuthenticationToken(email, solicitud.getClave()));

            SecurityContextHolder.getContext().setAuthentication(autenticacion);

            SeguridadUsuario detallesUsuario = (SeguridadUsuario) autenticacion.getPrincipal();

            Usuario usuario = usuarioServicio.obtenerUsuarioPorCorreo(detallesUsuario.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Login exitoso: reiniciar intentos fallidos y marcar como activo
            accountLockService.reiniciarIntentosFallidos(email);
            usuarioServicio.actualizarEstadoActivo(email, true);

            String tokenJwt = utilitarioJwt.generarToken(detallesUsuario);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario.getEmail());

            log.info("Usuario autenticado exitosamente: {} (ID: {})", usuario.getEmail(), usuario.getUsuarioId());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenJwt)
                    .refreshToken(refreshToken.getToken())
                    .nombreCompleto(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol())
                    .build());

        } catch (LockedException e) {
            log.warn("[SEGURIDAD] Cuenta bloqueada: {}", email);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "CUENTA_BLOQUEADA");
            response.put("message", "Tu cuenta está bloqueada. Usa 'Desbloquear cuenta'.");
            return ResponseEntity.status(423).body(response);
            
        } catch (BadCredentialsException e) {
            // Registrar intento fallido
            boolean cuentaBloqueada = accountLockService.registrarIntentoFallido(email);
            int intentosRestantes = accountLockService.obtenerIntentosRestantes(email);
            
            Map<String, Object> response = new HashMap<>();
            
            if (cuentaBloqueada) {
                log.warn("[SEGURIDAD] Cuenta bloqueada tras múltiples intentos: {}", email);
                response.put("error", "CUENTA_BLOQUEADA");
                response.put("message", "Tu cuenta ha sido bloqueada por seguridad. " +
                        "Usa 'Desbloquear cuenta' para recibir un código de verificación.");
                return ResponseEntity.status(423).body(response);
            }
            
            log.warn("[SEGURIDAD] Credenciales inválidas para: {} ({} intentos restantes)", 
                    email, intentosRestantes);
            response.put("error", "CREDENCIALES_INVALIDAS");
            response.put("message", "Credenciales inválidas. Te quedan " + intentosRestantes + " intentos.");
            response.put("intentosRestantes", intentosRestantes);
            return ResponseEntity.status(401).body(response);
            
        } catch (Exception e) {
            log.error("Error de autenticación para usuario {}: {}", email, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de autenticación: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión de un usuario.
     * 
     * Marca al usuario como inactivo en la base de datos.
     * 
     * @return Mensaje de confirmación
     */
    @PostMapping("/cerrar-sesion")
    @Operation(summary = "Cerrar sesión", description = "Marca al usuario como inactivo")
    public ResponseEntity<?> cerrarSesion() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Cerrando sesión para usuario: {}", email);

            usuarioServicio.actualizarEstadoActivo(email, false);

            Usuario usuario = usuarioServicio.obtenerUsuarioPorCorreo(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            refreshTokenService.deleteByUserId(Long.valueOf(usuario.getUsuarioId()));

            return ResponseEntity.ok("Sesión cerrada exitosamente");
        } catch (Exception e) {
            log.error("Error al cerrar sesión: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al cerrar sesión");
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Verifica que el correo no esté registrado, encripta la contraseña,
     * guarda el usuario en la base de datos y devuelve un token JWT.
     * 
     * @param usuario Datos del nuevo usuario a registrar
     * @return Token JWT y datos del usuario registrado
     */
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Registra un nuevo usuario y devuelve un token JWT")
    public ResponseEntity<?> registrar(@RequestBody UsuarioCreateRequest usuario) {
        try {
            log.info("Intento de registro para email: {}", usuario.getEmail());

            if (usuarioServicio.obtenerUsuarioPorCorreo(usuario.getEmail()).isPresent()) {
                log.warn("Intento de registro con email ya existente: {}", usuario.getEmail());
                return ResponseEntity.badRequest().body("El correo ya está registrado");
            }

            usuario.setClaveHash(codificadorContrasena.encode(usuario.getClaveHash()));
            Usuario nuevoUsuario = usuarioServicio.crearUsuario(usuario);

            SeguridadUsuario detallesUsuario = new SeguridadUsuario(nuevoUsuario);
            String tokenJwt = utilitarioJwt.generarToken(detallesUsuario);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(nuevoUsuario.getEmail());

            log.info("Nuevo usuario registrado exitosamente: {} (ID: {})", nuevoUsuario.getEmail(),
                    nuevoUsuario.getUsuarioId());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenJwt)
                    .refreshToken(refreshToken.getToken())
                    .nombreCompleto(nuevoUsuario.getNombre())
                    .email(nuevoUsuario.getEmail())
                    .rol(nuevoUsuario.getRol())
                    .build());
        } catch (Exception e) {
            log.error("Error al registrar usuario {}: {}", usuario.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }

    /**
     * Actualiza la contraseña de un usuario.
     * 
     * Cambia la contraseña del usuario identificado por su correo electrónico.
     * La nueva contraseña se encripta antes de almacenarse.
     * 
     * @param email            Email del usuario cuya contraseña se actualizará
     * @param nuevaContrasenia Nueva contraseña en texto plano
     * @return Mensaje de confirmación
     */
    @PostMapping("/actualizar-contrasenia")
    @Operation(summary = "Actualizar contraseña", description = "Actualiza la contraseña de un usuario")
    public ResponseEntity<?> actualizarContrasenia(@RequestParam String email, @RequestParam String nuevaContrasenia) {
        try {
            log.info("Solicitud de actualización de contraseña para: {}", email);

            Usuario usuarioActualizado = usuarioServicio.actualizarContrasenia(email, nuevaContrasenia);
            log.info("Contraseña actualizada exitosamente para usuario: {}", email);

            return ResponseEntity
                    .ok("Contraseña actualizada exitosamente para el usuario: " + usuarioActualizado.getEmail());
        } catch (Exception e) {
            log.error("Error al actualizar contraseña para {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Error al actualizar la contraseña: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar token", description = "Obtiene un nuevo token JWT usando un refresh token válido")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {
                    String token = utilitarioJwt.generarToken(new SeguridadUsuario(usuario));
                    return ResponseEntity.ok(AuthResponse.builder()
                            .token(token)
                            .refreshToken(requestRefreshToken)
                            .nombreCompleto(usuario.getNombre())
                            .email(usuario.getEmail())
                            .rol(usuario.getRol())
                            .build());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no está en la base de datos!"));
    }

    /**
     * Solicita recuperación de contraseña.
     * 
     * Genera un código OTP de 6 dígitos y lo envía al correo del usuario.
     * El código tiene validez de 10 minutos.
     * 
     * @param request Email del usuario
     * @return Mensaje de confirmación
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Olvidé mi contraseña", description = "Solicita código OTP para recuperación de contraseña")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Solicitud de recuperación de contraseña para: {}", request.getEmail());
        Map<String, Object> response = passwordRecoveryService.sendPasswordResetCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica el código OTP sin cambiar la contraseña.
     * 
     * Permite validar que el código sea correcto antes de proceder
     * al cambio de contraseña.
     * 
     * @param request Email y código OTP
     * @return Resultado de la verificación
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verificar código OTP", description = "Valida el código OTP sin cambiar la contraseña")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verificación de código OTP para: {}", request.getEmail());
        Map<String, Object> response = passwordRecoveryService.verifyOtpCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Restablece la contraseña usando código OTP.
     * 
     * Verifica el código OTP y actualiza la contraseña del usuario.
     * El código se marca como usado después de un restablecimiento exitoso.
     * 
     * @param request Email, código OTP y nueva contraseña
     * @return Resultado de la operación
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando código OTP")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Solicitud de restablecimiento de contraseña para: {}", request.getEmail());
        Map<String, Object> response = passwordRecoveryService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Solicita código OTP para desbloquear cuenta.
     * 
     * Envía un código de 6 dígitos al email del usuario para desbloquear
     * su cuenta después de múltiples intentos fallidos.
     * 
     * @param request Email del usuario
     * @return Resultado de la operación
     */
    @PostMapping("/solicitar-desbloqueo")
    @Operation(summary = "Solicitar desbloqueo", description = "Envía código OTP para desbloquear cuenta bloqueada")
    public ResponseEntity<Map<String, Object>> solicitarDesbloqueo(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("[SEGURIDAD] Solicitud de desbloqueo para: {}", request.getEmail());
        Map<String, Object> response = accountLockService.solicitarDesbloqueo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Desbloquea la cuenta usando código OTP.
     * 
     * Verifica el código OTP y desbloquea la cuenta del usuario,
     * reiniciando el contador de intentos fallidos.
     * 
     * @param request Email y código OTP
     * @return Resultado de la operación
     */
    @PostMapping("/desbloquear-cuenta")
    @Operation(summary = "Desbloquear cuenta", description = "Desbloquea la cuenta usando código OTP")
    public ResponseEntity<Map<String, Object>> desbloquearCuenta(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("[SEGURIDAD] Intento de desbloqueo para: {}", request.getEmail());
        Map<String, Object> response = accountLockService.desbloquearCuenta(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica el estado de bloqueo de una cuenta.
     * 
     * @param email Email del usuario
     * @return Estado de la cuenta
     */
    @GetMapping("/estado-cuenta")
    @Operation(summary = "Estado de cuenta", description = "Verifica si la cuenta está bloqueada")
    public ResponseEntity<Map<String, Object>> estadoCuenta(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean bloqueada = accountLockService.estaCuentaBloqueada(email.toLowerCase().trim());
        int intentosRestantes = accountLockService.obtenerIntentosRestantes(email.toLowerCase().trim());
        
        response.put("bloqueada", bloqueada);
        response.put("intentosRestantes", intentosRestantes);
        response.put("maxIntentos", maxIntentosFallidos);
        
        return ResponseEntity.ok(response);
    }
}
