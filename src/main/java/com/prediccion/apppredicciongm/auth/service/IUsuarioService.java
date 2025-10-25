package com.prediccion.apppredicciongm.auth.service;

import java.util.Optional;
import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.models.Usuario;

/**
 * Interfaz para el servicio de gestión de usuarios.
 * 
 * Define los métodos para operaciones de CRUD de usuarios y autenticación.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface IUsuarioService {
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * @param usuario Datos del usuario a crear
     * @return Usuario creado con ID generado
     */
    Usuario crearUsuario(UsuarioCreateRequest usuario);
    
    /**
     * Obtiene un usuario por su correo electrónico.
     * 
     * @param email Email del usuario a buscar
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> obtenerUsuarioPorCorreo(String email);
    
    /**
     * Actualiza la contraseña de un usuario.
     * 
     * @param email Email del usuario
     * @param contrasenia Nueva contraseña
     * @return Usuario actualizado
     */
    Usuario actualizarContrasenia(String email, String contrasenia);
}
