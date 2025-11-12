package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

/**
 * Entidad que representa un ítem dentro del carrito de compras de un cliente.
 *
 * Se asegura la unicidad de la combinación {@code client_id} y {@code product_id}
 * para evitar duplicados del mismo producto en el carrito del mismo cliente.
 *
 */
@Entity
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"client_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    /**
     * Identificador único (UUID) del ítem del carrito.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación Many-to-One con la entidad {@link Client}.
     * Representa el cliente propietario de este ítem del carrito.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Relación Many-to-One con la entidad {@link Product}.
     * Representa el producto añadido al carrito.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Cantidad del producto que el cliente ha añadido al carrito.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}