package com.farmatodo.apigetway.config;

import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.SearchLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * Interceptor de Spring MVC utilizado para registrar las búsquedas de productos
 * realizadas por los usuarios.
 *
 * Implementa {@link HandlerInterceptor} y se activa después de que el controlador
 * ha manejado la solicitud (en el método {@link #postHandle}).
 *
 */
@Component
@RequiredArgsConstructor
public class SearchLogInterceptor implements HandlerInterceptor {

    /** Servicio para el registro de las búsquedas. */
    private final SearchLogService searchLogService;
    /** Servicio para obtener información del usuario autenticado. */
    private final AuthService authService;

    /**
     * Registra el término de búsqueda después de que la solicitud ha sido manejada por el controlador.
     *
     * @param request La solicitud HTTP actual.
     * @param response La respuesta HTTP actual.
     * @param handler El handler (controlador) que ejecutó la solicitud.
     * @param modelAndView El modelo y vista que se va a renderizar (puede ser {@code null}).
     * @throws Exception Si ocurre un error interno durante el manejo.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {

        String keyword = request.getParameter("keyword");

        if (keyword != null && !keyword.trim().isEmpty()) {

            UUID clientId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    // Excluir tokens de autenticación anónima/API Key
                    !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                String username = authentication.getName();
                try {
                    // Intenta obtener el ID del cliente. Si falla (p.ej., si no es un cliente), el log se registrará sin ID.
                    clientId = authService.getClientIdByUsername(username);
                } catch (Exception e) {
                    // Ignorar la excepción y dejar clientId como null.
                }
            }

            searchLogService.logSearch(clientId, keyword);
        }
    }
}