package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.TokenizedCard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link TokenizedCard}.
 * Gestiona el acceso y la persistencia de las tarjetas de crédito tokenizadas.
 */
public interface TokenizedCardRepository extends JpaRepository<TokenizedCard, UUID> {

    /**
     * Busca todas las tarjetas tokenizadas asociadas a un cliente específico.
     *
     * @param clientId El ID único del cliente.
     * @return Una lista de {@link TokenizedCard} registradas para el cliente.
     */
    List<TokenizedCard> findByClient_Id(UUID clientId);
}