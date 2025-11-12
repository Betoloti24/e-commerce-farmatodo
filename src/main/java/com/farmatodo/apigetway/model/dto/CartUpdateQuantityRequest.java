package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * DTO utilizado para solicitar la actualización de la cantidad de un producto
 * específico dentro del carrito de compras.
 */
@Data
public class CartUpdateQuantityRequest {
    /**
     * La nueva cantidad deseada para el producto. Un valor de 0 indica eliminación.
     */
    @Min(value = 0, message = "La cantidad no puede ser negativa.")
    @NotNull(message = "La cantidad es obligatoria.")
    private Integer quantity;
}