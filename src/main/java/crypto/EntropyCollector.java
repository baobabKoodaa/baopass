package crypto;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
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

    /** Appends given object to buffer. Should be called with mouse coordinates, nanotimes, etc. */
    public void collect(Object o) {
        if (buffer.length() < 1000000) {
            buffer.append(o);
        }
    }

    /** Returns a hash of buffer contents, clears buffer. */
    public byte[] consume(int outputLengthInBytes) throws UnsupportedEncodingException {
        /* If buffer is low on entropy, ask user to move the mouse. */
        if (buffer.length() < 1000) {
            throw new RuntimeException("Low on entropy!");
        }

        /* Turn buffer into char array without creating Strings. */
        char[] charBuffer = new char[buffer.length()];
        for (int i=0; i<buffer.length(); i++) {
            charBuffer[i] = buffer.charAt(i);
        }

        /* Hash buffer contents. */
        int iterations = 100000;
        byte[] out = PBKDF2.generateKey(charBuffer, iterations, outputLengthInBytes).getEncoded();

        /* Minimize data lifetime. */
        for (int i=0; i<buffer.length(); i++) {
            buffer.replace(i, i+1, "0");
            charBuffer[i] = '0';
        }
        buffer = new StringBuilder();
        return out;
    }
}
