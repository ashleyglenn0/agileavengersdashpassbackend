package agileavengers.southwest_dashpass.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = System.getenv("SECRET_KEY"); // Retrieve the stored key

    // Method to generate a secure secret key (run this once, not every time)
    // Method to retrieve the key from the environment variable
    private static SecretKeySpec getSecretKey() throws Exception {
        // Retrieve the key from the environment variable
        String secretKeyString = System.getenv("SECRET_KEY");
        System.out.println("Fetched Secret Key: " + secretKeyString);

        if (secretKeyString == null || secretKeyString.isEmpty()) {
            throw new IllegalStateException("Secret key is not set in the environment variables.");
        }

        // Decode the Base64-encoded key
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);

        // Create the secret key using the decoded key bytes
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    // Encryption method
    // Encrypts a string using AES
    public static String encrypt(String strToEncrypt) {
        try {
            // Decode the base64 encoded secret key back into bytes
            byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

            // Create secret key specification using decoded bytes
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

            // Set up the cipher
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Perform encryption
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while encrypting: " + e.toString());
        }
    }

    // Decrypts a string using AES
    public static String decrypt(String strToDecrypt) {
        try {
            // Decode the base64 encoded secret key back into bytes
            byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

            // Create secret key specification using decoded bytes
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

            // Set up the cipher
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Perform decryption
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while decrypting: " + e.toString());
        }
    }
}
