package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.SystemPreference;
import com.farmatodo.apigetway.model.dto.PreferenceRequest;
import com.farmatodo.apigetway.repository.SystemPreferenceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * Servicio encargado de la gestión de las preferencias de configuración dinámica
 * del sistema.
 */
@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final SystemPreferenceRepository preferenceRepository;

    /**
     * Busca una preferencia por su clave.
     *
     * @param key Clave de la preferencia.
     * @return La entidad {@link SystemPreference}.
     * @throws IllegalArgumentException Si la preferencia no es encontrada.
     */
    @Transactional(readOnly = true)
    public SystemPreference findPreferenceByKey(String key) {
        return preferenceRepository.findByPrefKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Preferencia con clave '" + key + "' no encontrada."));
    }

    /**
     * Crea una nueva preferencia en el sistema.
     *
     * @param request DTO con los datos de la nueva preferencia.
     * @return La entidad {@link SystemPreference} guardada.
     * @throws IllegalArgumentException Si la clave de preferencia ya existe.
     */
    @Transactional
    public SystemPreference createPreference(PreferenceRequest request) {
        if (preferenceRepository.findByPrefKey(request.getPrefKey()).isPresent()) {
            throw new IllegalArgumentException("La clave de preferencia '" + request.getPrefKey() + "' ya existe.");
        }

        SystemPreference preference = new SystemPreference();
        preference.setPrefKey(request.getPrefKey());
        preference.setPrefValue(request.getPrefValue());
        preference.setDataType(request.getDataType());

        return preferenceRepository.save(preference);
    }

    /**
     * Actualiza el valor y el tipo de dato de una preferencia existente.
     *
     * @param key Clave de la preferencia a actualizar.
     * @param request DTO con el nuevo valor y tipo de dato.
     * @return La entidad {@link SystemPreference} actualizada.
     * @throws IllegalArgumentException Si la preferencia no es encontrada.
     */
    @Transactional
    public SystemPreference updatePreference(String key, PreferenceRequest request) {
        SystemPreference existingPref = preferenceRepository.findByPrefKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Preferencia con clave '" + key + "' no encontrada para actualizar."));

        existingPref.setPrefValue(request.getPrefValue());
        existingPref.setDataType(request.getDataType());
        existingPref.setUpdateDate(ZonedDateTime.now());

        return preferenceRepository.save(existingPref);
    }

    /**
     * Obtiene el valor de una preferencia y lo convierte a un entero.
     *
     * @param key Clave de la preferencia.
     * @return El valor de la preferencia como {@link Integer}.
     * @throws IllegalArgumentException Si la preferencia no existe o su valor no es un entero válido.
     */
    @Transactional(readOnly = true)
    public Integer getPreferenceValueAsInteger(String key) {
        SystemPreference preference = findPreferenceByKey(key);
        try {
            return Integer.parseInt(preference.getPrefValue());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "ERROR: El valor de la preferencia '" + key + "' no es un entero válido: " + preference.getPrefValue()
            );
        }
    }
}