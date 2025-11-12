package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta devuelto al cliente después de un intento de tokenización
 * de una tarjeta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenizeResponse {
    /**
     * ID único asignado a la tarjeta tokenizada (si la tokenización fue exitosa).
     */
    private UUID cardId;

    /**
     * El token (hash) de la tarjeta (si la tokenización fue exitosa).
     */
    private String token;

    /**
     * Los últimos cuatro dígitos de la tarjeta.
     */
    private String lastFourDigits;

    /**
     * Mensaje indicando el resultado del proceso (ej. "Tokenización exitosa").
     */
    private String message;
}