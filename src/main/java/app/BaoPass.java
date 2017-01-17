package app;

import crypto.*;
import ui.GUI;

import javax.crypto.SecretKey;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static crypto.Utils.*;

public class BaoPass {

    EntropyCollector entropyCollector;

    private EncryptedMessage masterKeyEncrypted;
    private char[] masterKeyPlainText;

    private boolean preferenceRememberKey;

    public BaoPass(EntropyCollector entropyCollector) {
        this.entropyCollector = entropyCollector;
    }

    public void run() throws Exception {
        Utils.hackCryptographyExportRestrictions();
        preferenceRememberKey = true; //TODO: load from config file
        File keyFile = new File("defaultKeyFile.txt");
        String initialView = (loadEncryptedMasterKey(keyFile) ? GUI.MAIN_VIEW_ID : GUI.FIRST_LAUNCH_VIEW_ID);
        GUI gui = new GUI(this, entropyCollector, initialView);
    }

    public boolean createNewMasterKey(char[] passwordForEncryption) {
        try {
            masterKeyPlainText = generateMasterKey();
            masterKeyEncrypted = AES.encrypt(new String(masterKeyPlainText).getBytes(), passwordForEncryption);
            /* TODO: save to file.
            * TODO: if preferenceRememberKey then remember file. */
            return true;
        } catch (InvalidKeyException ex) {
            System.err.println("Houston, we have a problem... Our efforts to use 256-bit keys were thwarped by Java's export restrictions on cryptography. You need to install the Cryptographic Policy Extensions from Oracle.");
        } catch (Exception ex) {
            System.err.println("Internal failure! " + ex.toString());
        }
        return false;
    }

    public char[] generateMasterKey() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] keyPartFromMainEntropySource = requestRandomBytesFromOS(32);
        byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
        byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy, true);
        return getUrlSafeCharsFromBytes(masterKey);
    }

    public char[] generateSitePass(char[] keyword) throws Exception {
        char[] combined = combine(masterKeyPlainText, keyword, false);
        int iterations = 276; /* Few iterations is enough, because master key has high entropy. */
        int passCharsLength = 12; /* Characters in Base64URLSafe. */
        int keyLengthBytes = (int) (passCharsLength * 0.8);
        SecretKey siteKey = PBKDF2.generateKey(combined, iterations, keyLengthBytes);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    public boolean loadEncryptedMasterKey(File file) {
        try {
            masterKeyEncrypted = new EncryptedMessage(file);
            if (preferenceRememberKey) {
                /* TODO: remember selected file */
            }
            decryptMasterKey(); // TODO: dont do this here.
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

    public boolean getPreferenceRememberKey() {
        return preferenceRememberKey;
    }

    public void setPreferenceRememberKey(boolean b) {
        this.preferenceRememberKey = b;
    }

}
