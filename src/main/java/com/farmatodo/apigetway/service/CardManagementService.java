package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.model.TokenizedCard;
import com.farmatodo.apigetway.model.dto.CardDetailResponse;
import com.farmatodo.apigetway.model.dto.CardDataRequest;
import com.farmatodo.apigetway.model.dto.TokenizationResult;
import com.farmatodo.apigetway.model.dto.TokenizeResponse;
import com.farmatodo.apigetway.repository.ClientRepository;
import com.farmatodo.apigetway.repository.TokenizedCardRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la gestión de tarjetas tokenizadas.
 *
 * Coordina la validación, la llamada al servicio de tokenización externo y la
 * persistencia segura de la tarjeta tokenizada.
 *
 */
@Service
@RequiredArgsConstructor
public class CardManagementService {

    private final TokenizedCardRepository cardRepository;
    private final ClientRepository clientRepository;
    private final EncryptionService encryptionService;
    private final TokenizationClientService tokenizationClientService;

    /**
     * Procesa la solicitud de una nueva tarjeta, la tokeniza usando un servicio externo
     * y la almacena de forma segura en la base de datos.
     *
     * @param request DTO con los datos de la tarjeta a tokenizar.
     * @param apiKey La API Key de seguridad para autenticarse con el servicio de tokenización.
     * @return Un DTO de respuesta con los detalles de la tarjeta tokenizada.
     * @throws IllegalArgumentException Si el cliente no es encontrado.
     */
    @Transactional
    public TokenizeResponse createTokenizedCard(CardDataRequest request, String apiKey) {

        TokenizationResult result = tokenizationClientService.callTokenizationService(request, apiKey);
        Client client = clientRepository.findById(result.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + result.getClientId() + " no encontrado."));

        TokenizedCard card = new TokenizedCard();
        card.setClient(client);
        card.setToken(result.getToken());
        card.setLastFourDigits(result.getLastFourDigits());
        card.setExpirationDateEncrypted(result.getExpirationDateEncrypted());

        TokenizedCard savedCard = cardRepository.save(card);

        return new TokenizeResponse(
                savedCard.getId(),
                savedCard.getToken(),
                savedCard.getLastFourDigits(),
                "Tarjeta tokenizada y almacenada con éxito."
        );
    }

    /**
     * Recupera todas las tarjetas tokenizadas asociadas a un cliente específico.
     *
     * @param clientId El ID único del cliente.
     * @return Una lista de DTOs {@link CardDetailResponse} con los detalles de las tarjetas.
     */
    @Transactional(readOnly = true)
    public List<CardDetailResponse> findAllCardsByClient(UUID clientId) {
        List<TokenizedCard> cards = cardRepository.findByClient_Id(clientId);
        return cards.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca una tarjeta específica por su ID, asegurando que pertenezca al cliente autenticado.
     *
     * @param cardId El ID de la tarjeta a buscar.
     * @param clientId El ID del cliente que debe ser el propietario.
     * @return Un DTO {@link CardDetailResponse}.
     * @throws IllegalArgumentException Si la tarjeta no es encontrada.
     * @throws AccessDeniedException Si la tarjeta no pertenece al cliente especificado.
     */
    public CardDetailResponse findCardByIdAndClient(UUID cardId, UUID clientId) {
        TokenizedCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta con ID " + cardId + " no encontrada."));

        if (!card.getClient().getId().equals(clientId)) {
            throw new AccessDeniedException("Acceso denegado: La tarjeta no pertenece al cliente autenticado.");
        }

        return convertToDetailResponse(card);
    }

    /**
     * Convierte una entidad {@link TokenizedCard} a un DTO de respuesta.
     *
     * @param card La entidad {@link TokenizedCard} de origen.
     * @return El DTO {@link CardDetailResponse}.
     */
    private CardDetailResponse convertToDetailResponse(TokenizedCard card) {
        return new CardDetailResponse(
                card.getId(),
                card.getClient().getId(),
                card.getToken(),
                card.getLastFourDigits(),
                card.getCreationDate(),
                card.getUpdateDate()
        );
    }
}