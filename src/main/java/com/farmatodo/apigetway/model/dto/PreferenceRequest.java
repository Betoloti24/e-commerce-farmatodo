package com.farmatodo.apigetway.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * DTO utilizado para solicitar la actualización de una preferencia del sistema.
 */
@Data
public class PreferenceRequest {

    /**
     * La clave única de la preferencia del sistema.
     */
    @NotBlank(message = "La clave de la preferencia (prefKey) es obligatoria.")
    private String prefKey;

    /**
     * El nuevo valor que se desea asignar a la preferencia.
     */
    @NotNull(message = "El valor de la preferencia (prefValue) es obligatorio.")
    private String prefValue;

    /**
     * El tipo de dato del valor (Ej: INTEGER, STRING, BOOLEAN).
     */
    @NotBlank(message = "El tipo de dato es obligatorio.")
    private String dataType;
}