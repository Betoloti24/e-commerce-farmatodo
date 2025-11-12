package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.config.InitialDataLoader;
import com.farmatodo.apigetway.model.dto.CardDataRequest;
import com.farmatodo.apigetway.model.dto.TokenizationResult;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Random;

/**
 * Servicio que simula la interacción con un proveedor de tokenización externo.
 *
 * Encargado de realizar la validación de la tarjeta, simular el rechazo basado
 * en una preferencia del sistema, generar el token (hash) y cifrar la fecha de expiración.
 *
 */
@Service
@RequiredArgsConstructor
public class ExternalTokenizationProviderService {

    private final CardValidatorService validatorService;
    private final EncryptionService encryptionService;
    private final PreferenceService preferenceService;

    private static final String TOKEN_REJECTION_RATE_KEY = InitialDataLoader.TOKEN_REJECTION_RATE;

    /**
     * Procesa la solicitud de tokenización, incluyendo validación, simulación de rechazo,
     * y generación/cifrado de los datos resultantes.
     *
     * @param request DTO con los datos de la tarjeta.
     * @return El DTO {@link TokenizationResult} con el token y datos cifrados.
     * @throws IllegalArgumentException Si la tarjeta falla la validación o si la tokenización es rechazada por simulación.
     */
    public TokenizationResult processTokenization(CardDataRequest request) {

        // 1. Validar datos de la tarjeta
        validatorService.validateCard(request);

        // 2. Simular rechazo basado en la preferencia del sistema
        Integer rejectionRate = preferenceService.getPreferenceValueAsInteger(TOKEN_REJECTION_RATE_KEY);
        int randomNumber = new Random().nextInt(100) + 1;
        if (randomNumber <= rejectionRate) {
            throw new IllegalArgumentException("La generación del token ha sido rechazada por el proveedor.");
        }

        // 3. Procesar datos
        String expirationDate = request.getExpirationDate();
        String expirationDateEncrypted = encryptionService.encrypt(expirationDate);
        String token = generateToken(request.getCardNumber());
        String lastFourDigits = request.getCardNumber().substring(request.getCardNumber().length() - 4);

        return new TokenizationResult(
                request.getClientId(),
                token,
                lastFourDigits,
                expirationDateEncrypted
        );
    }

    /**
     * Genera un token seguro (hash SHA-256) a partir del número de tarjeta.
     *
     * @param cardNumber El número de tarjeta (PAN).
     * @return El token hash SHA-256 en formato hexadecimal.
     * @throws RuntimeException Si el algoritmo SHA-256 no está disponible.
     */
    private String generateToken(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cardNumber.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar token: Algoritmo SHA-256 no encontrado.", e);
        }
    }
}