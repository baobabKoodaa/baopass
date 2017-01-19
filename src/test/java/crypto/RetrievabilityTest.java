package crypto;

import app.BaoPass;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Make sure reversible operations are actually reversible. */
public class RetrievabilityTest {


    /** Test reversibility of operations on master key (decryption, file ops) */
    @Test
    public void masterKeyAlwaysRetrievableTest() throws Exception {
        Utils.hackCryptographyExportRestrictions();
        BaoPass baoPass = new BaoPass(new EntropyCollector());
        SecureRandom rng = new SecureRandom();
        byte[] masterKeyBytes = new byte[384/8];
        rng.nextBytes(masterKeyBytes);
        char[] originalMasterKeyChars = Utils.getUrlSafeCharsFromBytes(masterKeyBytes);
        masterKeyBytes = new String(originalMasterKeyChars).getBytes();
        for (int passwordLength=1; passwordLength<100; passwordLength++) {
            byte[] masterPassBytes = new byte[passwordLength];
            rng.nextBytes(masterPassBytes);
            char[] masterPassChars = new String(masterPassBytes).toCharArray();
            EncryptedMessage enc = AES.encrypt(masterKeyBytes, masterPassChars);
            baoPass.setMasterKeyEncrypted(enc);
            baoPass.decryptMasterKey(new String(masterPassChars));
            int end = originalMasterKeyChars.length;
            char[] returnedChars = baoPass.getMasterKeyPlainText();

            /* Test that decrypting an encrypted master key returns the original. */
            assertEquals(originalMasterKeyChars.length, returnedChars.length);
            for (int i=0; i<end; i++) {
                assertEquals(originalMasterKeyChars[i], returnedChars[i]);
            }

            /* File ops: Test that loading a saved EncryptedMessage returns the original. */
            enc.saveToFile("target/temp.txt");
            EncryptedMessage enc2 = new EncryptedMessage(new File("target/temp.txt"));
            assertEquals(enc, enc2);
        }
    }

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

    // TODO: test we are actually using master key to generate site pass (rather than encrypted master key or something)

}
