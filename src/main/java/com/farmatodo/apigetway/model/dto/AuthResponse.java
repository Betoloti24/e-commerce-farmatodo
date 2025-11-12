package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta utilizado para encapsular el token de autenticación JWT
 * devuelto al cliente tras un inicio de sesión exitoso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    /**
     * El token JWT generado para el usuario autenticado.
     */
    private String token;

    /**
     * Tipo de esquema de autenticación. Por defecto es "Bearer".
     */
    private String type = "Bearer";
}