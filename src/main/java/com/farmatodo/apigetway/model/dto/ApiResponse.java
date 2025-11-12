package com.farmatodo.apigetway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * DTO genérico para estandarizar el formato de respuesta de la API.
 * Proporciona un cuerpo consistente que incluye metadatos de la operación
 * (éxito, estado, mensaje) y el payload de datos real.
 *
 * @param <T> El tipo de dato del payload que se retorna en el campo 'data'.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /**
     * Indica si la operación resultó en un error (true) o fue exitosa (false).
     */
    private boolean error;

    /**
     * El código de estado HTTP de la respuesta.
     */
    private int status;

    /**
     * Mensaje descriptivo de la operación o del error.
     */
    private String message;

    /**
     * El payload de datos retornado por el controlador. Su tipo está
     * definido por el genérico T.
     */
    private T data;

    /**
     * Crea una respuesta de éxito.
     * @param status El estado HTTP de la respuesta (ej. 200, 201).
     * @param message El mensaje de la operación.
     * @param data El payload a retornar.
     * @return Una instancia de ApiResponse.
     */
    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .error(false)
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Crea una respuesta de error.
     * @param status El estado HTTP del error (ej. 400, 500).
     * @param message El mensaje de error.
     * @return Una instancia de ApiResponse con data nula.
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .error(true)
                .status(status.value())
                .message(message)
                .data(null)
                .build();
    }
}