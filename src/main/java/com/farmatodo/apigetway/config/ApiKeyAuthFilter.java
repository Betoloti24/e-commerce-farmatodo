package com.farmatodo.apigetway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad que implementa la autenticación basada en una clave API (API Key).
 *
 * Este filtro intercepta las peticiones y verifica la presencia y validez de una
 * clave API en el encabezado {@value #API_KEY_HEADER}. Si la clave es válida,
 * establece un token de autenticación anónimo con la autoridad
 * "ROLE_TOKENIZATION_SERVICE" en el contexto de seguridad.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    /**
     * El valor secreto de la API Key esperado para la autenticación.
     */
    private final String apiKeyValue;

    /**
     * Nombre del encabezado HTTP donde se espera la API Key.
     */
    private final String API_KEY_HEADER = "X-API-KEY";

    /**
     * Constructor para inicializar el filtro con el valor de la API Key.
     *
     * @param apiKeyValue El valor secreto de la API Key configurado en la aplicación.
     */
    public ApiKeyAuthFilter(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    /**
     * Realiza la lógica de filtrado por solicitud.
     * Extrae el valor del encabezado {@value #API_KEY_HEADER}. Si coincide con
     * {@code apiKeyValue}, autentica al usuario internamente con el rol
     * "ROLE_TOKENIZATION_SERVICE".
     *
     * @param request La solicitud HTTP entrante.
     * @param response La respuesta HTTP saliente.
     * @param filterChain La cadena de filtros para continuar el procesamiento.
     * @throws ServletException Si ocurre un error de servlet.
     * @throws IOException Si ocurre un error de I/O.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey != null && requestApiKey.equals(apiKeyValue)) {
            // Establecer autenticación para el servicio de tokenización
            Authentication authentication = new AnonymousAuthenticationToken(
                    "API_KEY_PRINCIPAL",
                    requestApiKey,
                    AuthorityUtils.createAuthorityList("ROLE_TOKENIZATION_SERVICE")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}