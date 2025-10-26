package com.prediccion.apppredicciongm.auth.models;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.prediccion.apppredicciongm.models.Usuario;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Implementación de UserDetails para Spring Security.
 * 
 * Envuelve la entidad Usuario para que Spring Security pueda utilizarla
 * durante el proceso de autenticación y autorización.
 * 
 * @version 1.0
 * @since 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class SeguridadUsuario implements UserDetails {

    /** Usuario asociado a esta instancia de seguridad */
    private Usuario usuario;

    /**
     * Obtiene las autoridades (roles) del usuario.
     * 
     * Convierte el rol del usuario en una autoridad de Spring Security
     * con el prefijo ROLE_.
     * 
     * @return Colección de autoridades del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol()));
        return authorities.stream().collect(Collectors.toList());
    }

    /**
     * Obtiene la contraseña del usuario.
     * 
     * @return Contraseña hasheada
     */
    @Override
    public String getPassword() {
        return usuario.getClaveHash();
    }

    /**
     * Obtiene el nombre de usuario.
     * 
     * @return Email del usuario (usado como identificador único)
     */
    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

}
