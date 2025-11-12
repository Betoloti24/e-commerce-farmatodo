package com.farmatodo.apigetway.config;

import com.farmatodo.apigetway.model.Role;
import com.farmatodo.apigetway.model.SystemPreference;
import com.farmatodo.apigetway.repository.RoleRepository;
import com.farmatodo.apigetway.repository.SystemPreferenceRepository;
import com.farmatodo.apigetway.service.AuthService;
import com.farmatodo.apigetway.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente que se ejecuta al inicio de la aplicación para cargar y asegurar
 * la existencia de datos iniciales críticos, como roles de usuario y preferencias
 * del sistema. Implementa {@link CommandLineRunner}.
 */
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    /** Repositorio para la gestión de roles. */
    private final RoleRepository roleRepository;
    /** Repositorio para la gestión de preferencias del sistema. */
    private final SystemPreferenceRepository preferenceRepository;
    /** Logger para registrar eventos de inicialización. */
    private static final Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

    /** Clave de preferencia para la tasa de rechazo simulada de pagos. */
    public static final String PAYMENT_REJECTION_RATE = "payment.rejection_rate";
    /** Clave de preferencia para el stock mínimo requerido para la visibilidad de productos. */
    public static final String PRODUCT_MIN_STOCK = "product.min_stock_visibility";
    /** Clave de preferencia para el número máximo de reintentos de pago fallido. */
    public static final String PAYMENT_MAX_ATTEMPTS = "payment.max_attempts";
    /** Clave de preferencia para la tasa de rechazo simulada de tokenización de tarjetas. */
    public static final String TOKEN_REJECTION_RATE = "tokencard.rejection_rate";

    /** Servicio de notificaciones (inyectado, aunque no usado directamente en la carga de datos). */
    private final NotificationService notificationService;

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     * @throws Exception Si ocurre un error durante la inicialización.
     */
    @Override
    public void run(String... args) throws Exception {
        // Inicialización de Roles
        createRoleIfNotFound(AuthService.DEFAULT_CLIENT_ROLE, "Rol predeterminado para clientes registrados.");
        createRoleIfNotFound("ROLE_ADMIN", "Rol para administradores del sistema.");

        // Inicialización de Preferencias del Sistema
        createPreferenceIfNotFound(
                PAYMENT_REJECTION_RATE,
                "20",
                "INTEGER",
                "Porcentaje entero de probabilidad de rechazo simulado para transacciones de pago."
        );
        createPreferenceIfNotFound(
                PRODUCT_MIN_STOCK,
                "5",
                "INTEGER",
                "Cantidad mínima de stock para que un producto sea visible al cliente."
        );
        createPreferenceIfNotFound(
                PAYMENT_MAX_ATTEMPTS,
                "3",
                "INTEGER",
                "Número máximo de reintentos permitidos para una transacción de pago fallida."
        );
        createPreferenceIfNotFound(
                TOKEN_REJECTION_RATE,
                "20",
                "INTEGER",
                "Porcentaje entero de probabilidad de rechazo simulado durante la generación del token."
        );
    }

    /**
     * Crea un rol en la base de datos si no existe uno con el nombre especificado.
     *
     * @param name Nombre del rol a crear (ej. "ROLE_CLIENT").
     * @param description Descripción del rol.
     */
    private void createRoleIfNotFound(String name, String description) {
        Optional<Role> roleOpt = roleRepository.findByName(name);
        if (roleOpt.isEmpty()) {
            Role newRole = new Role();
            newRole.setName(name);
            newRole.setDescription(description);
            roleRepository.save(newRole);
            log.info("💾 Creado rol inicial: {}", name);
        } else {
            log.debug("Rol {} ya existe.", name);
        }
    }

    /**
     * Crea una preferencia del sistema en la base de datos si no existe una con la clave especificada.
     *
     * @param key La clave única de la preferencia (ej. "payment.rejection_rate").
     * @param value El valor inicial de la preferencia.
     * @param dataType El tipo de dato del valor (ej. "INTEGER", "STRING").
     * @param description Descripción de la preferencia.
     */
    private void createPreferenceIfNotFound(String key, String value, String dataType, String description) {
        Optional<SystemPreference> prefOpt = preferenceRepository.findByPrefKey(key);
        if (prefOpt.isEmpty()) {
            SystemPreference newPref = new SystemPreference();
            newPref.setPrefKey(key);
            newPref.setPrefValue(value);
            newPref.setDataType(dataType);
            newPref.setUpdateDate(null); // Es nuevo

            preferenceRepository.save(newPref);
            log.info("⚙️ Creada preferencia inicial: {}", key);
        } else {
            log.debug("Preferencia {} ya existe.", key);
        }
    }
}