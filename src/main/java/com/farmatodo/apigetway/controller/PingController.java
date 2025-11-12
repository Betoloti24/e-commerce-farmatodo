package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.dto.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * Controlador simple para verificar la salud (health check) y la accesibilidad de la API.
 * <p>
 * Este endpoint es típicamente público y no requiere autenticación.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    /**
     * Responde con un mensaje simple para indicar que la aplicación está en funcionamiento.
     *
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<String>) y el mensaje "pong".
     */
    @GetMapping
    public ResponseEntity<ApiResponse<String>> ping() {
        // Retorno exitoso 200 OK
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Servicio en línea",
                        "pong"
                )
        );
    }
}