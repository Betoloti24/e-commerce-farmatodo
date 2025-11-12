package com.farmatodo.apigetway.service.exception;

/**
 * Excepción lanzada cuando la transacción de pago es rechazada, ya sea por el
 * servicio externo o por simulación interna basada en el porcentaje de rechazo.
 *
 * Es una excepción de tipo {@code RuntimeException} para permitir la
 * exclusión de rollback en transacciones (usada con @Transactional(noRollbackFor=...)).
 *
 */
public class PaymentRejectionException extends RuntimeException {

    /**
     * Constructor que crea una nueva instancia de la excepción con el mensaje especificado.
     *
     * @param message Mensaje detallado sobre el motivo del rechazo del pago.
     */
    public PaymentRejectionException(String message) {
        super(message);
    }
}