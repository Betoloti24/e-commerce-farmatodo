package com.farmatodo.apigetway.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuración principal de seguridad para la aplicación Spring Boot.
 *
 * Habilita la seguridad web y define dos cadenas de filtros separadas para manejar
 * la autenticación basada en JWT y la autenticación basada en API Key.
 *
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filtro para la autenticación basada en JWT. */
    private final JwtAuthFilter jwtAuthFilter;

    /** Valor secreto de la API Key para el servicio de tokenización, inyectado desde la configuración. */
    @Value("${security.api-key.tokenization}")
    private String tokenizationApiKeyValue;

    /** URLs accesibles sin autenticación. */
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/ping",
    };

    /** URLs que requieren autenticación mediante JWT. */
    private static final String[] JWT_URLS = {
            "/api/v1/card/**",
            "/api/v1/products",
            "/api/v1/products/**",
            "/api/v1/orders",
            "/api/v1/orders/**",
            "/api/v1/cart",
            "/api/v1/cart/**",
            "/api/v1/preferences",
            "/api/v1/preferences/**",
            "/api/v1/search-log/**"
    };

    /** URLs que requieren autenticación mediante API Key para el servicio de tokenización. */
    String[] API_KEY_URLS = {
            "/api/v1/card",
            "/api/v1/tokenize"
    };

    /**
     * Provee el bean del filtro de autenticación por API Key, inicializado con el valor secreto.
     *
     * @return Una nueva instancia de {@link ApiKeyAuthFilter}.
     */
    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter() {
        return new ApiKeyAuthFilter(tokenizationApiKeyValue);
    }

    /**
     * Define la cadena de filtros de seguridad para la autenticación por API Key.
     *
     * Tiene {@code @Order(1)} para ser evaluada antes que la cadena JWT.
     *
     * @param http Configuración de seguridad HTTP.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error de configuración de seguridad.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher(API_KEY_URLS)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, API_KEY_URLS).hasAuthority("ROLE_TOKENIZATION_SERVICE")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(apiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Define la cadena de filtros de seguridad para la autenticación por JWT.
     *
     * Tiene {@code @Order(2)} y se aplica a todas las demás rutas.
     *
     * @param http Configuración de seguridad HTTP.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error de configuración de seguridad.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/**") // Aplica a todas las rutas no cubiertas por el filtro anterior
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_URLS).permitAll() // Rutas públicas
                        .requestMatchers(JWT_URLS).authenticated() // Rutas que requieren JWT
                        .anyRequest().authenticated() // Cualquier otra ruta también requiere autenticación (si no es pública)
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Provee el bean del codificador de contraseñas utilizando BCrypt.
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provee el bean del gestor de autenticación.
     *
     * @param configuration La configuración de autenticación.
     * @return Una instancia de {@link AuthenticationManager}.
     * @throws Exception Si ocurre un error al obtener el gestor.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configura y provee el filtro CORS (Cross-Origin Resource Sharing).
     *
     * @return Una instancia de {@link CorsFilter} configurada.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Permite todos los orígenes
        config.addAllowedHeader("*");       // Permite todos los encabezados
        config.addAllowedMethod("*");       // Permite todos los métodos (GET, POST, etc.)
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}