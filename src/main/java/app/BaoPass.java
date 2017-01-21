package app;

import crypto.*;
import ui.GUI;
import ui.Views.FirstLaunchView;
import ui.Views.MainView;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static crypto.Utils.*;

public class BaoPass {

    /* Few iterations for site pass is ok, because master key has high entropy. */
    private static final int SITE_PASS_ITERATIONS = 276;

    private static final String CRYPTO_EXPORT_RESTRICTIONS_ERROR = "Our efforts to use 256-bit keys were thwarped by Java's export restrictions on cryptography. You need to install the Cryptographic Policy Extensions from Oracle.";

    /* This variable determines generated site password length.
    *  Must be multiple of 3 for Base64 encoding. 9 bytes yields exactly 12 chars in Base64.
    *  If a non divisible byte amount is needed, this variable should be first rounded up
    *  to a multiple of 3 and the result should be truncated down AFTER Base64 encoding.
    *  Otherwise the last characters in the generated pass will have lower entropy. */
    private static final int SITE_PASS_BYTES = 9;

    private EntropyCollector entropyCollector;
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
        String initialView = (loadEncryptedMasterKey(keyFile) ? MainView.id : FirstLaunchView.id);
        GUI gui = new GUI(this, entropyCollector, initialView);
    }

    public boolean encryptMasterKey(char[] passwordForEncryption) {
        try {
            masterKeyEncrypted = AES.encrypt(new String(masterKeyPlainText).getBytes(), passwordForEncryption);
            forgetMasterKeyPlainText();
            /* TODO: save to file.
            * TODO: if preferenceRememberKey then remember file. */
            return true;
        } catch (InvalidKeyException ex) {
            System.err.println(CRYPTO_EXPORT_RESTRICTIONS_ERROR);
        } catch (Exception ex) {
            System.err.println("Internal failure! " + ex.toString());
        }
        return false;
    }

    public char[] generateMasterKey() {
        try {
            byte[] keyPartFromMainEntropySource = requestRandomBytesFromOS(32);
            byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
            byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy, true);
            masterKeyPlainText = getUrlSafeCharsFromBytes(masterKey);
            return masterKeyPlainText;
        } catch (UnsupportedEncodingException|NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
            return null;
        }
    }

    public char[] generateSitePass(char[] keyword) throws Exception {
        char[] combined = combine(masterKeyPlainText, keyword, false);
        SecretKey siteKey = PBKDF2.generateKey(combined, SITE_PASS_ITERATIONS, SITE_PASS_BYTES);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    public boolean loadEncryptedMasterKey(File file) {
        try {
            masterKeyEncrypted = new EncryptedMessage(file);
            if (preferenceRememberKey) {
                /* TODO: remember selected file */
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean decryptMasterKey(char[] masterPassword) {
        try {
            masterKeyPlainText = new String(AES.decrypt(masterKeyEncrypted, masterPassword)).toCharArray();
            return true;
        } catch (InvalidKeyException ex) {
            System.err.println(CRYPTO_EXPORT_RESTRICTIONS_ERROR);
        } catch (AEADBadTagException ex) {
            /* Most likely invalid password. Don't report error, we try to decrypt on every keystroke. */
        } catch (Exception ex) {
            /* Other unknown errors. */
            System.err.println(ex.toString()); //TODO
        }
        return false;
    }

    public void forgetMasterKeyPlainText() {
        wipe(masterKeyPlainText);
        masterKeyPlainText = null;
    }

    public void setMasterKeyPlainText(char[] k) {
        this.masterKeyPlainText = k;
    }

    public char[] getMasterKeyPlainText() {
        return this.masterKeyPlainText;
    }

    public void flipPreferenceRememberKey() {
        setPreferenceRememberKey(!getPreferenceRememberKey());
    }

    public boolean getPreferenceRememberKey() {
        return preferenceRememberKey;
    }

    public void setPreferenceRememberKey(boolean b) {
        this.preferenceRememberKey = b;
    }

    public EncryptedMessage getMasterKeyEncrypted() {
        return masterKeyEncrypted;
    }

    public void setMasterKeyEncrypted(EncryptedMessage masterKeyEncrypted) {
        this.masterKeyEncrypted = masterKeyEncrypted;
    }
}
