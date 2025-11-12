package com.farmatodo.apigetway.model.dto;

import com.farmatodo.apigetway.model.CartItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta que representa un ítem específico dentro del carrito de compras
 * de un cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    /**
     * ID único del ítem del carrito.
     */
    private UUID cartItemId;

    /**
     * ID del cliente propietario del carrito.
     */
    private UUID clientId;

    /**
     * ID único del producto enlazado a este ítem.
     */
    private UUID productId;

    /**
     * Número de parte o SKU del producto.
     */
    private String productPartNumber;

    /**
     * Cantidad de este producto en el carrito.
     */
    private Integer quantity;

    /**
     * Constructor que inicializa el DTO a partir de una entidad {@link CartItem}.
     *
     * @param item La entidad del ítem del carrito de origen.
     */
    public CartItemResponse(CartItem item) {
        this.cartItemId = item.getId();
        this.clientId = item.getClient().getId();
        this.productId = item.getProduct().getId();
        this.productPartNumber = item.getProduct().getPartNumber();
        this.quantity = item.getQuantity();
    }
}