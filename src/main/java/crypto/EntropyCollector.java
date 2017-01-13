package crypto;

import javax.crypto.SecretKey;
import java.util.Arrays;

/** Purpose of this class is to collect additional entropy from user actions
 * such as mouse movements and key strokes. This additional entropy is
 * used to construct a part of the master key. It is essentially insurance
 * in case the main entropy source is corrupted. */
public class EntropyCollector {
    private StringBuilder buffer;

    public EntropyCollector() {
        buffer = new StringBuilder();
    }

    public void collect(Object o) {
        if (buffer.length() < 1000000) {
            buffer.append(""+o);
        }
    }

    /** Returns a hash of buffer contents, clears buffer. */
    public byte[] consume(int outputLengthInBytes) {
        if (buffer.length() < 1000) {
            throw new RuntimeException("Out of entropy!");
        }
        char[] charBuffer = new char[buffer.length()];
        for (int i=0; i<buffer.length(); i++) {
            charBuffer[i] = buffer.charAt(i);
        }

        /* Create part of the master key from buffer contents. */
        SecretKey keyPart = PBKDF2.generateKey(charBuffer, 100000, outputLengthInBytes);
        byte[] out = keyPart.getEncoded();

        /* Overwrite buffer values from memory to minimize data lifetime. */
        for (int i=0; i<buffer.length(); i++) {
            buffer.replace(i, i+1, "0");
            charBuffer[i] = '0';
        }
        buffer = new StringBuilder();

        System.out.println("Returning size " + out.length + " contents: " + Arrays.toString(out));
        return out;
    }
}
