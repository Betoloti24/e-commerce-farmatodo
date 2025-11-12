package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que registra las búsquedas realizadas por los usuarios en el sistema.
 * Esto es útil para análisis de comportamiento y tendencias.
 */
@Entity
@Table(name = "search_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog {

    /**
     * Identificador único (UUID) del registro de búsqueda.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Fecha y hora en que se realizó la búsqueda.
     */
    @Column(name = "query_date", nullable = false)
    private ZonedDateTime queryDate = ZonedDateTime.now();

    /**
     * La palabra clave o frase que se buscó.
     */
    @Column(name = "search_keyword", length = 255, nullable = false)
    private String searchKeyword;

    /**
     * Relación Many-to-One con la entidad {@link Client}.
     * El cliente que realizó la búsqueda (puede ser nulo si la búsqueda es anónima).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
}