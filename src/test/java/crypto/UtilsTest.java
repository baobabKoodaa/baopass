package crypto;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    /** Test Base64URLSafe conversions with different sized inputs. */
    @Test
    public void testUrlSafeConversions() {
        SecureRandom rng = new SecureRandom();
        for (int i=1; i<100; i++) {
            /* Fill array with i random bytes. */
            byte[] orig = new byte[i];
            rng.nextBytes(orig);

            /* Convert to Base64Urlsafe encoding. Conversion ok? */
            char[] chars = Utils.getUrlSafeCharsFromBytes(orig);
            assertTrue(urlSafe(chars));

            /* Convert back to bytes. Do we get the same array back? */
            byte[] back = Utils.getBytesFromUrlSafeChars(chars);
            for (int j=0; j<i; j++) {
                assertTrue(orig[j] == back[j]);
            }
        }
    }

    /** Returns true if given array looks like a Base64URLSafe encoding. */
    private boolean urlSafe(char[] chars) {
        int j = 0;
        for (; j<chars.length; j++) {
            char c = chars[j];
            if (c >= '0' && c <= '9') continue;
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c == '-' || c == '_') continue;
            break;
        }
        for (; j<chars.length; j++) {
            /* End of array may contain '=' */
            char c = chars[j];
            if (c == '=') continue;
            break;
        }
        return (j==chars.length);
    }

}
