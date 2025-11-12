package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.dto.CardDataRequest;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.InputMismatchException;

/**
 * Servicio encargado de realizar validaciones básicas de formato y coherencia
 * en los datos de la tarjeta (PAN, CVV, fecha de expiración).
 */
@Service
public class CardValidatorService {

    /**
     * Realiza todas las validaciones necesarias sobre los datos de la tarjeta antes de la tokenización.
     *
     * @param request DTO con los datos de la tarjeta.
     * @throws IllegalArgumentException Si alguna validación (Luhn, CVV, Expiración) falla.
     */
    public void validateCard(CardDataRequest request) {
        if (!isLuhnValid(request.getCardNumber())) {
            throw new IllegalArgumentException("El número de tarjeta es inválido (Luhn check fallido).");
        }
        if (!isCvvValid(request.getCvv(), request.getCardNumber())) {
            throw new IllegalArgumentException("El CVV es inválido para este tipo de tarjeta.");
        }
        if (!isExpirationDateValid(request.getExpirationMonth(), request.getExpirationYear())) {
            throw new IllegalArgumentException("La fecha de expiración es inválida o ha expirado.");
        }
    }

    /**
     * Verifica si el número de tarjeta es válido según el Algoritmo de Luhn.
     *
     * @param cardNumber El número de tarjeta a validar.
     * @return {@code true} si la tarjeta pasa el check de Luhn, {@code false} en caso contrario.
     */
    public boolean isLuhnValid(String cardNumber) {
        String cleanNumber = cardNumber.replaceAll("[^0-9]", "");
        int sum = 0;
        boolean alternate = false;

        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cleanNumber.substring(i, i + 1));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Valida si el CVV tiene la longitud correcta (3 o 4 dígitos).
     *
     * @param cvv El CVV a validar.
     * @param cardNumber El número de tarjeta (se puede usar para validar si es Amex, pero aquí solo verifica la longitud básica).
     * @return {@code true} si la longitud del CVV es válida.
     */
    public boolean isCvvValid(String cvv, String cardNumber) {
        return cvv.length() >= 3 && cvv.length() <= 4;
    }

    /**
     * Valida si la fecha de expiración no ha caducado.
     *
     * @param month El mes de expiración (MM).
     * @param year El año de expiración (YY).
     * @return {@code true} si la fecha es igual o posterior a la fecha actual, {@code false} en caso contrario.
     * @throws InputMismatchException Si el formato de mes/año no es numérico.
     */
    public boolean isExpirationDateValid(String month, String year) {
        try {
            int currentYear = YearMonth.now().getYear() % 100;
            int currentMonth = YearMonth.now().getMonthValue();
            int expYear = Integer.parseInt(year);
            int expMonth = Integer.parseInt(month);

            if (expYear < currentYear) {
                return false;
            }

            if (expYear == currentYear) {
                return expMonth >= currentMonth;
            }

            return true;

        } catch (NumberFormatException e) {
            throw new InputMismatchException("Formato de fecha de expiración inválido.");
        }
    }
}