package app;

import crypto.AES;
import crypto.EncryptedMessage;
import crypto.EntropyCollector;
import crypto.PBKDF2;
import ui.GUI;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static crypto.Utils.*;

public class BaoPass {

    EntropyCollector entropyCollector;

    private char[] masterKey;

    public BaoPass() {
        this.entropyCollector = new EntropyCollector();
    }

    public void run() throws Exception {
        GUI gui = new GUI(this, entropyCollector);
        EncryptedMessage enc = new EncryptedMessage("test.txt");
        masterKey = new String(AES.decrypt(enc, "passu".toCharArray())).toCharArray();
    }

    public char[] generateMasterKey() throws Exception {
        byte[] keyPartFromMainEntropySource = requestRandomBytesFromOS(32);
        byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
        byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy, true);
        return getUrlSafeCharsFromBytes(masterKey);
    }

    public char[] generateSitePass(char[] mkey, char[] keyword) throws Exception {
        char[] combined = combine(mkey, keyword, false);
        int iterations = 276; /* Few iterations is enough, because master key has high entropy. */
        int passCharsLength = 12; /* Characters in Base64URLSafe. */
        int keyLengthBytes = (int) (passCharsLength * 0.8);
        SecretKey siteKey = PBKDF2.generateKey(combined, iterations, keyLengthBytes);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    public void setMasterKey(char[] k) {
        this.masterKey = k;
    }

    public char[] getMasterKey() {
        return this.masterKey;
    }

}
