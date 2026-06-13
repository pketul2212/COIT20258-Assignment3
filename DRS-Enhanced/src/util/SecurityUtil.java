package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for encryption, decryption, and password hashing.
 * Provides security features: SHA-256 hashing, AES-128 encryption,
 * non-repudiation tokens.
 */
public class SecurityUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "DRS2024SecureKey"; // 16 bytes = AES-128

    /**
     * Hashes a password using SHA-256.
     * @param password plain-text password
     * @return 64-character hex hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Encrypts a string using AES-128.
     * @param plainText text to encrypt
     * @return Base64-encoded encrypted string
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("[Security] Encryption error: " + e.getMessage());
            return plainText;
        }
    }

    /**
     * Decrypts an AES-128 encrypted string.
     * @param encryptedText Base64-encoded encrypted string
     * @return original plain text
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.err.println("[Security] Decryption error: " + e.getMessage());
            return encryptedText;
        }
    }

    /**
     * Generates a non-repudiation token for an action.
     * @param username the acting user
     * @param action the action performed
     * @return 16-character uppercase token
     */
    public static String generateToken(String username, String action) {
        String raw = username + ":" + action + ":" + System.currentTimeMillis();
        return hashPassword(raw).substring(0, 16).toUpperCase();
    }
}
