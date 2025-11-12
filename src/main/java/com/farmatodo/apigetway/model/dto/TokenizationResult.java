package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO interno utilizado para encapsular el resultado de un proceso exitoso de tokenización.
 * Contiene los datos que deben ser persistidos en el repositorio seguro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenizationResult {
    /**
     * ID del cliente al que pertenece la tarjeta tokenizada.
     */
    private UUID clientId;

    /**
     * El token (hash SHA-256) que representa el número de tarjeta (PAN).
     */
    private String token;

    /**
     * Los últimos cuatro dígitos del número de tarjeta.
     */
    private String lastFourDigits;

    /**
     * La fecha de expiración cifrada.
     */
    private String expirationDateEncrypted;
}