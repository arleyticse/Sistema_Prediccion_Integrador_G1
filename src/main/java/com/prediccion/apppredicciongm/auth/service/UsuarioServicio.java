package com.prediccion.apppredicciongm.auth.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.models.Usuario;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de gestión de usuarios.
 * 
 * Proporciona operaciones de CRUD para usuarios, incluyendo autenticación,
 * registro y actualización de contraseñas. Las contraseñas se codifican
 * usando BCrypt antes de almacenarse.
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
        nuevoUsuario.setClaveHash(usuario.getClaveHash());
        
        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getUsuarioId());
        
        return usuarioGuardado;
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
}
