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
import com.prediccion.apppredicciongm.auth.models.SeguridadUsuario;
import com.prediccion.apppredicciongm.auth.service.IUsuarioService;
import com.prediccion.apppredicciongm.models.Usuario;
import com.prediccion.apppredicciongm.security.jwt.JwtTokenUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de autenticación y autorización.
 * 
 * Maneja las operaciones de login, registro y actualización de contraseñas.
 * Genera tokens JWT para las peticiones autenticadas.
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
    public ResponseEntity<?> iniciarSesion(@RequestBody AuthRequest solicitud) {
        try {
            log.info("Intento de autenticación para usuario: {}", solicitud.getEmail());
            
            Authentication autenticacion = manejadorAutenticacion.authenticate(
                    new UsernamePasswordAuthenticationToken(solicitud.getEmail(), solicitud.getClave()));

            SecurityContextHolder.getContext().setAuthentication(autenticacion);

            SeguridadUsuario detallesUsuario = (SeguridadUsuario) autenticacion.getPrincipal();
            String tokenJwt = utilitarioJwt.generarToken(detallesUsuario);

            Usuario usuario = usuarioServicio.obtenerUsuarioPorCorreo(detallesUsuario.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            log.info("Usuario autenticado exitosamente: {} (ID: {})", usuario.getEmail(), usuario.getUsuarioId());
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenJwt)
                    .nombreCompleto(usuario.getNombre())
                    .email(usuario.getEmail())
                    .rol(usuario.getRol())
                    .build());

        } catch (Exception e) {
            log.warn("Error de autenticación para usuario {}: {}", solicitud.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales inválidas");
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
            
            log.info("Nuevo usuario registrado exitosamente: {} (ID: {})", nuevoUsuario.getEmail(), nuevoUsuario.getUsuarioId());
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenJwt)
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
     * @param email Email del usuario cuya contraseña se actualizará
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
            
            return ResponseEntity.ok("Contraseña actualizada exitosamente para el usuario: " + usuarioActualizado.getEmail());
        } catch (Exception e) {
            log.error("Error al actualizar contraseña para {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Error al actualizar la contraseña: " + e.getMessage());
        }
    }
}
