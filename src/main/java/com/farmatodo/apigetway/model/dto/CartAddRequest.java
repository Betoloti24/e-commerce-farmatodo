package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.UUID;

/**
 * DTO utilizado para solicitar la adición de un producto al carrito de compras
 * del cliente.
 */
@Data
public class CartAddRequest {

    /**
     * El ID único del producto que se desea añadir.
     */
    @NotNull(message = "El ID del producto es obligatorio.")
    private UUID productId;

    /**
     * La cantidad del producto que se desea añadir. Debe ser al menos 1.
     */
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    @NotNull(message = "La cantidad es obligatoria.")
    private Integer quantity;
}