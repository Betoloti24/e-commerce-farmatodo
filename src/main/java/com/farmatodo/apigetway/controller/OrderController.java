package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.Order;
import com.farmatodo.apigetway.model.dto.*;
import com.farmatodo.apigetway.model.PaymentTransaction;
import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.OrderService;
import com.farmatodo.apigetway.service.exception.PaymentRejectionException;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
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
 * Controlador REST para la gestión de Pedidos (Orders) y transacciones de Pago.
 * <p>
 * Permite a los clientes autenticados crear pedidos a partir de su carrito de compras
 * e iniciar procesos de pago.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
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
     * Consulta todas las órdenes de compra asociadas al cliente autenticado.
     *
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<List<OrderSummaryResponse>>).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getAllOrders(@AuthenticationPrincipal UserDetails userDetails) {

        try {
            UUID clientId = getAuthenticatedClientId(userDetails);

            // LLAMADA AL SERVICIO: ahora retorna List<OrderSummaryResponse>
            List<OrderSummaryResponse> orders = orderService.getAllOrdersByClient(clientId);

            if (orders.isEmpty()) {
                return ResponseEntity.ok(
                        ApiResponse.success(
                                HttpStatus.OK,
                                "El cliente no tiene órdenes de compra registradas.",
                                orders
                        )
                );
            }

            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Órdenes de compra recuperadas exitosamente. Total: " + orders.size(),
                            orders
                    )
            );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado: " + e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al consultar las órdenes de compra."
                    ));
        }
    }

    /**
     * Crea un nuevo pedido a partir del contenido actual del carrito de compras del cliente.
     * El carrito se vacía tras la creación exitosa del pedido.
     *
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @param request DTO con la tarjeta tokenizada y la dirección de entrega.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<OrderResponse>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        try {
            UUID clientId = getAuthenticatedClientId(userDetails);
            OrderResponse newOrderResponse = orderService.createOrderFromCart(clientId, request);

            // Retorno exitoso 201 CREATED
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            HttpStatus.CREATED,
                            "Pedido creado exitosamente a partir del carrito. Pendiente de pago.",
                            newOrderResponse
                    ));
        } catch (AccessDeniedException e) {
            // Error de autenticación/autorización
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado: " + e.getMessage()
                    ));
        } catch (IllegalArgumentException e) {
            // Error de negocio (ej. carrito vacío, tarjeta no encontrada)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error al crear el pedido: " + e.getMessage()
                    ));
        } catch (DataIntegrityViolationException e) {
            // Error de integridad de datos
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error de datos: Verifique los IDs proporcionados."
                    ));
        } catch (Exception e) {
            // Cualquier otro error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor al procesar el pedido."
                    ));
        }
    }

    /**
     * Procesa el pago de un pedido existente.
     *
     * @param orderId El ID del pedido a pagar.
     * @param userDetails Los detalles del usuario autenticado vía JWT.
     * @param request DTO con el ID de la tarjeta tokenizada a usar para el pago.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<PaymentResponse>).
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> payOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {

        try {
            UUID clientId = getAuthenticatedClientId(userDetails);

            PaymentTransaction transaction = orderService.processOrderPayment(orderId, clientId, request);
            PaymentResponse response = new PaymentResponse(transaction);

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Pago procesado exitosamente. Estado: " + transaction.getStatus(),
                            response
                    )
            );
        } catch (PaymentRejectionException e) {
            // Rechazo simulado o por intentos agotados (400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Pago Rechazado: " + e.getMessage()
                    ));
        } catch (AccessDeniedException | SecurityException e) {
            // Error de seguridad (pedido no pertenece al cliente) (403 Forbidden)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            HttpStatus.FORBIDDEN,
                            "Acceso denegado al pedido o tarjeta."
                    ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Errores de negocio (pedido no encontrado, pedido bloqueado) (400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error de pago: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Cualquier otro error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar el pago."
                    ));
        }
    }
}