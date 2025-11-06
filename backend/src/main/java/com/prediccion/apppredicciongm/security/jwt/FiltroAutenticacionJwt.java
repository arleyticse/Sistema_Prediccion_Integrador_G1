package com.prediccion.apppredicciongm.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prediccion.apppredicciongm.security.service.UsuarioDetalleServicio;

import java.io.IOException;

/**
 * Filtro de autenticación JWT.
 * 
 * Se ejecuta en cada petición HTTP para extraer, validar y establecer
 * el contexto de seguridad basado en el token JWT presente en el
 * encabezado "Authorization".
 * 
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroAutenticacionJwt.class);
    
    private static final String ENCABEZADO_BEARER = "Bearer ";
    private static final String ENCABEZADO_AUTORIZACION = "Authorization";

    private final JwtTokenUtil jwtTokenUtil;
    private final UsuarioDetalleServicio usuarioDetalleServicio;

    /**
     * Procesa el filtro de autenticación JWT.
     * 
     * Extrae el token del encabezado Authorization, lo valida y establece
     * el contexto de seguridad si el token es válido.
     * 
     * @param solicitud Solicitud HTTP entrante
     * @param respuesta Respuesta HTTP saliente
     * @param cadenaFiltros Cadena de filtros a ejecutar
     * @throws ServletException Si ocurre un error en el servlet
     * @throws IOException Si ocurre un error de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
            FilterChain cadenaFiltros)
            throws ServletException, IOException {
        
        final String encabezadoAutorizacion = solicitud.getHeader(ENCABEZADO_AUTORIZACION);

        String nombreUsuario = null;
        String tokenJwt = null;

        try {
            if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith(ENCABEZADO_BEARER)) {
                tokenJwt = encabezadoAutorizacion.substring(7);
                nombreUsuario = jwtTokenUtil.extraerNombreUsuario(tokenJwt);
                log.debug("Token JWT extraído para usuario: {}", nombreUsuario);
            }

            if (nombreUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails detallesUsuario = this.usuarioDetalleServicio.loadUserByUsername(nombreUsuario);

                if (jwtTokenUtil.validarToken(tokenJwt, detallesUsuario)) {
                    UsernamePasswordAuthenticationToken tokenAutenticacion = new UsernamePasswordAuthenticationToken(
                            detallesUsuario, null, detallesUsuario.getAuthorities());

                    tokenAutenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
                    SecurityContextHolder.getContext().setAuthentication(tokenAutenticacion);
                    
                    log.info("Usuario autenticado mediante JWT: {} con autoridades: {}", 
                            nombreUsuario, detallesUsuario.getAuthorities());
                } else {
                    log.warn("Validación de token fallida para usuario: {}", nombreUsuario);
                }
            }
        } catch (Exception e) {
            log.error("Error en el filtro de autenticación JWT: {}", e.getMessage());
        }

        cadenaFiltros.doFilter(solicitud, respuesta);
    }

    /**
     * Define las rutas que no requieren filtro de autenticación.
     * 
     * @param request Solicitud HTTP
     * @return true si la ruta debe omitirse del filtro
     * @throws ServletException Si ocurre un error en el servlet
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/api/catalogos");
    }
}
