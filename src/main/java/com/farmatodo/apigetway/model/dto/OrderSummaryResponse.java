package com.farmatodo.apigetway.model.dto;

import com.farmatodo.apigetway.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de respuesta utilizado para resumir una orden de compra.
 * Se utiliza para evitar la carga perezosa de entidades JPA y el error de LOB
 * al serializar en el controlador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID orderId;
    private UUID clientId;
    private BigDecimal totalAmount;
    private ZonedDateTime orderDate;
    private Boolean isBlockedForPayment;
    private String deliveryAddress;
    private String tokenizedCardLastFourDigits; // Campo resumido de la relación TokenizedCard

    private List<OrderItemSummary> items;

    /**
     * Constructor estático para mapear la entidad {@link Order} al DTO.
     * Este mapeo debe ocurrir dentro de una transacción activa.
     * * @param order La entidad Order a mapear.
     * @return Una instancia de OrderSummaryResponse.
     */
    public static OrderSummaryResponse fromEntity(Order order) {
        // Acceder a la entidad TokenizedCard aquí fuerza su inicialización dentro del ámbito transaccional.
        String lastFourDigits = order.getTokenizedCard() != null ?
                order.getTokenizedCard().getLastFourDigits() :
                "N/A";

        List<OrderItemSummary> itemSummaries = order.getDetails().stream()
                .map(detail -> new OrderItemSummary(
                        detail.getProduct().getId(),
                        detail.getQuantity()
                ))
                .collect(Collectors.toList());

        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .clientId(order.getClient().getId())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .isBlockedForPayment(order.getIsBlockedForPayment())
                .deliveryAddress(order.getDeliveryAddress())
                .tokenizedCardLastFourDigits(lastFourDigits)
                .items(itemSummaries)
                .build();
    }

    // Clase interna reutilizada del DTO anterior
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemSummary {
        private UUID productId;
        private Integer quantity;
    }
}