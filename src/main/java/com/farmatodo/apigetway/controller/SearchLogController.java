package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.SearchLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la consulta de registros de búsqueda (SearchLog).
 * <p>
 * Permite a los usuarios autenticados consultar su historial de búsquedas.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/search-log")
@RequiredArgsConstructor
public class SearchLogController {

    private final SearchLogService searchLogService;
    private final AuthService authService;

    /**
     * Método auxiliar para obtener el ID del cliente autenticado.
     *
     * @param userDetails Objeto de seguridad de Spring Security.
     * @return El ID (UUID) del cliente autenticado.
     * @throws AccessDeniedException Si el usuario no está autenticado o su ID no puede ser resuelto.
     */
    private UUID getAuthenticatedClientId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new AccessDeniedException("Usuario no autenticado o token inválido.");
        }
        try {
            String username = userDetails.getUsername();
            return authService.getClientIdByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new AccessDeniedException("ID de cliente inválido en el token de autenticación.", e);
        } catch (Exception e) {
            throw new AccessDeniedException("Error al obtener la identidad del cliente.", e);
        }
    }

    /**
     * Consulta el historial de palabras clave de búsqueda únicas de un usuario autenticado.
     *
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<List<String>>).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<String>>> getSearchHistory(@AuthenticationPrincipal UserDetails userDetails) {

        try {
            UUID clientId = getAuthenticatedClientId(userDetails);
            List<String> keywords = searchLogService.getUniqueSearchKeywordsByClient(clientId);

            if (keywords.isEmpty()) {
                // 200 OK con lista vacía
                return ResponseEntity.ok(
                        ApiResponse.success(
                                HttpStatus.OK,
                                "El historial de búsqueda está vacío.",
                                keywords
                        )
                );
            }

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Historial de búsquedas únicas recuperado exitosamente. Total: " + keywords.size(),
                            keywords
                    )
            );

        } catch (AccessDeniedException e) {
            // Error de autenticación/autorización
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado: " + e.getMessage()
                    ));

        } catch (IllegalArgumentException e) {
            // Error de negocio (ej. Cliente no encontrado, aunque se maneja con AccessDenied la mayoría de las veces)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error al buscar historial: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Cualquier otro error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al consultar el historial de búsquedas."
                    ));
        }
    }
}