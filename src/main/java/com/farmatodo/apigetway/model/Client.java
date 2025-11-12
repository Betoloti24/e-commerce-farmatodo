package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad que representa a un cliente registrado en el sistema.
 *
 * Almacena los datos personales y de acceso, y gestiona la relación de roles.
 * El {@code username}, {@code email} y {@code phone_number} son únicos.
 *
 */
@Entity
@Table(name = "clients", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"phone_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    /**
     * Identificador único (UUID) del cliente.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Nombre de usuario único utilizado para el login.
     */
    @Column(name = "username", length = 100, nullable = false)
    private String username;

    /**
     * Hash de la contraseña del usuario, almacenada de forma segura (e.g., usando BCrypt).
     */
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    /**
     * Primer nombre del cliente.
     */
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    /**
     * Segundo nombre del cliente (opcional).
     */
    @Column(name = "middle_name", length = 100)
    private String middleName;

    /**
     * Primer apellido del cliente.
     */
    @Column(name = "first_surname", length = 100, nullable = false)
    private String firstSurname;

    /**
     * Segundo apellido del cliente (opcional).
     */
    @Column(name = "second_surname", length = 100)
    private String secondSurname;

    /**
     * Dirección de correo electrónico única del cliente.
     */
    @Column(name = "email", length = 255, nullable = false)
    private String email;

    /**
     * Número de teléfono único del cliente (opcional).
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Estado de actividad de la cuenta (true por defecto).
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Fecha y hora de creación de la cuenta.
     */
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate = ZonedDateTime.now();

    /**
     * Fecha y hora de la última actualización de los datos del cliente.
     */
    @Column(name = "update_date")
    private ZonedDateTime updateDate;

    /**
     * Relación Many-to-Many con la entidad {@link Role}.
     * Define los roles de seguridad asociados a este cliente.
     * La carga es EAGER para facilitar la verificación de seguridad.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "client_roles",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;
}