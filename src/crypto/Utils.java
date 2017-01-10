package crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class Utils {

    public static byte[] PBKDF2(final char[] secret, final byte[] salt, int iterations, int keyLengthBytes) {
        try {
            int keyLengthBits = 8 * keyLengthBytes;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(secret, salt, iterations, keyLengthBits);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        } catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /** Note that output size is larger than input due to conversion.
     *  Also note that last chars may be '=' due to Encoder's padding
     *  ...so truncate the result when printing to user. */
    public static char[] getUrlSafeCharsFromBytes(byte[] bytes) {
        Base64.Encoder enc = Base64.getUrlEncoder();
        byte[] keyTransformed = enc.encode(bytes);
        char[] out = new char[keyTransformed.length];
        for (int i=0; i<keyTransformed.length; i++) {
            out[i] = (char) keyTransformed[i];
        }
        return out;
    }

    /** Requests the underlying operating system for random bytes
     *  from the most secure entropy source available. May be blocking. */
    public static byte[] requestRandomBytesFromOS(int outputLengthInBytes) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        return secureRandom.generateSeed(outputLengthInBytes);
    }
}
