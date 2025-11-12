package com.farmatodo.apigetway.model.dto;

import com.farmatodo.apigetway.model.PaymentTransaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que encapsula el resultado de una transacción de pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * ID único de la transacción de pago.
     */
    private UUID transactionId;

    /**
     * ID del pedido al que se aplica esta transacción.
     */
    private UUID orderId;

    /**
     * Monto de la transacción.
     */
    private BigDecimal amount;

    /**
     * Estado actual de la transacción (ej. "APPROVED", "REJECTED", "PENDING").
     */
    private String status;

    /**
     * ID de correlación único para el seguimiento de la transacción.
     */
    private UUID correlationId;

    /**
     * Fecha y hora en que se realizó la transacción.
     */
    private ZonedDateTime transactionDate;

    /**
     * Constructor que inicializa el DTO a partir de una entidad {@link PaymentTransaction}.
     *
     * @param transaction La entidad de la transacción de pago de origen.
     */
    public PaymentResponse(PaymentTransaction transaction) {
        this.transactionId = transaction.getId();
        this.orderId = transaction.getOrder().getId();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
        this.correlationId = transaction.getTransactionUuid();
        this.transactionDate = transaction.getTransactionDate();
    }
}