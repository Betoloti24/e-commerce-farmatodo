package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta que representa los detalles de un pedido (Order) para ser
 * devuelto al cliente. Incluye el resumen de los ítems y el total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * ID único del pedido.
     */
    private UUID orderId;

    /**
     * ID del cliente que realizó el pedido.
     */
    private UUID clientId;

    /**
     * ID de la tarjeta tokenizada utilizada para el pago.
     */
    private UUID tokenizedCardId;

    /**
     * Monto total del pedido.
     */
    private BigDecimal totalAmount;

    /**
     * Indica si el pedido está bloqueado o en espera de pago.
     */
    private Boolean isBlockedForPayment;

    /**
     * Dirección de entrega especificada para el pedido.
     */
    private String deliveryAddress;

    /**
     * Lista de los ítems que componen el pedido.
     */
    private List<OrderItemSummary> items;

    /**
     * DTO interno estático para resumir la información de un ítem dentro del pedido.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemSummary {
        /**
         * ID del producto.
         */
        private UUID productId;

        /**
         * Cantidad comprada de ese producto.
         */
        private Integer quantity;
    }
}