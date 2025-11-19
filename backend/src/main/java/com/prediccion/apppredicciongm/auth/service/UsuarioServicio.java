package com.prediccion.apppredicciongm.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.auth.dto.AuthResponse;
import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.Roles;
import com.prediccion.apppredicciongm.models.Usuario;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de usuarios.
 * 
 * Proporciona operaciones de CRUD para usuarios, incluyendo autenticación,
 * registro y actualización de contraseñas. Las contraseñas se codifican
 * usando BCrypt antes de almacenarse.
 * Incluye RF001: funcionalidades de administración de usuarios
 * 
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UsuarioServicio implements IUsuarioService {
    
    private static final Logger log = LoggerFactory.getLogger(UsuarioServicio.class);
    
    private final IUsuarioRepository usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * La contraseña se codifica antes de guardarla. El email debe ser único.
     * 
     * @param usuario Datos del usuario a crear (sin contraseña codificada)
     * @return Usuario creado con ID generado
     * @throws RuntimeException Si ocurre un error al guardar
     */
    @Override
    public Usuario crearUsuario(UsuarioCreateRequest usuario) {
        log.info("Creando nuevo usuario con email: {}", usuario.getEmail());
        
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(usuario.getNombre());
        nuevoUsuario.setEmail(usuario.getEmail());
        nuevoUsuario.setRol(usuario.getRol().name());
        nuevoUsuario.setClaveHash(passwordEncoder.encode(usuario.getClaveHash()));
        
        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getUsuarioId());
        
        return usuarioGuardado;
    }

    /**
     * RF001: Crea un nuevo usuario y retorna AuthResponse
     */
    @Override
    @Transactional
    public AuthResponse crearUsuarioAdmin(UsuarioCreateRequest request) {
        log.info("Admin creando usuario con email: {}", request.getEmail());
        
        // Verificar que el email no exista
        if (usuarioRepositorio.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setRol(request.getRol().name());
        nuevoUsuario.setClaveHash(passwordEncoder.encode(request.getClaveHash()));
        
        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);
        log.info("Usuario creado por admin con ID: {}", usuarioGuardado.getUsuarioId());
        
        return AuthResponse.builder()
            .nombreCompleto(usuarioGuardado.getNombre())
            .email(usuarioGuardado.getEmail())
            .rol(usuarioGuardado.getRol())
            .build();
    }

    /**
     * RF001: Lista todos los usuarios del sistema
     */
    @Override
    public List<AuthResponse> listarUsuarios() {
        log.info("Listando todos los usuarios del sistema");
        
        return usuarioRepositorio.findAll()
            .stream()
            .map(usuario -> AuthResponse.builder()
                .nombreCompleto(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * RF001: Actualiza el rol de un usuario
     */
    @Override
    @Transactional
    public AuthResponse actualizarRol(Long usuarioId, String nuevoRol) {
        log.info("Actualizando rol del usuario {} a {}", usuarioId, nuevoRol);
        
        // Validar que el rol sea válido
        try {
            Roles.valueOf(nuevoRol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido: " + nuevoRol);
        }
        
        Usuario usuario = usuarioRepositorio.findById(usuarioId.intValue())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setRol(nuevoRol.toUpperCase());
        Usuario usuarioActualizado = usuarioRepositorio.save(usuario);
        
        return AuthResponse.builder()
            .nombreCompleto(usuarioActualizado.getNombre())
            .email(usuarioActualizado.getEmail())
            .rol(usuarioActualizado.getRol())
            .build();
    }

    /**
     * RF001: Elimina un usuario del sistema
     */
    @Override
    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        log.info("Eliminando usuario: {}", usuarioId);
        
        if (!usuarioRepositorio.existsById(usuarioId.intValue())) {
            throw new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
        }
        
        usuarioRepositorio.deleteById(usuarioId.intValue());
        log.info("Usuario {} eliminado correctamente", usuarioId);
    }
    
    /**
     * Obtiene un usuario por su correo electrónico.
     * 
     * @param correoElectronico Email del usuario a buscar
     * @return Optional con el usuario si existe, vacío en caso contrario
     */
    @Override
    public Optional<Usuario> obtenerUsuarioPorCorreo(String correoElectronico) {
        log.debug("Buscando usuario con email: {}", correoElectronico);
        return usuarioRepositorio.findByEmail(correoElectronico);
    }
    
    /**
     * Actualiza la contraseña de un usuario.
     * 
     * La nueva contraseña se codifica antes de guardarla. Requiere una transacción
     * activa para asegurar la consistencia de los datos.
     * 
     * @param correoElectronico Email del usuario cuya contraseña se actualizará
     * @param contrasenia Nueva contraseña en texto plano
     * @return Usuario actualizado
     * @throws RuntimeException Si el usuario no existe
     */
    @Override
    @Transactional
    public Usuario actualizarContrasenia(String correoElectronico, String contrasenia) {
        log.info("Actualizando contraseña para usuario: {}", correoElectronico);
        
        Usuario usuario = usuarioRepositorio.findByEmail(correoElectronico)
            .orElseThrow(() -> {
                log.warn("Intento de actualizar contraseña para usuario no existente: {}", correoElectronico);
                return new RuntimeException("Usuario no encontrado con email: " + correoElectronico);
            });
        
        usuario.setClaveHash(passwordEncoder.encode(contrasenia));
        Usuario usuarioActualizado = usuarioRepositorio.save(usuario);
        log.info("Contraseña actualizada exitosamente para usuario: {}", correoElectronico);
        
        return usuarioActualizado;
    }
    /**
     * Actualiza el estado activo de un usuario.
     * 
     * @param email Email del usuario
     * @param activo Nuevo estado activo
     */
    @Override
    @Transactional
    public void actualizarEstadoActivo(String email, boolean activo) {
        log.info("Actualizando estado activo para usuario: {} a {}", email, activo);
        
        Usuario usuario = usuarioRepositorio.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
            
        usuario.setActivo(activo);
        usuarioRepositorio.save(usuario);
    }
}
