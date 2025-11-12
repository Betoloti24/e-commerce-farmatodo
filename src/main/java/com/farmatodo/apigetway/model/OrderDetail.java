package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad que representa el detalle de un ítem dentro de un pedido {@link Order}.
 *
 * Almacena el precio unitario del producto en el momento exacto de la compra.
 * Se asegura la unicidad de la combinación {@code order_id} y {@code product_id}.
 *
 */
@Entity
@Table(name = "order_detail", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    /**
     * Identificador único (UUID) del detalle del pedido.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación Many-to-One con la entidad {@link Order}.
     * El pedido al que pertenece este detalle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Relación Many-to-One con la entidad {@link Product}.
     * El producto comprado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Cantidad del producto comprado en este detalle.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Precio unitario del producto al momento de la compra.
     */
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
}