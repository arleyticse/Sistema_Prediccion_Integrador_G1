package com.prediccion.apppredicciongm.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

import java.util.Map;

/**
 * Controlador de autenticación y autorización.
 * 
 * Maneja las operaciones de login, registro, recuperación de contraseña
 * y actualización de contraseñas. Genera tokens JWT para las peticiones autenticadas.
 * 
 * @version 1.0
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

    /**
     * Inicia sesión de un usuario.
     * 
     * Autentica al usuario con sus credenciales y devuelve un token JWT
     * junto con sus datos de perfil.
     * 
     * @param solicitud Credenciales del usuario (email y contraseña)
     * @return Token JWT y datos del usuario autenticado
     */
    @PostMapping("/iniciar-sesion")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT junto con sus datos")
    @jakarta.transaction.Transactional
    public ResponseEntity<?> iniciarSesion(@RequestBody AuthRequest solicitud) {
        try {
            log.info("Intento de autenticación para usuario: {}", solicitud.getEmail());

            Authentication autenticacion = manejadorAutenticacion.authenticate(
                    new UsernamePasswordAuthenticationToken(solicitud.getEmail(), solicitud.getClave()));

            SecurityContextHolder.getContext().setAuthentication(autenticacion);

            SeguridadUsuario detallesUsuario = (SeguridadUsuario) autenticacion.getPrincipal();

            Usuario usuario = usuarioServicio.obtenerUsuarioPorCorreo(detallesUsuario.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar si ya tiene sesión activa
            if (Boolean.TRUE.equals(usuario.getActivo())) {
                log.warn("Intento de inicio de sesión duplicado para usuario: {}", usuario.getEmail());
                return ResponseEntity.status(409).body("El usuario ya tiene una sesión activa.");
            }

            // Marcar usuario como activo
            usuarioServicio.actualizarEstadoActivo(usuario.getEmail(), true);

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

        } catch (Exception e) {
            log.error("Error de autenticación para usuario {}: {}", solicitud.getEmail(), e.getMessage(), e);
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
}
