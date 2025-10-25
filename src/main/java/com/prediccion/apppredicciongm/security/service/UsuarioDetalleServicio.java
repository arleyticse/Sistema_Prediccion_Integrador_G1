package com.prediccion.apppredicciongm.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.auth.models.SeguridadUsuario;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.models.Usuario;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de detalles de usuario para Spring Security.
 * 
 * Implementa la interfaz UserDetailsService para cargar los detalles
 * de un usuario durante el proceso de autenticación.
 * 
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetalleServicio implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetalleServicio.class);

    @Autowired
    private final IUsuarioRepository usuarioRepositorio;

    /**
     * Carga los detalles de un usuario por su email.
     * 
     * Este método es invocado por Spring Security durante la autenticación
     * para obtener los detalles del usuario que será autenticado.
     * 
     * @param username Email del usuario a cargar
     * @return Detalles del usuario envueltos en SeguridadUsuario
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando detalles del usuario: {}", username);
        
        Usuario usuario = usuarioRepositorio.findByEmail(username)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado con email: " + username);
            });
        
        log.info("Detalles de usuario cargados exitosamente: {}", username);
        return new SeguridadUsuario(usuario);
    }
}
