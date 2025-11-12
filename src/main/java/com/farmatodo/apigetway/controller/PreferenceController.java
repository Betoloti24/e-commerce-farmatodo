package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.SystemPreference;
import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.model.dto.PreferenceRequest;
import com.farmatodo.apigetway.service.PreferenceService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de las preferencias de configuración dinámica
 * del sistema.
 * <p>
 * Nota: Los endpoints de este controlador generalmente deberían estar protegidos
 * con el rol ROLE_ADMIN o similar, aunque la lógica de autenticación se maneja
 * en {@link com.farmatodo.apigetway.config.SecurityConfig}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    /**
     * Crea una nueva preferencia de configuración en el sistema.
     *
     * @param request DTO con la clave, valor y tipo de dato de la nueva preferencia.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<SystemPreference>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SystemPreference>> createPreference(@Valid @RequestBody PreferenceRequest request) {
        try {
            SystemPreference newPreference = preferenceService.createPreference(request);

            // Retorno exitoso 201 CREATED
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            HttpStatus.CREATED,
                            "Preferencia '" + newPreference.getPrefKey() + "' creada con éxito.",
                            newPreference
                    ));
        } catch (IllegalArgumentException e) {
            // Error de conflicto (409 Conflict) si la clave ya existe
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            HttpStatus.CONFLICT,
                            "Error al crear preferencia: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al guardar la preferencia."
                    ));
        }
    }

    /**
     * Actualiza el valor y el tipo de dato de una preferencia existente.
     *
     * @param prefKey La clave de la preferencia a actualizar (obtenida de la URL).
     * @param request DTO con el nuevo valor y tipo de dato.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<SystemPreference>).
     */
    @PutMapping("/{prefKey}")
    public ResponseEntity<ApiResponse<SystemPreference>> updatePreference(
            @PathVariable String prefKey,
            @Valid @RequestBody PreferenceRequest request) {
        try {
            // Nota: Aunque el request DTO contiene la clave, se usa el PathVariable por seguridad y consistencia
            // Sin embargo, el DTO de entrada no debería ser modificado directamente en un controlador,
            // pero para esta estructura, simplemente delegamos la clave de la URL al servicio.

            SystemPreference updatedPreference = preferenceService.updatePreference(prefKey, request);

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Preferencia '" + prefKey + "' actualizada con éxito.",
                            updatedPreference
                    )
            );
        } catch (IllegalArgumentException e) {
            // Error de preferencia no encontrada (404 Not Found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            HttpStatus.NOT_FOUND,
                            "Preferencia con clave '" + prefKey + "' no encontrada."
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al actualizar la preferencia."
                    ));
        }
    }
}