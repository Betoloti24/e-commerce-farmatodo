package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link CartItem}.
 * Proporciona métodos para acceder y gestionar los ítems dentro de los carritos de compra.
 */
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Busca y retorna todos los ítems del carrito asociados a un cliente específico.
     *
     * @param clientId El ID único del cliente.
     * @return Una lista de {@link CartItem} pertenecientes al cliente.
     */
    List<CartItem> findByClient_Id(UUID clientId);

    /**
     * Busca un ítem específico en el carrito de un cliente dado el ID del cliente y el ID del producto.
     *
     * Este método es crucial para verificar si un producto ya existe en el carrito
     * antes de intentar añadirlo o actualizarlo.
     *
     * @param clientId El ID único del cliente.
     * @param productId El ID único del producto.
     * @return Un {@link Optional} que contiene el {@link CartItem} si existe.
     */
    Optional<CartItem> findByClient_IdAndProduct_Id(UUID clientId, UUID productId);
}