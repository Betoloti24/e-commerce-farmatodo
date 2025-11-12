package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.SystemPreference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link SystemPreference}.
 * Permite acceder a las configuraciones dinámicas (preferencias) del sistema.
 */
public interface SystemPreferenceRepository extends JpaRepository<SystemPreference, UUID> {

    /**
     * Busca una preferencia del sistema basada en su clave única.
     *
     * @param prefKey La clave de la preferencia (ej. "payment.rejection_rate").
     * @return Un {@link Optional} que contiene el {@link SystemPreference} si se encuentra.
     */
    Optional<SystemPreference> findByPrefKey(String prefKey);
}