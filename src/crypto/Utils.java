package crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Utils {

    /** Note that output size is larger than input due to conversion.
     *  Also note that last chars may be '=' due to Encoder's padding. */
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
