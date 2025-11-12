package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.model.Role;
import com.farmatodo.apigetway.model.dto.LoginRequest;
import com.farmatodo.apigetway.model.dto.RegisterRequest;
import com.farmatodo.apigetway.repository.ClientRepository;
import com.farmatodo.apigetway.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;
import java.util.UUID;

/**
 * Servicio encargado de la lógica de negocio para el registro y la autenticación
 * de clientes, integrando Spring Security y JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService clientDetailsService;
    private final JwtService jwtService;

    /**
     * Rol predeterminado asignado a los clientes recién registrados.
     */
    public final static String DEFAULT_CLIENT_ROLE = "ROLE_CLIENT";

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * @param request DTO con los datos de registro del nuevo cliente.
     * @return La entidad {@link Client} guardada en la base de datos.
     * @throws IllegalArgumentException Si el nombre de usuario ya existe.
     * @throws IllegalStateException Si el rol por defecto no se encuentra configurado.
     */
    @Transactional
    public Client registerNewClient(RegisterRequest request) {
        if (clientRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe.");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_CLIENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("El rol por defecto no está configurado."));

        Client client = new Client();
        client.setUsername(request.getUsername());
        client.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        client.setEmail(request.getEmail());
        client.setFirstName(request.getFirstName());
        client.setFirstSurname(request.getFirstSurname());
        client.setRoles(Set.of(defaultRole));

        return clientRepository.save(client);
    }

    /**
     * Autentica a un cliente utilizando sus credenciales y genera un token JWT.
     *
     * @param request DTO con el nombre de usuario y la contraseña.
     * @return El token JWT generado para el cliente autenticado.
     */
    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = clientDetailsService.loadUserByUsername(request.getUsername());
        return jwtService.generateToken(userDetails);
    }

    /**
     * Obtiene el ID único de un cliente a partir de su nombre de usuario.
     *
     * @param username El nombre de usuario del cliente.
     * @return El ID (UUID) del cliente.
     * @throws UsernameNotFoundException Si el cliente con el nombre de usuario no existe.
     */
    public UUID getClientIdByUsername(String username) {
        return clientRepository.findByUsername(username)
                .map(Client::getId)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente con username " + username + " no encontrado."));
    }
}