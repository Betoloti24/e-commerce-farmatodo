package com.farmatodo.apigetway.config;

import com.farmatodo.apigetway.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad para la autenticación basada en JWT (JSON Web Token).
 *
 * Intercepta las solicitudes, extrae el token del encabezado 'Authorization', lo valida
 * y, si es válido, establece la autenticación del usuario en el contexto de seguridad.
 *
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Servicio para la manipulación y validación de JWT. */
    private final JwtService jwtService;
    /** Servicio para cargar los detalles del usuario a partir del nombre de usuario. */
    private final UserDetailsService clientDetailsService;

    /**
     * Realiza la lógica de filtrado por solicitud.
     *
     * @param request La solicitud HTTP entrante.
     * @param response La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros para continuar el procesamiento.
     * @throws ServletException Si ocurre un error de servlet.
     * @throws IOException Si ocurre un error de I/O.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Verificar si el encabezado de autorización existe y tiene formato "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el JWT y el nombre de usuario
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        // 3. Validar y autenticar si el usuario no está ya autenticado
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.clientDetailsService.loadUserByUsername(username);

            // 4. Si el token es válido, establecer la autenticación en el contexto
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}