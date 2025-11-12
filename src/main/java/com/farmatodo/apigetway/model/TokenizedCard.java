package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que almacena la información de una tarjeta de crédito/débito tokenizada.
 *
 * Contiene el token seguro (hash del PAN) y la fecha de expiración cifrada.
 *
 */
@Entity
@Table(name = "tokenized_cards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"token"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenizedCard {

    /**
     * Identificador único (UUID) de la tarjeta tokenizada.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Relación Many-to-One con la entidad {@link Client}.
     * El cliente propietario de la tarjeta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * El hash único (token) que representa el número de tarjeta (PAN).
     */
    @Column(name = "token", length = 255, nullable = false, unique = true)
    private String token;

    /**
     * Los últimos cuatro dígitos del PAN.
     */
    @Column(name = "last_four_digits", length = 4, nullable = false)
    private String lastFourDigits;

    /**
     * La fecha de expiración cifrada. Se almacena como {@code LOB} (Large Object) o CLOB/TEXT.
     */
    @Lob
    @Column(name = "expiration_date_encrypted", nullable = false)
    private String expirationDateEncrypted;

    /**
     * Fecha y hora de creación del registro.
     */
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate = ZonedDateTime.now();

    /**
     * Fecha y hora de la última actualización.
     */
    @Column(name = "update_date")
    private ZonedDateTime updateDate;

    /**
     * Método de ciclo de vida de JPA ejecutado antes de cada actualización.
     * Establece la fecha de actualización automáticamente.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateDate = ZonedDateTime.now();
    }
}