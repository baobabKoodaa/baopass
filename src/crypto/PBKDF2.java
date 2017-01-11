package crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PBKDF2 {

    public static SecretKey PBKDF2(final char[] secret, final byte[] salt, int iterations, int keyLengthBytes) {
        try {
            int keyLengthBits = 8 * keyLengthBytes;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(secret, salt, iterations, keyLengthBits);
            SecretKey key = skf.generateSecret(spec);
            return key;
        } catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}
