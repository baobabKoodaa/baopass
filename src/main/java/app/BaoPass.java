package app;

import crypto.AES;
import crypto.EncryptedMessage;
import crypto.EntropyCollector;
import crypto.PBKDF2;
import ui.GUI;

import javax.crypto.SecretKey;

import static crypto.Utils.*;

public class BaoPass {

    EntropyCollector entropyCollector;

    private EncryptedMessage masterKeyEncrypted;
    private char[] masterKeyPlainText;

    public BaoPass() {
        this.entropyCollector = new EntropyCollector();
    }

    public void run() throws Exception {
        String initialView = (loadEncryptedMasterKey() ? GUI.MAIN_VIEW_ID : GUI.FIRST_LAUNCH_VIEW_ID);
        GUI gui = new GUI(this, entropyCollector, initialView);
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

    private boolean loadEncryptedMasterKey() {
        try {
            masterKeyEncrypted = new EncryptedMessage("test2.txt");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean decryptMasterKey() {
        try {
            masterKeyPlainText = new String(AES.decrypt(masterKeyEncrypted, "passu".toCharArray())).toCharArray();
            return true;
        } catch (Exception ex) {
            System.err.println("Error decrypting master key! " + ex.toString());
            return false;
        }
    }

    public void forgetMasterKeyPlainText() {
        wipe(masterKeyPlainText);
    }

    public void setMasterKeyPlainText(char[] k) {
        this.masterKeyPlainText = k;
    }

    public char[] getMasterKeyPlainText() {
        return this.masterKeyPlainText;
    }

}
