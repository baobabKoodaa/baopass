package crypto;

import app.BaoPass;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaoPassTest {

    public static final int SITE_PASS_LEN = 12;

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
        BaoPass baoPass = new BaoPass();
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
                assertTrue(passNotSeenBefore, "Collision detected!");
                for (int k=0; k<SITE_PASS_LEN; k++) {
                    char c = sitePass[k];
                    charCountsPos[k][c]++;
                    charCounts[c]++;
                }
            }
        }

        /* Test the distribution of each character position. */
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
            assertEquals(64, seenChars.size(), "Expected 64 distinct characters, actual: " + seenChars.size());
            int mostDiff = Math.abs(max-min);
            assertTrue(mostDiff < 0.5*max, "Badly skewed character distribution for position " + k);
        }

        /* Test the distribution of characters overall. */
        int globalMin = Integer.MAX_VALUE;
        int globalMax = Integer.MIN_VALUE;
        for (int c=0; c<500; c++) {
            if (charCounts[c] == 0) continue;
            globalMax = Math.max(globalMax, charCounts[c]);
            globalMin = Math.min(globalMin, charCounts[c]);
        }
        int mostDiff = Math.abs(globalMax-globalMin);
        assertTrue(mostDiff < 0.2*globalMax, "Skewed character distribution!");
    }

    void printDist(int[][] charCounts, int k) {
        System.out.println("Character distribution for position " + k);
        for (int c=0; c<500; c++) {
            if (charCounts[k][c] == 0) continue;
            System.out.println("    " + ((char) c) + " count " + charCounts[k][c]);
        }
    }
}
