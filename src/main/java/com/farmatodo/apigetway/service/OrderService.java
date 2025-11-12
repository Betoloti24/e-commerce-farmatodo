package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.config.InitialDataLoader;
import com.farmatodo.apigetway.model.*;
import com.farmatodo.apigetway.model.dto.*;
import com.farmatodo.apigetway.repository.PaymentTransactionRepository;
import com.farmatodo.apigetway.repository.CartItemRepository;
import com.farmatodo.apigetway.repository.ClientRepository;
import com.farmatodo.apigetway.repository.OrderRepository;
import com.farmatodo.apigetway.repository.TokenizedCardRepository;
import com.farmatodo.apigetway.service.exception.PaymentRejectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio central para la gestión de pedidos (Orders)
 * y el procesamiento de pagos.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final TokenizedCardRepository tokenizedCardRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final PreferenceService preferenceService;
    private final NotificationService notificationService;

    private static final String PAYMENT_REJECTION_RATE_KEY = InitialDataLoader.PAYMENT_REJECTION_RATE;
    private static final String PAYMENT_MAX_ATTEMPTS_KEY = InitialDataLoader.PAYMENT_MAX_ATTEMPTS;

    /**
     * Busca un cliente por ID o lanza una excepción.
     * @param clientId ID del cliente.
     * @return El cliente encontrado.
     * @throws IllegalArgumentException Si el cliente no existe.
     */
    private Client findClientById(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + clientId + " no encontrado."));
    }

    /**
     * Busca una tarjeta tokenizada por ID y verifica la propiedad del cliente.
     * @param cardId ID de la tarjeta.
     * @param clientId ID del cliente propietario.
     * @return La tarjeta tokenizada.
     * @throws IllegalArgumentException Si la tarjeta no existe.
     * @throws SecurityException Si la tarjeta no pertenece al cliente.
     */
    private TokenizedCard findCardByIdAndClient(UUID cardId, UUID clientId) {
        TokenizedCard card = tokenizedCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta con ID " + cardId + " no encontrada."));

        if (!card.getClient().getId().equals(clientId)) {
            throw new SecurityException("Acceso denegado: La tarjeta no pertenece al cliente.");
        }
        return card;
    }

    /**
     * Busca un producto por ID. (Delegada a ProductService, aquí solo para consistencia)
     * @param productId ID del producto.
     * @return El producto encontrado.
     */
    private Product findProductById(UUID productId) {
        return productService.findProductById(productId);
    }

    /**
     * Crea y configura una nueva transacción de pago.
     * @param order Pedido asociado.
     * @param status Estado inicial.
     * @param transactionUuid UUID de correlación.
     * @param attempts Número de intentos (se calcula dentro del método).
     * @return La entidad {@link PaymentTransaction} (sin guardar).
     */
    private PaymentTransaction createPaymentTransaction(Order order, String status, UUID transactionUuid, int attempts) {
        Optional<Integer> maxAttemptOpt = paymentTransactionRepository.findMaxAttemptByOrderId(order.getId());
        int newAttempt = maxAttemptOpt.map(max -> max + 1).orElse(1);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setAmount(order.getTotalAmount());
        transaction.setStatus(status);
        transaction.setTransactionUuid(transactionUuid);
        transaction.setTransactionDate(ZonedDateTime.now());
        transaction.setAttempts(newAttempt);

        return transaction;
    }

    /**
     * Procesa el pago de un pedido existente.
     *
     * Incluye chequeos de intentos máximos, simulación de rechazo (basado en preferencia)
     * y, en caso de rechazo, notifica al cliente y bloquea el pedido si se supera el límite.
     *
     * @param orderId ID del pedido a pagar.
     * @param clientId ID del cliente que intenta pagar.
     * @param request DTO con la tarjeta tokenizada a usar (solo por ID).
     * @return La transacción de pago si es exitosa.
     * @throws PaymentRejectionException Si el pago es rechazado (sin hacer rollback).
     * @throws IllegalStateException Si el pedido está bloqueado o supera los intentos.
     */
    @Transactional(noRollbackFor = PaymentRejectionException.class)
    public PaymentTransaction processOrderPayment(UUID orderId, UUID clientId, PaymentRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido con ID " + orderId + " no encontrado."));

        if (!order.getClient().getId().equals(clientId)) {
            throw new SecurityException("Acceso denegado: El pedido no pertenece al cliente autenticado.");
        }

        // 1. Chequeo de intentos
        Optional<Integer> maxAttemptOpt = paymentTransactionRepository.findMaxAttemptByOrderId(order.getId());
        int currentAttempt = maxAttemptOpt.map(max -> max + 1).orElse(1);
        Integer maxAllowedAttempts = preferenceService.getPreferenceValueAsInteger(PAYMENT_MAX_ATTEMPTS_KEY);

        if (order.getIsBlockedForPayment() || currentAttempt > maxAllowedAttempts) {
            // Bloquear si es el primer intento después del límite (o ya está bloqueado)
            if (!order.getIsBlockedForPayment()) {
                order.setIsBlockedForPayment(true);
                orderRepository.save(order);
            }
            throw new IllegalStateException("El pedido ha sido bloqueado por sobrepasar la cantidad de intentos de pago.");
        }

        // 2. Simulación de pago
        String transactionStatus;
        UUID transactionUuid = UUID.randomUUID();
        Integer rejectionRate = preferenceService.getPreferenceValueAsInteger(PAYMENT_REJECTION_RATE_KEY);
        int randomNumber = new Random().nextInt(100) + 1;

        if (randomNumber <= rejectionRate) {
            // RECHAZO
            transactionStatus = "REJECTED";
            final String rejectionMessage = "El servicio de pago ha rechazado su transaccion, valide los datos ingresados.";

            PaymentTransaction rejectedTransaction = createPaymentTransaction(order, transactionStatus, transactionUuid, currentAttempt);
            paymentTransactionRepository.save(rejectedTransaction);

            // 3. Notificación y bloqueo final
            Client clientForEmail = findClientById(clientId);
            notificationService.sendPaymentRejectionEmail(clientForEmail, orderId, order.getTotalAmount(), rejectionMessage);

            if (currentAttempt == maxAllowedAttempts) {
                order.setIsBlockedForPayment(true);
                orderRepository.save(order);
            }

            throw new PaymentRejectionException(rejectionMessage);
        } else {
            // ÉXITO
            transactionStatus = "SUCCESS";
            PaymentTransaction transaction = createPaymentTransaction(order, transactionStatus, transactionUuid, currentAttempt);
            return paymentTransactionRepository.save(transaction);
        }
    }

    /**
     * Recupera todas las órdenes de compra asociadas a un cliente específico y las mapea a DTOs.
     * <p>
     * El mapeo a DTO se realiza DENTRO del ámbito transaccional para evitar la
     * excepción de LOB al acceder a la tarjeta tokenizada.
     * </p>
     *
     * @param clientId El ID único del cliente.
     * @return Una lista de objetos {@link OrderSummaryResponse} (DTOs).
     * @throws IllegalArgumentException Si el cliente no existe.
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getAllOrdersByClient(UUID clientId) {
        // Opcional: Validar la existencia del cliente si no se hace en el repositorio
        if (!clientRepository.existsById(clientId)) {
            throw new IllegalArgumentException("Cliente con ID " + clientId + " no encontrado.");
        }

        List<Order> orders = orderRepository.findByClientId(clientId);

        // Mapeo a DTO DENTRO DE LA TRANSACCIÓN
        return orders.stream()
                .map(OrderSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo pedido a partir de los ítems actualmente en el carrito del cliente.
     *
     * Transfiere ítems del carrito a detalles de pedido, calcula el monto total,
     * guarda el pedido y vacía el carrito.
     *
     * @param clientId ID del cliente.
     * @param request DTO con la tarjeta tokenizada y la dirección de entrega.
     * @return El DTO {@link OrderResponse} del pedido creado.
     * @throws IllegalArgumentException Si el carrito está vacío.
     */
    @Transactional
    public OrderResponse createOrderFromCart(UUID clientId, OrderCreateRequest request) {

        List<CartItemResponse> cartItemsResponse = cartService.getCartItemsByClient(clientId);

        if (cartItemsResponse.isEmpty()) {
            throw new IllegalArgumentException("El carrito de compras está vacío. No se puede crear un pedido.");
        }

        TokenizedCard paymentCard = findCardByIdAndClient(request.getTokenizedCardId(), clientId);
        Client client = findClientById(clientId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItem> itemsToDelete = cartItemRepository.findByClient_Id(clientId); // Para borrado posterior

        List<OrderDetail> orderDetails = cartItemsResponse.stream()
                .map(itemResponse -> {
                    Product product = findProductById(itemResponse.getProductId());
                    BigDecimal unitPrice = BigDecimal.valueOf(10.00); // Precio fijo/simulado

                    OrderDetail detail = new OrderDetail();
                    detail.setProduct(product);
                    detail.setQuantity(itemResponse.getQuantity());
                    detail.setUnitPrice(unitPrice);
                    return detail;
                })
                .collect(Collectors.toList());

        for (OrderDetail detail : orderDetails) {
            totalAmount = totalAmount.add(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }

        // 1. Crear y guardar el pedido
        Order order = new Order();
        order.setClient(client);
        order.setTotalAmount(totalAmount);
        order.setTokenizedCard(paymentCard);
        order.setDeliveryAddress(request.getDeliveryAddress());

        Order savedOrder = orderRepository.save(order);

        // 2. Asociar y guardar los detalles del pedido
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder);
        }
        savedOrder.setDetails(orderDetails);
        savedOrder = orderRepository.save(savedOrder);

        // 3. Vaciar el carrito
        cartItemRepository.deleteAll(itemsToDelete);

        // 4. Mapear respuesta
        List<OrderResponse.OrderItemSummary> itemSummaries = savedOrder.getDetails().stream()
                .map(detail -> new OrderResponse.OrderItemSummary(
                        detail.getProduct().getId(),
                        detail.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getClient().getId(),
                savedOrder.getTokenizedCard().getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getIsBlockedForPayment(),
                savedOrder.getDeliveryAddress(),
                itemSummaries
        );
    }
}