package crypto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Utils {

    /** Note that output size is larger than input due to conversion.
     *  Also note that last chars may be '=' due to Encoder's padding. */
    public static char[] getUrlSafeCharsFromBytes(final byte[] bytesIn) {
        Base64.Encoder enc = Base64.getUrlEncoder();
        byte[] bytesOut = enc.encode(bytesIn);
        char[] charsOut = new char[bytesOut.length];
        for (int i=0; i<bytesOut.length; i++) {
            charsOut[i] = (char) bytesOut[i];
        }
        return charsOut;
    }

    /** Compatible with the output from the method above. */
    public static byte[] getBytesFromUrlSafeChars(final char[] charsIn) {
        byte[] bytesIn = new byte[charsIn.length];
        for (int i=0; i<charsIn.length; i++) {
            bytesIn[i] = (byte) charsIn[i];
        }
        Base64.Decoder dec = Base64.getUrlDecoder();
        byte[] bytesOut = dec.decode(bytesIn);
        return bytesOut;
    }

    /** Requests the underlying operating system for random bytes
     *  from the most secure entropy source available. May be blocking. */
    public static byte[] requestRandomBytesFromOS(int outputLengthInBytes) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        return secureRandom.generateSeed(outputLengthInBytes);
    }

    public static void wipe(byte[] b) {
        for (int i=0; i<b.length; i++) {
            b[i] = b[i] = '0';
        }
        /* Below line is just to stop compilers from 'optimizing' this method away. */
        if (b[b.length - b[0] + b[b.length-1] - 1] != '0') throw new RuntimeException();
    }

    public static void wipe(char[] b) {
        for (int i=0; i<b.length; i++) {
            b[i] = b[i] = '0';
        }
        /* Below line is just to stop compilers from 'optimizing' this method away. */
        if (b[b.length - b[0] + b[b.length-1] - 1] != '0') throw new RuntimeException();
    }

    public static byte[] combine(byte[] a, byte[] b, boolean overwriteOriginals) {
        byte[] out = new byte[a.length + b.length];
        move(a, out, 0, overwriteOriginals);
        move(b, out, a.length, overwriteOriginals);
        return out;
    }

    public static char[] combine(char[] a, char[] b, boolean overwriteOriginals) {
        char[] out = new char[a.length + b.length];
        move(a, out, 0, overwriteOriginals);
        move(b, out, a.length, overwriteOriginals);
        return out;
    }

    public static void move(byte[] src, byte[] dest, int start, boolean overwriteOriginal) {
        for (int i=0; i<src.length; i++) {
            dest[start+i] = src[i];
        }
        if (overwriteOriginal) {
            wipe(src);
        }
    }

    /** Moves data from one array to another, wipes original. */
    public static void move(char[] src, char[] dest, int start, boolean overwriteOriginal) {
        for (int i=0; i<src.length; i++) {
            dest[start+i] = src[i];
        }
        if (overwriteOriginal) {
            wipe(src);
        }
    }


    /** This hack disables export restrictions on cryptography, allowing us to
     *  use 256-bit AES with GCM mode with normal JRE configurations.
     *  Not guaranteed to work with future JRE versions. If that happens, an exception
     *  will be thrown during cryptographic operations. The exception will be
     *  caught and the user should be directed to install a Cryptographic
     *  Policy Extension pack from Oracle. Source for this hack:
     *  http://stackoverflow.com/a/21148472/4490400 */
    public static void hackCryptographyExportRestrictions() throws Exception {
        Field gate = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
        gate.setAccessible(true);
        gate.setBoolean(null, false);
        Field allPerm = Class.forName("javax.crypto.CryptoAllPermission").getDeclaredField("INSTANCE");
        allPerm.setAccessible(true);
        Object accessAllAreasCard = allPerm.get(null);
        final Constructor<?> constructor = Class.forName("javax.crypto.CryptoPermissions").getDeclaredConstructor();
        constructor.setAccessible(true);
        Object coll = constructor.newInstance();
        Method addPerm = Class.forName("javax.crypto.CryptoPermissions").getDeclaredMethod("add", java.security.Permission.class);
        addPerm.setAccessible(true);
        addPerm.invoke(coll, accessAllAreasCard);
        Field defaultPolicy = Class.forName("javax.crypto.JceSecurity").getDeclaredField("defaultPolicy");
        defaultPolicy.setAccessible(true);
        defaultPolicy.set(null, coll);
    }
}
