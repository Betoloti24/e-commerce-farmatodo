package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.model.dto.RegisterRequest;
import com.farmatodo.apigetway.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.farmatodo.apigetway.model.dto.LoginRequest;
import com.farmatodo.apigetway.model.dto.AuthResponse;
import com.farmatodo.apigetway.model.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para registrar un nuevo cliente.
     *
     * @param request DTO con los datos de registro.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse).
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerClient(@RequestBody RegisterRequest request) {
        try {
            Client newClient = authService.registerNewClient(request);

            // Retorno exitoso
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            HttpStatus.CREATED,
                            "Usuario registrado con éxito. ID: " + newClient.getId(),
                            null
                    ));

        } catch (IllegalArgumentException e) {
            // Error de solicitud inválida (ej. usuario ya existe)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            e.getMessage()
                    ));

        } catch (IllegalStateException e) {
            // Error interno (ej. rol por defecto no encontrado)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar el registro: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error de autenticación (ej. credenciales inválidas)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Error de registro: " + e.getMessage()
                    ));
        }
    }

    /**
     * Endpoint para iniciar sesión y obtener un token JWT.
     *
     * @param request DTO con las credenciales de login.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse) que contiene AuthResponse.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            String jwt = authService.login(request);
            AuthResponse authResponse = new AuthResponse(jwt, "Bearer");

            // Retorno exitoso
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Autenticación exitosa.",
                            authResponse
                    )
            );

        } catch (Exception e) {
            // Error de autenticación (ej. credenciales inválidas)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            HttpStatus.UNAUTHORIZED,
                            "Autenticación fallida: " + e.getMessage()
                    ));
        }
    }
}