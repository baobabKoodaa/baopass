package crypto;

import app.BaoPass;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntropyTest {

    public static final int MASTER_KEY_LEN = 64;
    public static final int SITE_PASS_LEN = 12;


    /** Expect success from master key entropy test with a simulated EntropyCollector. */
    @Test
    public void masterKeySimulatedEntropyCollectorTestSucceeds() throws Exception {
        boolean brokenEntropyCollector = false;
        assertTrue(masterKeyLooksRandom(brokenEntropyCollector));
    }

    /** Expect failure from master key entropy test with a broken EntropyCollector
     *  despite the fact that master key is partially produced by SecureRandom. */
    @Test
    public void masterKeyBrokenEntropyCollectorTestFails() throws Exception {
        boolean brokenEntropyCollector = true;
        assertFalse(masterKeyLooksRandom(brokenEntropyCollector));
    }

    /** Entropy collector depends on user input, so we have to fake it for automated tests.
     *  This means that the actual EntropyCollector should be separately tested. */
    class EntropyCollectorStub extends EntropyCollector {

        /** Incrementing i between calls should provide different outputs to simulate
         *  a functional Entropy Collector. Same i should provide the same output
         *  every time to simulate a broken entropy collector. */
        int i = 0;

        @Override
        public byte[] consume(int outputLengthInBytes) throws UnsupportedEncodingException {
            char[] charBuffer = (""+i).toCharArray();
            return PBKDF2.generateKey(charBuffer, 1, 16).getEncoded();
        }
    }

    /** Generates 100 000 master keys and returns true if they look random.
     *  @param brokenEntropyCollector determines if we simulate a functional or broken entropy collector. */
    private boolean masterKeyLooksRandom(boolean brokenEntropyCollector) throws Exception {
        EntropyCollectorStub simulatedEntropyCollector = new EntropyCollectorStub();
        BaoPass baoPass = new BaoPass(simulatedEntropyCollector);
        HashSet<String> seenKeys = new HashSet<>();
        int[][] charCountsPos = new int[MASTER_KEY_LEN][500];
        int[] charCounts = new int[500];
        for (int i=0; i<100000; i++) {
            if (!brokenEntropyCollector) {
                /* If value is not incremented, entropyCollector produces same output every time. */
                simulatedEntropyCollector.i++;
            }
            char[] mkey = baoPass.generateMasterKey();
            boolean keyNotSeenBefore = seenKeys.add(new String(mkey));
            /* Even if we simulate a broken entropy collector, we do not expect to see
            *  collisions, because part of the master key is made with SecureRandom. */
            assertTrue(keyNotSeenBefore, "Master key collision detected!");
            assertEquals(MASTER_KEY_LEN, mkey.length);
            for (int j=0; j<MASTER_KEY_LEN; j++) {
                char c = mkey[j];
                charCountsPos[j][c]++;
                charCounts[c]++;
            }
        }
        boolean dist1 = overallSensibleCharacterDistribution(charCounts);
        boolean dist2 = perPositionSensibleCharacterDistribution(charCountsPos);
        return (dist1 && dist2);
    }

    /** Rudimentary testing for randomness of generated site passwords. */
    @Test
    public void deterministicSitePassEntropyTest() throws Exception {
        sitePassEntropyTest("This fixed String always produces the same test results.");
    }

    /** Runs forever with random input. */
    //@Test
    public void nondeterministicSitePassEntropyTest() throws Exception {
        SecureRandom rng = new SecureRandom();
        while (true) {
            byte[] b = new byte[20];
            rng.nextBytes(b);
            sitePassEntropyTest(new String(b));
        }
    }

    public void sitePassEntropyTest(String prefix) throws Exception {
        BaoPass baoPass = new BaoPass(new EntropyCollector());
        HashSet<String> outputs = new HashSet<>();
        int[][] charCountsPos = new int[SITE_PASS_LEN][500];
        int[] charCounts = new int[500];
        for (int i=0; i<100; i++) {
            char[] mkey = (prefix + i).toCharArray();
            baoPass.setMasterKeyPlainText(mkey);
            for (int j=0; j<100; j++) {
                char[] keyword = ("Similar keywords " + j).toCharArray();
                char[] sitePass = baoPass.generateSitePass(keyword);
                boolean passNotSeenBefore = outputs.add(new String(sitePass));
                assertEquals(SITE_PASS_LEN, sitePass.length);
                assertTrue(passNotSeenBefore, "Site pass collision detected!");
                for (int k=0; k<SITE_PASS_LEN; k++) {
                    char c = sitePass[k];
                    charCountsPos[k][c]++;
                    charCounts[c]++;
                }
            }
        }
        assertTrue(overallSensibleCharacterDistribution(charCounts));
        assertTrue(perPositionSensibleCharacterDistribution(charCountsPos));
    }

    /** Returns true if PER POSITION character distribution appears somewhat sensible. */
    private boolean perPositionSensibleCharacterDistribution(int[][] charCountsPos) {
        HashSet<Character> seenChars = new HashSet<>();
        for (int k=0; k<SITE_PASS_LEN; k++) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int c=0; c<500; c++) {
                if (charCountsPos[k][c] == 0) continue;
                seenChars.add((char)c);
                int count = charCountsPos[k][c];
                min = Math.min(min, count);
                max = Math.max(max, count);
            }
            if (seenChars.size() != 64) {
                System.err.println("Expected 64 distinct chars in site pass, actual: " + seenChars.size());
                return false;
            }
            if (Math.abs(max-min) > 0.5*max) {
                System.err.println("Badly skewed character distribution for position " + k);
                return false;
            }
        }
        return true;
    }

    /** Returns true if OVERALL character distribution appears somewhat sensible. */
    private boolean overallSensibleCharacterDistribution(int[] charCounts) {
        int globalMin = Integer.MAX_VALUE;
        int globalMax = Integer.MIN_VALUE;
        for (int c=0; c<500; c++) {
            if (charCounts[c] == 0) continue;
            globalMax = Math.max(globalMax, charCounts[c]);
            globalMin = Math.min(globalMin, charCounts[c]);
        }
        return (Math.abs(globalMax-globalMin) < 0.2*globalMax);
    }

    void printDist(int[][] charCounts, int k) {
        System.out.println("Character distribution for position " + k);
        for (int c=0; c<500; c++) {
            if (charCounts[k][c] == 0) continue;
            System.out.println("    " + ((char) c) + " count " + charCounts[k][c]);
        }
    }
}
