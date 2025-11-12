package com.farmatodo.apigetway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio encargado de las operaciones de cifrado y descifrado de datos sensibles
 * utilizando el algoritmo AES con modo GCM (Galois/Counter Mode), proporcionando
 * autenticación y confidencialidad.
 */
@Service
public class EncryptionService {

    private static final Logger LOGGER = Logger.getLogger(EncryptionService.class.getName());

    @Value("${crypto.aes.secret.key}")
    private String base64SecretKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16; // Longitud del tag de autenticación en bytes (128 bits)
    private static final int GCM_IV_LENGTH = 12;  // Longitud del vector de inicialización (IV) en bytes (96 bits)

    /**
     * Decodifica la clave secreta base64 y la convierte a un objeto {@link SecretKeySpec}.
     *
     * @return La clave secreta para AES.
     * @throws IllegalStateException Si la clave configurada no es de 32 bytes (256 bits).
     */
    private SecretKey getSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
        if (decodedKey.length != 32) {
            throw new IllegalStateException("La clave AES debe ser de 32 bytes (256 bits).");
        }
        return new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * Cifra una cadena de texto plano.
     *
     * Utiliza AES/GCM e incluye un IV generado aleatoriamente al inicio de los datos cifrados.
     *
     * @param plainText La cadena a cifrar.
     * @return La cadena cifrada, codificada en Base64 (incluyendo IV + datos cifrados).
     * @throws RuntimeException Si ocurre un error de cifrado (e.g., clave incorrecta).
     */
    public String encrypt(String plainText) {
        try {
            SecretKey key = getSecretKey();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // Generación de IV aleatorio

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Concatenar IV y texto cifrado
            byte[] encryptedData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cifrar datos.", e);
            throw new RuntimeException("Error al cifrar datos", e);
        }
    }

    /**
     * Descifra una cadena de datos cifrados en Base64.
     *
     * Espera que los datos cifrados contengan el IV al inicio.
     *
     * @param encryptedData La cadena cifrada y codificada en Base64.
     * @return La cadena de texto plano descifrada.
     * @throws RuntimeException Si ocurre un error de descifrado (e.g., clave incorrecta o datos corruptos).
     * @throws IllegalArgumentException Si los datos cifrados son incompletos o tienen formato incorrecto.
     */
    public String decrypt(String encryptedData) {
        try {
            SecretKey key = getSecretKey();
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            if (decodedData.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Datos cifrados incompletos o inválidos.");
            }
            // Extraer IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);

            // Extraer texto cifrado (excluyendo IV)
            byte[] cipherText = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText, "UTF-8");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al descifrar datos. Posiblemente clave incorrecta o datos corruptos.", e);
            throw new RuntimeException("Error de descifrado. Datos de tarjeta posiblemente inválidos.", e);
        }
    }
}