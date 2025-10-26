package com.prediccion.apppredicciongm.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para generación y validación de tokens JWT.
 * 
 * Proporciona métodos para crear, parsear y validar tokens JWT
 * utilizando la librería JJWT con algoritmo HS256.
 * 
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    /** Clave secreta para firmar los tokens JWT */
    @Value("${jwt.secret:defaultSecretKey}")
    private String claveSecretaJwt;

    /** Tiempo de expiración de los tokens en milisegundos (por defecto: 1 día) */
    @Value("${jwt.expiration:86400000}")
    private long tiempoExpiracionJwt;

    /**
     * Obtiene la clave segura para firmar tokens.
     * 
     * @return Clave HMAC-SHA
     */
    private Key obtenerClaveFirma() {
        byte[] claveBytes = claveSecretaJwt.getBytes();
        return Keys.hmacShaKeyFor(claveBytes);
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     * 
     * @param token Token JWT a procesar
     * @return Nombre de usuario del subject del token
     */
    public String extraerNombreUsuario(String token) {
        return extraerReclamo(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     * 
     * @param token Token JWT a procesar
     * @return Fecha de expiración del token
     */
    public Date extraerFechaExpiracion(String token) {
        return extraerReclamo(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token.
     * 
     * @param token Token JWT a procesar
     * @param resolvedorReclamos Función que procesa los claims
     * @param <T> Tipo de dato a retornar
     * @return Valor del claim solicitado
     */
    public <T> T extraerReclamo(String token, Function<Claims, T> resolvedorReclamos) {
        final Claims claims = extraerTodosLosReclamos(token);
        return resolvedorReclamos.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     * 
     * @param token Token JWT a procesar
     * @return Objeto Claims con toda la información del token
     */
    private Claims extraerTodosLosReclamos(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(obtenerClaveFirma())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token JWT ha expirado.
     * 
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean tokenHaExpirado(String token) {
        Date fechaExpiracion = extraerFechaExpiracion(token);
        return fechaExpiracion.before(new Date());
    }

    /**
     * Genera un nuevo token JWT para un usuario autenticado.
     * 
     * El token incluye:
     * - Subject (email del usuario)
     * - Fecha de emisión
     * - Fecha de expiración
     * - Firma digital HS256
     * 
     * @param usuario Detalles del usuario autenticado
     * @return Token JWT compacto
     */
    public String generarToken(UserDetails usuario) {
        log.debug("Generando token JWT para usuario: {}", usuario.getUsername());
        Map<String, Object> reclamos = new HashMap<>();
        String token = crearToken(reclamos, usuario.getUsername());
        log.info("Token JWT generado exitosamente para usuario: {}", usuario.getUsername());
        return token;
    }

    /**
     * Construye el token JWT con los claims especificados.
     * 
     * @param reclamos Claims adicionales del token
     * @param sujeto Email del usuario (subject)
     * @return Token JWT compacto
     */
    private String crearToken(Map<String, Object> reclamos, String sujeto) {
        return Jwts.builder()
                .setClaims(reclamos)
                .setSubject(sujeto)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracionJwt))
                .signWith(obtenerClaveFirma(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida un token JWT verificando:
     * - Que el usuario del token coincida con el usuario proporcionado
     * - Que el token no esté expirado
     * 
     * @param token Token JWT a validar
     * @param usuario Detalles del usuario para comparación
     * @return true si el token es válido, false en caso contrario
     */
    public Boolean validarToken(String token, UserDetails usuario) {
        final String nombreUsuario = extraerNombreUsuario(token);
        boolean esValido = nombreUsuario.equals(usuario.getUsername()) && !tokenHaExpirado(token);
        if (esValido) {
            log.debug("Token válido para usuario: {}", nombreUsuario);
        } else {
            log.warn("Validación de token fallida para usuario: {}", nombreUsuario);
        }
        return esValido;
    }
}
