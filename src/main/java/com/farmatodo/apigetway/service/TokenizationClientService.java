package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.model.dto.CardDataRequest;
import com.farmatodo.apigetway.model.dto.TokenizationResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class TokenizationClientService {

    @Value("${tokenization.header.name}")
    private String API_KEY_HEADER;

    @Value("${tokenization.service.url}")
    private String TOKENIZATION_SERVICE_URL;

    private final RestTemplate restTemplate;

    public TokenizationClientService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Realiza la llamada HTTP POST al servicio de tokenización externo y
     * maneja la respuesta envuelta en ApiResponse.
     *
     * @param request DTO con los datos de la tarjeta a enviar.
     * @param apiKey La clave de autenticación para el servicio externo.
     * @return El DTO {@link TokenizationResult} extraído del cuerpo 'data'.
     * @throws IllegalArgumentException Si la llamada falla o el resultado indica un error de negocio.
     * @throws RuntimeException Si ocurre un error de comunicación o cliente.
     */
    public TokenizationResult callTokenizationService(CardDataRequest request, String apiKey) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_KEY_HEADER, apiKey);
        HttpEntity<CardDataRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ApiResponse<TokenizationResult>> responseEntity = restTemplate.exchange(
                    TOKENIZATION_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<TokenizationResult>>() {}
            );

            ApiResponse<TokenizationResult> apiResponse = responseEntity.getBody();

            if (apiResponse == null) {
                throw new RuntimeException("Respuesta nula del servicio de tokenización.");
            }

            if (apiResponse.isError()) {
                throw new IllegalArgumentException("Error de tokenización del servicio: " + apiResponse.getMessage());
            }

            // Extraer el DTO de la clave 'data'
            TokenizationResult result = apiResponse.getData();

            if (result == null || result.getClientId() == null) {
                throw new IllegalArgumentException("Error con los datos de la tarjeta. Valide y vuelva a intentar más tarde");
            }

            return result;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("La validación de la tarjeta falló en el servicio de tokenización.", e);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error en el cliente de tokenización: Código HTTP " + e.getStatusCode(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Fallo al comunicarse con el servicio de tokenización.", e);
        }
    }
}