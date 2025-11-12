package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.model.dto.CardDataRequest;
import com.farmatodo.apigetway.model.dto.CardDetailResponse;
import com.farmatodo.apigetway.model.dto.TokenizeResponse;
import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.CardManagementService;

import jakarta.validation.Valid;

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

@RestController
@RequestMapping("/api/v1/card")
@RequiredArgsConstructor
public class CardController {

    private final CardManagementService cardManagementService;
    private final AuthService authService;

    /**
     * Endpoint para tokenizar una tarjeta. Requiere autenticación por API Key (Role ROLE_TOKENIZATION_SERVICE).
     * El payload de la respuesta es {@link TokenizeResponse}.
     *
     * @param request DTO con los datos de la tarjeta.
     * @param apiKey La API Key de seguridad para autenticar la llamada al servicio interno.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<TokenizeResponse>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TokenizeResponse>> createCardForClient(
            @Valid @RequestBody CardDataRequest request,
            @RequestHeader("X-API-KEY") String apiKey) {
        try {
            TokenizeResponse response = cardManagementService.createTokenizedCard(request, apiKey);

            // Retorno exitoso 201 CREATED
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            HttpStatus.CREATED,
                            "Tarjeta tokenizada y almacenada con éxito.",
                            response
                    ));
        } catch (IllegalArgumentException e) {
            // Error de validación o rechazo del proveedor (400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error de tokenización: " + e.getMessage()
                    ));
        } catch (RuntimeException e) {
            // Error interno del servidor o comunicación fallida (500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar la tokenización. Detalles: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Error de registro: " + e.getMessage()
                    ));
        }
    }

    /**
     * Método auxiliar para obtener el ID del cliente autenticado a partir del UserDetails.
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
     * Endpoint para obtener todas las tarjetas tokenizadas de un cliente.
     * El payload de la respuesta es List<CardDetailResponse>.
     *
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @return ResponseEntity con la lista de tarjetas.
     */
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<CardDetailResponse>>> getAllCardsForClient(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID clientId = getAuthenticatedClientId(userDetails);
            List<CardDetailResponse> cards = cardManagementService.findAllCardsByClient(clientId);

            if (cards.isEmpty()) {
                // 200 OK (contenido vacío) o 204 NO_CONTENT, aquí usamos 200 OK con mensaje de éxito
                return ResponseEntity.ok(
                        ApiResponse.success(
                                HttpStatus.OK,
                                "El cliente no tiene tarjetas registradas.",
                                cards // Lista vacía
                        )
                );
            }

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Tarjetas recuperadas con éxito.",
                            cards
                    )
            );
        } catch (AccessDeniedException e) {
            // Error de autenticación/autorización
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Error de registro: " + e.getMessage()
                    ));
        }
    }

    /**
     * Endpoint para obtener una tarjeta específica por su ID.
     * El payload de la respuesta es CardDetailResponse.
     *
     * @param cardId El ID de la tarjeta a buscar.
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @return ResponseEntity con la tarjeta solicitada.
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CardDetailResponse>> getCardById(@PathVariable UUID cardId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID clientId = getAuthenticatedClientId(userDetails);
            CardDetailResponse response = cardManagementService.findCardByIdAndClient(cardId, clientId);

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Detalles de la tarjeta recuperados.",
                            response
                    )
            );
        } catch (IllegalArgumentException e) {
            // Tarjeta no encontrada (404 Not Found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            HttpStatus.NOT_FOUND,
                            "Tarjeta con ID " + cardId + " no encontrada."
                    ));
        } catch (AccessDeniedException e) {
            // Tarjeta encontrada, pero no pertenece al cliente (403 Forbidden)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Error de registro: " + e.getMessage()
                    ));
        }
    }
}