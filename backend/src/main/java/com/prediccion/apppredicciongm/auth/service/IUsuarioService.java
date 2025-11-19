package com.prediccion.apppredicciongm.auth.service;

import java.util.List;
import java.util.Optional;
import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.auth.dto.AuthResponse;
import com.prediccion.apppredicciongm.models.Usuario;

/**
 * Interfaz para el servicio de gestión de usuarios.
 * 
 * Define los métodos para operaciones de CRUD de usuarios y autenticación.
 * Incluye RF001: funcionalidades de gestión de usuarios por gerente
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
     * RF001: Crea un nuevo usuario y retorna AuthResponse
     * 
     * @param usuario Datos del usuario a crear
     * @return AuthResponse con datos del usuario creado
     */
    AuthResponse crearUsuarioAdmin(UsuarioCreateRequest usuario);

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
     * @param email       Email del usuario
     * @param contrasenia Nueva contraseña
     * @return Usuario actualizado
     */
    Usuario actualizarContrasenia(String email, String contrasenia);

    /**
     * RF001: Lista todos los usuarios del sistema
     * 
     * @return Lista de usuarios como AuthResponse
     */
    List<AuthResponse> listarUsuarios();

    /**
     * RF001: Actualiza el rol de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param nuevoRol  Nuevo rol a asignar
     * @return AuthResponse con datos actualizados
     */
    AuthResponse actualizarRol(Long usuarioId, String nuevoRol);

    /**
     * RF001: Desactiva un usuario
     * 
     * @param usuarioId ID del usuario a desactivar
     */
    void desactivarUsuario(Long usuarioId);

    /**
     * Actualiza el estado activo de un usuario.
     * 
     * @param email  Email del usuario
     * @param activo Nuevo estado activo
     */
    void actualizarEstadoActivo(String email, boolean activo);

    /**
     * RF001: Actualiza los datos de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param usuario   Datos a actualizar
     * @return AuthResponse con datos actualizados
     */
    AuthResponse actualizarUsuario(Long usuarioId, UsuarioCreateRequest usuario);
}
