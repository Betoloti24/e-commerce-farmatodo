package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.dto.ApiResponse; // Importamos el DTO genérico
import com.farmatodo.apigetway.model.dto.CardDataRequest;
import com.farmatodo.apigetway.model.dto.TokenizationResult;
import com.farmatodo.apigetway.service.ExternalTokenizationProviderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que simula la API del proveedor de servicios de tokenización externa.
 * <p>
 * Este endpoint está protegido por una API Key (ROLE_TOKENIZATION_SERVICE) y no por JWT
 * en la configuración de seguridad, ya que no debe ser accesible directamente por clientes
 * sino por el componente interno {@code CardManagementService}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tokenize")
@RequiredArgsConstructor
public class TokenizationController {

    private final ExternalTokenizationProviderService tokenizationProviderService;

    /**
     * Procesa los datos sensibles de la tarjeta para generar un token seguro,
     * cifrar la fecha de expiración y simular el rechazo.
     *
     * @param request DTO con los datos de la tarjeta a tokenizar.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<TokenizationResult>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TokenizationResult>> tokenizeCard(@Valid @RequestBody CardDataRequest request) {
        try {
            TokenizationResult result = tokenizationProviderService.processTokenization(request);

            // Retorno exitoso 200 OK
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(
                            HttpStatus.OK,
                            "Tokenización y cifrado exitosos.",
                            result
                    ));
        } catch (IllegalArgumentException e) {
            // Error de validación (Luhn, CVV, Expiración) o rechazo simulado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error de tokenización: " + e.getMessage()
                    ));
        } catch (RuntimeException e) {
            // Error interno (e.g., error de cifrado/hashing)
            System.err.println("Error interno durante la tokenización: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor al procesar la tokenización."
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Error de registro: " + e.getMessage()
                    ));
        }
    }
}