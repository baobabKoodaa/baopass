package crypto;

import app.CoreService;
import org.junit.jupiter.api.Test;
import util.Utils;

import java.io.File;
import java.security.SecureRandom;
import java.util.Random;

import static util.Utils.byteArrayEquals;
import static util.Utils.charArrayEquals;
import static org.junit.jupiter.api.Assertions.*;

/** Make sure reversible operations are actually reversible. */
public class RetrievabilityTest {


    /** Test reversibility of operations on master key (decryption, file ops) */
    @Test
    public void masterKeyAlwaysRetrievableTest() throws Exception {
        Utils.hackCryptographyExportRestrictions();
        CoreService coreService = new CoreService(new EntropyCollector());
        SecureRandom rng = new SecureRandom();
        byte[] masterKeyBytes = new byte[384/8];
        rng.nextBytes(masterKeyBytes);
        char[] originalMasterKeyChars = Utils.getUrlSafeCharsFromBytes(masterKeyBytes);
        masterKeyBytes = Utils.getBytesFromChars(originalMasterKeyChars);
        for (int passwordLength=1; passwordLength<100; passwordLength++) {
            byte[] masterPassBytes = new byte[passwordLength];
            rng.nextBytes(masterPassBytes);
            char[] masterPassChars = new String(masterPassBytes).toCharArray();
            EncryptedMessage enc = AES.encrypt(masterKeyBytes, masterPassChars);
            coreService.setMasterKeyEncrypted(enc);
            coreService.decryptMasterKey(masterPassChars);
            int end = originalMasterKeyChars.length;
            char[] returnedChars = coreService.getMasterKeyPlainText();

            /* Test that decrypting an encrypted master key returns the original. */
            assertEquals(originalMasterKeyChars.length, returnedChars.length);
            for (int i=0; i<end; i++) {
                assertEquals(originalMasterKeyChars[i], returnedChars[i]);
            }

            /* Test that cipherText is not accidentally plain text master key in a different encoding. */
            assertFalse(charArrayEquals(originalMasterKeyChars, Utils.getUrlSafeCharsFromBytes(enc.getCipherText())));
            assertFalse(charArrayEquals(originalMasterKeyChars, new String(enc.getCipherText()).toCharArray()));
            assertFalse(byteArrayEquals(masterKeyBytes, enc.getCipherText()));

            /* File ops: Test that loading a saved EncryptedMessage returns the original. */
            String tempFilePath = getTempFilePath();
            enc.saveToFile(tempFilePath);
            File tempFile = new File(tempFilePath);
            EncryptedMessage enc2 = new EncryptedMessage(tempFile);
            assertEquals(enc, enc2);
            tempFile.delete();
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
            assertTrue(byteArrayEquals(orig, back));
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

    private String getTempFilePath() {
        String targetDir = System.getProperty("user.dir") + File.separator + "target";
        String randomFileName = "tempFileCreatedByTest" + new Random().nextInt(10000000) + ".txt";
        return targetDir + File.separator + randomFileName;
    }

}
