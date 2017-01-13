package app;

import crypto.EntropyCollector;
import crypto.PBKDF2;
import ui.GUI;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

import static crypto.Utils.*;

public class BaoPass {

    EntropyCollector entropyCollector;

    public BaoPass() {
        this.entropyCollector = new EntropyCollector();
    }

    public void run() throws NoSuchAlgorithmException {
        GUI gui = new GUI(this, entropyCollector);
    }

    public char[] generateMasterKey() throws NoSuchAlgorithmException {
        byte[] keyPartFromMainEntropySource = requestRandomBytesFromOS(32);
        byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
        byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy, true);
        return getUrlSafeCharsFromBytes(masterKey);
    }

    public char[] generateSitePass(char[] mkey, char[] keyword) {
        char[] combined = combine(mkey, keyword, false);
        SecretKey siteKey = PBKDF2.generateKey(combined, 276, 32);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }


}
