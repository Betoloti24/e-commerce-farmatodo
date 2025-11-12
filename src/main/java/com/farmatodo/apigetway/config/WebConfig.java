package com.farmatodo.apigetway.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC para registrar interceptores y otras personalizaciones.
 *
 * Implementa {@link WebMvcConfigurer} para añadir el {@link SearchLogInterceptor}
 * a rutas específicas.
 *
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /** Interceptor para el registro de búsquedas. */
    private final SearchLogInterceptor searchLogInterceptor;

    /**
     * Registra interceptores de la aplicación.
     *
     * @param registry El registro de interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra el SearchLogInterceptor y lo aplica solo a la ruta de búsqueda de productos
        registry.addInterceptor(searchLogInterceptor)
                .addPathPatterns("/api/v1/products/search");
    }
}