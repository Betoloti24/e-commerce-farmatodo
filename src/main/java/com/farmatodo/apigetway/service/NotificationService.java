package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.Client;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de notificaciones por correo electrónico.
 *
 * Utiliza la anotación {@code @Async} para ejecutar el envío de correos en un
 * hilo separado, evitando bloquear el hilo de la solicitud HTTP.
 *
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${spring.mail.username}")
    private String mailUsername;

    /**
     * Envía un correo electrónico asíncrono notificando al cliente sobre el rechazo de un pago.
     *
     * @param client El cliente al que se le envía el correo.
     * @param orderId El ID del pedido afectado.
     * @param amount El monto del pago rechazado.
     * @param rejectionMessage El motivo del rechazo.
     */
    @Async
    public void sendPaymentRejectionEmail(Client client, UUID orderId, BigDecimal amount, String rejectionMessage) {

        try {
            log.info("📧 Iniciando envío asíncrono de correo a: {}", client.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            String subject = "❌ Pago Rechazado para el Pedido #" + orderId.toString().substring(0, 8);
            String body = String.format(
                    "<!DOCTYPE html><html><body>" +
                            "<h4>Estimado(a) %s %s,</h4>" +
                            "<p>Su intento de pago para el **Pedido #%s** por un monto de <b>%s$</b> ha sido **RECHAZADO**.</p>" +
                            "<p style='color: red;'>Motivo del rechazo: %s</p>" +
                            "<p>Por favor, intente con otro método de pago o contacte a soporte.</p>" +
                            "</body></html>",
                    client.getFirstName(), client.getFirstSurname(), orderId, amount.toString(), rejectionMessage
            );

            helper.setFrom(mailUsername);
            helper.setTo(client.getEmail());
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

            log.warn("🚨 EMAIL ENVIADO REALMENTE a {} con asunto: {}", client.getEmail(), subject);

        } catch (MessagingException e) {
            log.error("Error al crear el mensaje MIME para el pedido {}: {}", orderId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error al enviar la notificación de rechazo de pago para el pedido {}: {}", orderId, e.getMessage(), e);
        }
    }
}