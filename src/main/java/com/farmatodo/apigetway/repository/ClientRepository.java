package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.Client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link Client}.
 * Gestiona el acceso y la persistencia de los datos de los usuarios clientes.
 */
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Busca un cliente basado en su nombre de usuario único.
     *
     * Utilizado principalmente durante el proceso de autenticación (login).
     *
     * @param username El nombre de usuario del cliente.
     * @return Un {@link Optional} que contiene el {@link Client} si se encuentra.
     */
    Optional<Client> findByUsername(String username);
}