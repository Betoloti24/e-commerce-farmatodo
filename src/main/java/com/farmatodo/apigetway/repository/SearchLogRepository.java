package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.SearchLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link SearchLog}.
 * Gestiona la persistencia de los registros de búsqueda de los usuarios.
 */
public interface SearchLogRepository extends JpaRepository<SearchLog, UUID> {
    // ... métodos existentes

    /**
     * Recupera las palabras clave de búsqueda únicas asociadas a un cliente específico.
     *
     * @param clientId El ID único del cliente.
     * @return Una lista de cadenas con las palabras clave únicas.
     */
    @Query("SELECT DISTINCT s.searchKeyword FROM SearchLog s WHERE s.client.id = :clientId ORDER BY s.searchKeyword")
    List<String> findUniqueKeywordsByClientId(UUID clientId);
}