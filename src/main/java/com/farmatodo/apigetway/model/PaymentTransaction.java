package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que registra cada intento de transacción de pago asociado a un pedido {@link Order}.
 */
@Entity
@Table(name = "payment_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    /**
     * Identificador único (UUID) de la transacción.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación Many-to-One con la entidad {@link Order}.
     * El pedido al que pertenece esta transacción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * UUID de correlación para seguimiento externo de la transacción.
     */
    @Column(name = "transaction_uuid", nullable = false)
    private UUID transactionUuid;

    /**
     * Monto de la transacción.
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Estado de la transacción (ej. "APPROVED", "REJECTED").
     */
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    /**
     * Número de intento de esta transacción para el pedido (inicia en 1).
     */
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 1;

    /**
     * Fecha y hora de la transacción.
     */
    @Column(name = "transaction_date", nullable = false)
    private ZonedDateTime transactionDate = ZonedDateTime.now();
}