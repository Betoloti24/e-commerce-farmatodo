package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que almacena pares clave-valor para la configuración dinámica del sistema
 * (preferencias de negocio).
 */
@Entity
@Table(name = "system_preference")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemPreference {

    /**
     * Identificador único (UUID) de la preferencia.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Clave única para identificar la preferencia (ej. 'payment.rejection_rate').
     */
    @Column(name = "pref_key", length = 100, nullable = false, unique = true)
    private String prefKey;

    /**
     * Valor de la preferencia, almacenado como una cadena.
     */
    @Column(name = "pref_value", nullable = false)
    private String prefValue;

    /**
     * Tipo de dato del valor para facilitar la conversión (ej. 'INTEGER', 'STRING').
     */
    @Column(name = "data_type", length = 20, nullable = false)
    private String dataType;

    /**
     * Fecha y hora de creación de la preferencia.
     */
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate = ZonedDateTime.now();

    /**
     * Fecha y hora de la última actualización del valor de la preferencia.
     */
    @Column(name = "update_date")
    private ZonedDateTime updateDate;
}