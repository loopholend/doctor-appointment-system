package common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing utility using PBKDF2WithHmacSHA256.
 * Hashes are stored as "base64(salt):base64(hash)".
 * Includes transparent migration support: if a stored password does not
 * contain ":", it is treated as a legacy plaintext password.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a plaintext password using PBKDF2 with a random salt.
     * @return "base64(salt):base64(hash)"
     */
    public static String hashPassword(String plainPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored hash.
     * Supports legacy plaintext passwords for transparent migration.
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        if (storedValue == null || plainPassword == null) return false;

        // Legacy plaintext support: if no ":" separator, compare directly
        if (!storedValue.contains(":")) {
            return storedValue.equals(plainPassword);
        }

        try {
            String[] parts = storedValue.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(
                plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            // Constant-time comparison to prevent timing attacks
            if (actualHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < actualHash.length; i++) {
                diff |= actualHash[i] ^ expectedHash[i];
            }
            return diff == 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the stored value is already a PBKDF2 hash.
     */
    public static boolean isHashed(String storedValue) {
        return storedValue != null && storedValue.contains(":");
    }
}
