package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO de respuesta utilizado para devolver los detalles de una tarjeta
 * tokenizada (excluyendo datos sensibles).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailResponse {
    /**
     * ID único de la tarjeta tokenizada en el sistema.
     */
    private UUID cardId;

    /**
     * ID único del cliente propietario de la tarjeta.
     */
    private UUID clientId;

    /**
     * El token (hash) que representa de forma segura la tarjeta.
     */
    private String token;

    /**
     * Los últimos cuatro dígitos del número de tarjeta para fines de visualización.
     */
    private String lastFourDigits;

    /**
     * Fecha y hora de creación del registro de la tarjeta.
     */
    private ZonedDateTime creationDate;

    /**
     * Fecha y hora de la última actualización del registro de la tarjeta.
     */
    private ZonedDateTime updateDate;
}