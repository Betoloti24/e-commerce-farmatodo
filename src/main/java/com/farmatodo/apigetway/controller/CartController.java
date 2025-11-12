package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.model.dto.CartAddRequest;
import com.farmatodo.apigetway.model.dto.CartItemResponse;
import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.CartService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la gestión del carrito de compras del cliente.
 * <p>
 * Permite a los clientes autenticados añadir productos al carrito.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    /**
     * Método auxiliar para obtener el ID del cliente autenticado a partir del {@link UserDetails}.
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
     * Añade un producto al carrito del cliente autenticado o actualiza su cantidad si ya existe.
     *
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @param request DTO con el ID del producto y la cantidad a añadir.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<CartItemResponse>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartAddRequest request) {

        try {
            UUID clientId = getAuthenticatedClientId(userDetails);

            CartItemResponse item = cartService.addOrUpdateItemInCart(
                    clientId,
                    request.getProductId(),
                    request.getQuantity()
            );

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Producto añadido/actualizado en el carrito.",
                            item
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
            // Error de negocio (ej. Producto o Cliente no encontrado)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error en el carrito: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Cualquier otro error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar el carrito."
                    ));
        }
    }
}