import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        String password = "testi";
        String salt = "bah";
        int iterations = 137;
        int keyLength = 12*8;
        String out = PBKDF2(password.toCharArray(), salt.getBytes(), iterations, keyLength);
        System.out.println(out);
    }

    public static String PBKDF2(final char[] password, final byte[] salt, int iterations, int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            return enc.encodeToString(res);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
