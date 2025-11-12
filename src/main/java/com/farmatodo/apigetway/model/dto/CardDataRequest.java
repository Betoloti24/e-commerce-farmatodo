package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.util.UUID;

/**
 * DTO utilizado para recibir los datos sensibles de la tarjeta de crédito/débito
 * que se van a tokenizar y almacenar de forma segura.
 */
@Data
public class CardDataRequest {

    /**
     * ID único del cliente al que se asociará la tarjeta tokenizada. Es obligatorio.
     */
    @NotNull(message = "El ID del cliente es obligatorio.")
    private UUID clientId;

    /**
     * El número de tarjeta (PAN). Debe ser una cadena de 13 a 16 dígitos.
     */
    @NotBlank(message = "El número de tarjeta es obligatorio.")
    @Pattern(regexp = "^[0-9]{13,16}$", message = "El número de tarjeta debe tener entre 13 y 16 dígitos.")
    private String cardNumber;

    /**
     * El Código de Verificación de Valor (CVV). Debe ser de 3 o 4 dígitos.
     */
    @NotBlank(message = "El CVV es obligatorio.")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 dígitos.")
    private String cvv;

    /**
     * El mes de expiración de la tarjeta en formato MM (01-12).
     */
    @NotBlank(message = "El mes de expiración es obligatorio.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "El mes de expiración debe estar en formato MM (01-12).")
    private String expirationMonth;

    /**
     * El año de expiración de la tarjeta en formato YY (últimos dos dígitos).
     */
    @NotBlank(message = "El año de expiración es obligatorio.")
    @Pattern(regexp = "^[0-9]{2}$", message = "El año de expiración debe estar en formato YY.")
    private String expirationYear;

    /**
     * Construye la fecha de expiración completa a partir del mes y el año.
     *
     * @return La fecha de expiración en formato MM/YY.
     */
    public String getExpirationDate() {
        return this.expirationMonth + "/" + this.expirationYear;
    }
}