package com.farmatodo.apigetway.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizado para recibir las credenciales de un usuario durante el proceso
 * de inicio de sesión (Login).
 */
@Data
public class LoginRequest {
    /**
     * Nombre de usuario o identificador de acceso.
     */
    @NotBlank
    private String username;

    /**
     * Contraseña del usuario.
     */
    @NotBlank
    private String password;
}