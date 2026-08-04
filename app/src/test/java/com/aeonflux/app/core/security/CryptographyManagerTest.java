/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.1 - Unit test for CryptographyManager.
 */
package com.aeonflux.app.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * [TSK-20260804-003.1] Unit tests for CryptographyManager validating encrypt/decrypt and null rejection.
 */
public class CryptographyManagerTest {

    private CryptographyManager cryptoManager;

    @Before
    public void setUp() {
        cryptoManager = new CryptographyManager();
    }

    @Test
    public void testEncryptAndDecryptSuccess() {
        String originalText = "AeonFlux-Secret-Token-123456";
        CryptographyManager.EncryptedData encryptedData = cryptoManager.encrypt(originalText);

        assertNotNull("EncryptedData must not be null", encryptedData);
        assertNotNull("Base64Ciphertext must not be null", encryptedData.getBase64Ciphertext());
        assertNotNull("Base64Iv must not be null", encryptedData.getBase64Iv());
        assertNotEquals("Ciphertext should not equal original text", originalText, encryptedData.getBase64Ciphertext());

        String decryptedText = cryptoManager.decrypt(encryptedData.getBase64Ciphertext(), encryptedData.getBase64Iv());
        assertEquals("Decrypted text must match original plain text", originalText, decryptedText);
    }

    @Test
    public void testEncryptNullRejection() {
        try {
            cryptoManager.encrypt(null);
            fail("Expected NullPointerException when passing null to encrypt");
        } catch (NullPointerException expected) {
            assertEquals("plainText must not be null", expected.getMessage());
        }
    }

    @Test
    public void testDecryptNullRejection() {
        try {
            cryptoManager.decrypt(null, "someIv");
            fail("Expected NullPointerException when passing null ciphertext to decrypt");
        } catch (NullPointerException expected) {
            assertEquals("base64Ciphertext must not be null", expected.getMessage());
        }

        try {
            cryptoManager.decrypt("someCipher", null);
            fail("Expected NullPointerException when passing null IV to decrypt");
        } catch (NullPointerException expected) {
            assertEquals("base64Iv must not be null", expected.getMessage());
        }
    }

    @Test
    public void testEncryptedDataEqualsAndHashCode() {
        CryptographyManager.EncryptedData data1 = new CryptographyManager.EncryptedData("cipher1", "iv1");
        CryptographyManager.EncryptedData data2 = new CryptographyManager.EncryptedData("cipher1", "iv1");
        CryptographyManager.EncryptedData data3 = new CryptographyManager.EncryptedData("cipher2", "iv1");

        assertEquals("Same fields should yield equals == true", data1, data2);
        assertEquals("Same fields should yield same hashCode", data1.hashCode(), data2.hashCode());
        assertNotEquals("Different fields should yield equals == false", data1, data3);
    }
}
