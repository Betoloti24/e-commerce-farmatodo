package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.util.UUID;

/**
 * DTO utilizado para solicitar la creación de un nuevo pedido (Order).
 * Requiere el identificador de la tarjeta tokenizada para el pago y la dirección de entrega.
 */
@Data
public class OrderCreateRequest {

    /**
     * ID único de la tarjeta tokenizada previamente registrada a usar para el pago.
     */
    @NotNull(message = "El ID de la tarjeta tokenizada es obligatorio.")
    private UUID tokenizedCardId;

    /**
     * Dirección física donde debe ser entregado el pedido.
     */
    @NotNull(message = "La dirección de entrega es obligatoria.")
    @NotBlank(message = "La dirección de entrega es obligatoria.")
    @Size(max = 100, message = "La dirección de entrega no puede exceder los 100 caracteres.")
    private String deliveryAddress;
}