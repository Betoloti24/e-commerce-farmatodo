package com.farmatodo.apigetway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de la creación, validación y extracción de información de los
 * JSON Web Tokens (JWT).
 */
@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.time.ms}")
    private long jwtExpiration;

    /**
     * Genera un JWT para un usuario, incluyendo los claims estándar y una lista de roles.
     *
     * @param userDetails Los detalles del usuario (username, roles, etc.).
     * @return El token JWT como cadena de texto.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList());
        return buildToken(claims, userDetails);
    }

    /**
     * Construye y firma el token JWT final con claims, sujeto, fecha de emisión y expiración.
     *
     * @param extraClaims Claims adicionales a incluir (ej. roles).
     * @param userDetails Los detalles del usuario para establecer el sujeto y la expiración.
     * @return La cadena del JWT firmado.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) del token JWT.
     *
     * @param token El JWT.
     * @return El nombre de usuario.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token.
     *
     * @param token El JWT.
     * @param claimsResolver Función para resolver el claim deseado.
     * @param <T> Tipo del claim a extraer.
     * @return El valor del claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Verifica si un token es válido, chequeando el nombre de usuario y la expiración.
     *
     * @param token El JWT.
     * @param userDetails Los detalles del usuario para comparación.
     * @return {@code true} si el token es válido, {@code false} en caso contrario.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si la fecha de expiración del token es anterior a la fecha actual.
     *
     * @param token El JWT.
     * @return {@code true} si el token ha expirado.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token El JWT.
     * @return La fecha de expiración.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parsea el token y extrae todos los claims.
     *
     * @param token El JWT.
     * @return Un objeto {@link Claims} con todos los datos del payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave de firma secreta a partir de la cadena base64 configurada.
     *
     * @return La clave secreta.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}