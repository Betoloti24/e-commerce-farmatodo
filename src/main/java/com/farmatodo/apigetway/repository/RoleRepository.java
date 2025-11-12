package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link Role}.
 * Gestiona los roles de seguridad de la aplicación.
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Busca un rol basado en su nombre único (ej. "ROLE_CLIENT").
     *
     * Utilizado durante la inicialización de datos y la gestión de permisos.
     *
     * @param name El nombre del rol.
     * @return Un {@link Optional} que contiene el {@link Role} si se encuentra.
     */
    Optional<Role> findByName(String name);
}