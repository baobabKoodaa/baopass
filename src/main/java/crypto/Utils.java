package crypto;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static void writeEncryptedMessageToFile(EncryptedMessage msg) {
        try {
            FileOutputStream fos = new FileOutputStream("test.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(msg);
            oos.close();
            fos.close();
        } catch (Exception ex) {
            System.out.println("Error " + ex.toString());
        }
    }

    public static EncryptedMessage readEncryptedMessageFromFile(String filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            EncryptedMessage msg = (EncryptedMessage) ois.readObject();
            ois.close();
            fis.close();
            return msg;
        } catch (Exception ex) {
            System.out.println("Error " + ex.toString());
            return null;
        }

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
        moveSafely(a, out, 0, overwriteOriginals);
        moveSafely(b, out, a.length, overwriteOriginals);
        return out;
    }

    public static char[] combine(char[] a, char[] b, boolean overwriteOriginals) {
        char[] out = new char[a.length + b.length];
        moveSafely(a, out, 0, overwriteOriginals);
        moveSafely(b, out, a.length, overwriteOriginals);
        return out;
    }

    public static void moveSafely(byte[] src, byte[] dest, int start, boolean overwriteOriginals) {
        for (int i=0; i<src.length; i++) {
            dest[start+i] = src[i];
        }
        if (overwriteOriginals) {
            wipe(src);
        }
    }

    /** Moves data from one array to another, wipes original. */
    public static void moveSafely(char[] src, char[] dest, int start, boolean overwriteOriginals) {
        for (int i=0; i<src.length; i++) {
            dest[start+i] = src[i];
        }
        if (overwriteOriginals) {
            wipe(src);
        }
    }


    /** Standard JRE configuration does not allow 256-bit AES with CGM mode
     *  due to export restrictions on cryptography. This hack disables restrictions.
     *  It may break with future JRE versions. If that happens, an exception
     *  will be thrown during cryptographic operations. The exception should be
     *  caught and the user should be directed to install a Cryptographic
     *  Policy Extension pack from Oracle. Source for this hack:
     *  http://stackoverflow.com/a/21148472/4490400 */
    public static void hackCryptographyExportRestrictions() throws Exception {
        Field gate = Class.forName("javax.main.baopass.crypto.JceSecurity").getDeclaredField("isRestricted");
        gate.setAccessible(true);
        gate.setBoolean(null, false);
        Field allPerm = Class.forName("javax.main.baopass.crypto.CryptoAllPermission").getDeclaredField("INSTANCE");
        allPerm.setAccessible(true);
        Object accessAllAreasCard = allPerm.get(null);
        final Constructor<?> constructor = Class.forName("javax.main.baopass.crypto.CryptoPermissions").getDeclaredConstructor();
        constructor.setAccessible(true);
        Object coll = constructor.newInstance();
        Method addPerm = Class.forName("javax.main.baopass.crypto.CryptoPermissions").getDeclaredMethod("add", java.security.Permission.class);
        addPerm.setAccessible(true);
        addPerm.invoke(coll, accessAllAreasCard);
        Field defaultPolicy = Class.forName("javax.main.baopass.crypto.JceSecurity").getDeclaredField("defaultPolicy");
        defaultPolicy.setAccessible(true);
        defaultPolicy.set(null, coll);
    }
}
