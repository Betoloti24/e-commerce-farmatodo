package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementación de {@link UserDetailsService} de Spring Security.
 *
 * Se utiliza para cargar los detalles del cliente (usuario) a partir de su nombre
 * de usuario durante el proceso de autenticación.
 *
 */
@Service
@RequiredArgsConstructor
public class ClientDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    /**
     * Carga el usuario por nombre de usuario. Método requerido por Spring Security.
     *
     * @param username El nombre de usuario del cliente.
     * @return Un objeto {@link UserDetails} (instancia de {@link User}) con la información de seguridad.
     * @throws UsernameNotFoundException Si el cliente no es encontrado en la base de datos.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Collection<? extends GrantedAuthority> authorities = client.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new User(
                client.getUsername(),
                client.getPasswordHash(),
                client.getIsActive(), // Cuenta activa
                true, // Cuenta no expirada
                true, // Credenciales no expiradas
                true, // Cuenta no bloqueada
                authorities
        );
    }
}