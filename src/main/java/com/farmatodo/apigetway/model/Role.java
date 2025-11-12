package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

/**
 * Entidad que define los roles de seguridad en la aplicación (ej. ROLE_CLIENT, ROLE_ADMIN).
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * Identificador único (UUID) del rol.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Nombre único del rol (ej. "ROLE_CLIENT").
     */
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    /**
     * Descripción del rol.
     */
    @Column(name = "description", length = 255)
    private String description;
}