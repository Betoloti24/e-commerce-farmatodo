package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un pedido realizado por un cliente.
 *
 * Almacena información clave del pedido como el monto total, la fecha,
 * la tarjeta utilizada para el pago y la dirección de entrega.
 *
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Identificador único (UUID) del pedido.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación Many-to-One con la entidad {@link Client}.
     * Representa al cliente que realizó el pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Monto total del pedido, incluyendo impuestos y posibles costos de envío.
     */
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /**
     * Fecha y hora en que se realizó el pedido.
     */
    @Column(name = "order_date", nullable = false)
    private ZonedDateTime orderDate = ZonedDateTime.now();

    /**
     * Relación Many-to-One con la entidad {@link TokenizedCard}.
     * Identifica la tarjeta tokenizada utilizada para pagar el pedido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tokenized_card_id", nullable = false)
    private TokenizedCard tokenizedCard;

    /**
     * Fecha y hora de creación del registro del pedido.
     */
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate = ZonedDateTime.now();

    /**
     * Indicador de si el pedido está bloqueado o pendiente de pago (true si es el caso).
     */
    @Column(name = "is_blocked_for_payment", nullable = false)
    private Boolean isBlockedForPayment = false;

    /**
     * Dirección de entrega del pedido.
     */
    @Column(name = "delivery_address", length = 100, nullable = false)
    private String deliveryAddress;

    /**
     * Relación One-to-Many con la entidad {@link OrderDetail}.
     * Lista de los ítems y sus precios en el momento de la compra.
     * {@code cascade = CascadeType.ALL} asegura que los detalles se persistan/eliminen con la orden.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details;

    /**
     * Relación One-to-Many con la entidad {@link PaymentTransaction}.
     * Lista de todas las transacciones de pago asociadas a este pedido.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> transactions;
}