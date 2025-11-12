package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.UUID;

/**
 * DTO utilizado para iniciar un proceso de pago, generalmente para un pedido
 * ya existente que está a la espera de ser pagado.
 */
@Data
public class PaymentRequest {

    /**
     * El ID de la tarjeta tokenizada a utilizar para la transacción.
     */
    @NotNull(message = "El ID de la tarjeta tokenizada es obligatorio.")
    private UUID tokenizedCardId;
}