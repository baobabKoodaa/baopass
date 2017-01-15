package crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PBKDF2 {

    public static SecretKeySpec generateAESkey(final char[] password, final byte[] salt, int iterations, int keyLength) throws Exception {
        SecretKey secretKey = PBKDF2.generateKey(password, salt, iterations, keyLength);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public static SecretKey generateKey(final char[] secret, int iterations, int keyLengthBytes) throws UnsupportedEncodingException {
        /* Salt is irrelevant for site pass generation and entropy hashing, but required by the library. */
        byte[] salt = "BaoPass!".getBytes("UTF-8");
        return generateKey(secret, salt, iterations, keyLengthBytes);
    }

    public static SecretKey generateKey(final char[] secret, final byte[] salt, int iterations, int keyLengthBytes) {
        try {
            int keyLengthBits = 8 * keyLengthBytes;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(secret, salt, iterations, keyLengthBits);
            SecretKey key = skf.generateSecret(spec);
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}
