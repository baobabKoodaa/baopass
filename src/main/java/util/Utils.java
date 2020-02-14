package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    public static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd__HH_mm_ss");
        return dateFormat.format(new Date());
    }

    public static char[] getCharsFromBytes(final byte[] bytesIn) {
        char[] charsOut = new char[bytesIn.length];
        for (int i=0; i<bytesIn.length; i++) {
            charsOut[i] = (char) (bytesIn[i] & 0xFF);
        }
        return charsOut;
    }

    /* Modified from http://stackoverflow.com/a/9670279/4490400 . */
    public static byte[] getBytesFromChars(final char[] charsIn) {
        CharBuffer charBuffer = CharBuffer.wrap(charsIn);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytesOut = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytesOut;
    }

    /** Note that output size is larger than input due to conversion.
     *  Also note that last chars may be '=' due to Encoder's padding. */
    public static char[] getUrlSafeCharsFromBytes(final byte[] bytesIn) {
        Base64.Encoder enc = Base64.getUrlEncoder();
        byte[] bytesOut = enc.encode(bytesIn);
        char[] charsOut = getCharsFromBytes(bytesOut);
        return charsOut;
    }

    /** Compatible with the output from the method above. */
    public static byte[] getBytesFromUrlSafeChars(final char[] charsIn) {
        byte[] bytesIn = getBytesFromChars(charsIn);
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
        if (b == null || b.length == 0) return;
        for (int i=0; i<b.length; i++) {
            b[i] = '0';
        }
        /* Below line is just to stop compilers from 'optimizing' this method away. */
        if (b[b.length - b[0] + b[b.length-1] - 1] != '0') throw new RuntimeException();
    }

    public static void wipe(char[] b) {
        if (b == null || b.length == 0) return;
        for (int i=0; i<b.length; i++) {
            b[i] = '0';
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


    public static boolean charArrayEquals(char[] a, char[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i=0; i<a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public static boolean byteArrayEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i=0; i<a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public static Map<String, String> getMapFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file, "UTF-8");
        HashMap<String, String> map = new HashMap<>();
        scanner.nextLine(); // first line is general info.
        while (scanner.hasNext()){
            String[] line = scanner.nextLine().split(":", 2);
            String key = line[0];
            String val = line[1];
            map.put(key, val);
        }
        scanner.close();
        return map;
    }

}
