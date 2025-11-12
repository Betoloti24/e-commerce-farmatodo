package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO utilizado para solicitar la actualización del estado de un pedido.
 */
@Data
public class OrderUpdateRequest {

    /**
     * El nuevo estado deseado para el pedido (ej. "SHIPPED", "DELIVERED").
     */
    @NotBlank(message = "El estado del pedido es obligatorio.")
    private String status;
}