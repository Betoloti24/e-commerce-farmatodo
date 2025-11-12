package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.PaymentTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link PaymentTransaction}.
 * Gestiona el registro de las transacciones de pago e incluye consultas personalizadas
 * para determinar el historial de intentos de pago de un pedido.
 */
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    /**
     * Recupera el número máximo de intentos de pago registrados para un pedido específico.
     *
     * @param orderId El ID único del pedido.
     * @return Un {@link Optional} que contiene el número máximo de intentos como {@link Integer}.
     */
    @Query("SELECT MAX(pt.attempts) FROM PaymentTransaction pt WHERE pt.order.id = :orderId")
    Optional<Integer> findMaxAttemptByOrderId(@Param("orderId") UUID orderId);
}