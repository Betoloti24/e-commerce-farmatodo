package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Busca todas las órdenes por el ID del cliente.
     * Asume que la entidad Order tiene un campo 'clientId' o una relación con el campo 'client'
     * cuyo ID se mapea automáticamente.
     */
    List<Order> findByClientId(UUID clientId);
}