package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

/**
 * DTO utilizado para recibir los datos necesarios para registrar un nuevo usuario (cliente)
 * en el sistema.
 */
@Data
public class RegisterRequest {

    /**
     * Nombre de usuario único para el login.
     */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 100)
    private String username;

    /**
     * Contraseña del usuario.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    /**
     * Dirección de correo electrónico del usuario. Debe ser un formato válido.
     */
    @Email(message = "El email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    /**
     * Primer nombre del usuario.
     */
    @NotBlank(message = "El primer nombre es obligatorio")
    private String firstName;

    /**
     * Primer apellido del usuario.
     */
    @NotBlank(message = "El primer apellido es obligatorio")
    private String firstSurname;

    /**
     * Segundo nombre del usuario (opcional).
     */
    private String middleName;

    /**
     * Segundo apellido del usuario (opcional).
     */
    private String secondSurname;

    /**
     * Número de teléfono de contacto (opcional).
     */
    private String phoneNumber;
}