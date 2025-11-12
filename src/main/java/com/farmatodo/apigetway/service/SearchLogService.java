package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.model.SearchLog;
import com.farmatodo.apigetway.repository.ClientRepository;
import com.farmatodo.apigetway.repository.SearchLogRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;


/**
 * Servicio encargado del registro asíncrono de las búsquedas realizadas por los usuarios.
 *
 * Utiliza {@code @Async} para garantizar que el registro de búsqueda no ralentice
 * la respuesta principal de la API.
 *
 */
@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final ClientRepository clientRepository;
    private static final Logger log = LoggerFactory.getLogger(SearchLogService.class);

    /**
     * Registra una búsqueda de manera asíncrona.
     *
     * @param clientId ID del cliente que realizó la búsqueda (puede ser {@code null} si es anónimo).
     * @param keyword La palabra clave que se buscó.
     */
    @Async
    @Transactional
    public void logSearch(UUID clientId, String keyword) {
        try {
            SearchLog searchLog = new SearchLog();
            searchLog.setSearchKeyword(keyword);
            searchLog.setQueryDate(ZonedDateTime.now());

            if (clientId != null) {
                // Se busca al cliente para establecer la relación, si existe
                Optional<Client> clientOpt = clientRepository.findById(clientId);
                clientOpt.ifPresent(searchLog::setClient);
            }

            searchLogRepository.save(searchLog);
            log.info("📝 Log de búsqueda asíncrono guardado para keyword: {}", keyword);
        } catch (Exception e) {
            log.error("Error al guardar el log de búsqueda de manera asíncrona: {}", e.getMessage(), e);
            // La excepción es loggeada, pero no relanzada debido a la naturaleza asíncrona.
        }
    }

    /**
     * Obtiene una lista de todas las palabras clave de búsqueda únicas que ha utilizado un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de strings con las palabras clave únicas.
     * @throws IllegalArgumentException Si el cliente no existe.
     */
    @Transactional(readOnly = true)
    public List<String> getUniqueSearchKeywordsByClient(UUID clientId) {
        // Validación: Asegurar que el cliente existe antes de consultar
        if (!clientRepository.existsById(clientId)) {
            throw new IllegalArgumentException("Cliente con ID " + clientId + " no encontrado.");
        }
        return searchLogRepository.findUniqueKeywordsByClientId(clientId);
    }
}