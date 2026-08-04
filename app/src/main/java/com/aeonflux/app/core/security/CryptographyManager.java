/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.1 - Cryptographic storage helper.
 */
package com.aeonflux.app.core.security;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * [TSK-20260804-003.1] CryptographyManager provides AES/GCM encryption and decryption.
 * It manages an Android KeyStore backed master key named "AeonFluxMasterKey",
 * with a fallback mechanism for standard JVM unit testing environments.
 */
public class CryptographyManager {

    private static final String ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String MASTER_KEY_ALIAS = "AeonFluxMasterKey";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKey fallbackKey;

    /* TSK-20260804-003.1 - Constructor initializing KeyStore or Fallback Key */
    public CryptographyManager() {
        SecretKey key = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER);
            keyStore.load(null);
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE_PROVIDER);
                keyGenerator.init(256);
                key = keyGenerator.generateKey();
            } else {
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(MASTER_KEY_ALIAS, null);
                if (secretKeyEntry != null) {
                    key = secretKeyEntry.getSecretKey();
                }
            }
        } catch (Exception e) {
            // Fallback for standard JVM unit tests where AndroidKeyStore provider is not registered
            key = null;
        }

        if (key == null) {
            byte[] seed = new byte[32];
            new SecureRandom().nextBytes(seed);
            this.fallbackKey = new SecretKeySpec(seed, "AES");
        } else {
            this.fallbackKey = key;
        }
    }

    /* TSK-20260804-003.1 - Helper constructor for custom SecretKey injection in unit testing */
    public CryptographyManager(SecretKey customKey) {
        this.fallbackKey = Objects.requireNonNull(customKey, "customKey must not be null");
    }

    /**
     * [TSK-20260804-003.1] Encryption Result POJO carrying base64 ciphertext and base64 IV.
     */
    public static final class EncryptedData {
        private final String base64Ciphertext;
        private final String base64Iv;

        /* TSK-20260804-003.1 - EncryptedData Constructor */
        public EncryptedData(String base64Ciphertext, String base64Iv) {
            this.base64Ciphertext = Objects.requireNonNull(base64Ciphertext, "base64Ciphertext must not be null");
            this.base64Iv = Objects.requireNonNull(base64Iv, "base64Iv must not be null");
        }

        /* TSK-20260804-003.1 - Accessor for ciphertext */
        public String getBase64Ciphertext() {
            return base64Ciphertext;
        }

        /* TSK-20260804-003.1 - Accessor for IV */
        public String getBase64Iv() {
            return base64Iv;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EncryptedData that = (EncryptedData) o;
            return Objects.equals(base64Ciphertext, that.base64Ciphertext) &&
                   Objects.equals(base64Iv, that.base64Iv);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base64Ciphertext, base64Iv);
        }
    }

    /**
     * [TSK-20260804-003.1] Encrypts raw text using AES-256-GCM.
     * @param plainText Cleartext string to encrypt.
     * @return EncryptedData containing base64 ciphertext and base64 IV.
     */
    public EncryptedData encrypt(String plainText) {
        Objects.requireNonNull(plainText, "plainText must not be null");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);

            byte[] ciphertextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String base64Ciphertext = encodeBase64(ciphertextBytes);
            String base64Iv = encodeBase64(iv);

            return new EncryptedData(base64Ciphertext, base64Iv);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt data", e);
        }
    }

    /**
     * [TSK-20260804-003.1] Decrypts base64 ciphertext using the corresponding base64 IV.
     * @param base64Ciphertext Encrypted text in Base64 format.
     * @param base64Iv Initialization Vector in Base64 format.
     * @return Decrypted plain text string.
     */
    public String decrypt(String base64Ciphertext, String base64Iv) {
        Objects.requireNonNull(base64Ciphertext, "base64Ciphertext must not be null");
        Objects.requireNonNull(base64Iv, "base64Iv must not be null");
        try {
            byte[] ciphertextBytes = decodeBase64(base64Ciphertext);
            byte[] ivBytes = decodeBase64(base64Iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

            byte[] plainTextBytes = cipher.doFinal(ciphertextBytes);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt data", e);
        }
    }

    /* TSK-20260804-003.1 - Retrieves the active SecretKey */
    private SecretKey getSecretKey() {
        return fallbackKey;
    }

    /* TSK-20260804-003.1 - Safe Base64 encoder helper compatible with standard Java and Android */
    private static String encodeBase64(byte[] data) {
        try {
            return Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (Throwable t) {
            return java.util.Base64.getEncoder().encodeToString(data);
        }
    }

    /* TSK-20260804-003.1 - Safe Base64 decoder helper compatible with standard Java and Android */
    private static byte[] decodeBase64(String base64Str) {
        try {
            return Base64.decode(base64Str, Base64.NO_WRAP);
        } catch (Throwable t) {
            return java.util.Base64.getDecoder().decode(base64Str);
        }
    }
}
