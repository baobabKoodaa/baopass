package app;

import crypto.*;
import ui.GUI;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static crypto.Utils.*;

/** Core contains state and convenience methods for key/pass generation, encryption, etc. */
public class BaoPassCore {

    /* Few iterations for site pass is ok, because master key has high entropy. */
    private static final int SITE_PASS_ITERATIONS = 276;

    private static final String CRYPTO_EXPORT_RESTRICTIONS_ERROR = "Our efforts to use 256-bit keys were thwarped by Java's export restrictions on cryptography. You need to install the Cryptographic Policy Extensions from Oracle.";

    /* This variable determines generated site password length.
    *  Must be multiple of 3 for Base64 encoding. 9 bytes yields exactly 12 chars in Base64.
    *  If a non divisible byte amount is needed, this variable should be first rounded up
    *  to a multiple of 3 and the result should be truncated down AFTER Base64 encoding.
    *  Otherwise the last characters in the generated pass will have lower entropy. */
    private static final int SITE_PASS_BYTES = 9;

    /* Dependencies. */
    private EntropyCollector entropyCollector;
    private GUI gui;

    /* Properties. */
    private EncryptedMessage masterKeyEncrypted;
    private char[] masterKeyPlainText;
    private boolean preferenceRememberKey;
    private boolean preferenceHideSitePass;

    public BaoPassCore(EntropyCollector entropyCollector) throws Exception {
        this.entropyCollector = entropyCollector;
        Utils.hackCryptographyExportRestrictions();
        loadPreferencesFromConfigFile();
    }

    private void loadPreferencesFromConfigFile() throws FileNotFoundException {
        File configFile = getOrCreateConfigFile();
        Map<String, String> config = Utils.getMapFromFile(configFile);
        preferenceRememberKey = Boolean.parseBoolean(config.get("preferenceRememberKey"));
        preferenceHideSitePass = Boolean.parseBoolean(config.get("preferenceHideSitePass"));
        loadEncryptedMasterKey(config.get("activeKey"));
    }

    private File getOrCreateConfigFile() {
        File configDir = new File(System.getProperty("user.dir") + File.separator + "baoData");
        if (configDir.exists() && !configDir.isDirectory()) {
            throw new RuntimeException("Unable to create config dir, because a file has reserved name baoData");
        }
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir.getAbsolutePath() + File.separator + "baoConfig.txt");
        if (configFile.exists() && configFile.isDirectory()) {
            throw new RuntimeException("Unable to create config file, because a directory has reserved name baoConfig.txt");
        }
        if (!configFile.exists()) {
            List<String> out = new ArrayList<>();
            out.add("activeKey:");
            out.add("preferenceRememberKey:true");
            out.add("preferenceHideSitePass:false");
            try {
                Files.write(Paths.get(configFile.getAbsolutePath()), out, Charset.forName("UTF-8"));
            } catch (Exception ex) {
                throw new RuntimeException("Unable to save config file to disk!");
            }
        }
        return configFile;
    }

    public boolean encryptMasterKey(char[] passwordForEncryption) {
        try {
            masterKeyEncrypted = AES.encrypt(new String(masterKeyPlainText).getBytes(), passwordForEncryption);
            forgetMasterKeyPlainText();
            /* TODO: save to file.
            * TODO: if preferenceRememberKey then remember file. */
            return true;
        } catch (InvalidKeyException ex) {
            gui.popupError(CRYPTO_EXPORT_RESTRICTIONS_ERROR);
        } catch (Exception ex) {
            gui.popupError("Internal failure! " + ex.toString());
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
            gui.popupError("Internal failure! " + ex.toString());
            return null;
        }
    }

    public char[] generateSitePass(char[] keyword) throws Exception {
        char[] combined = combine(masterKeyPlainText, keyword, false);
        SecretKey siteKey = PBKDF2.generateKey(combined, SITE_PASS_ITERATIONS, SITE_PASS_BYTES);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    public boolean hasActiveKeyfile() {
        return masterKeyEncrypted != null;
    }

    public boolean loadEncryptedMasterKey(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        File configDir = new File(System.getProperty("user.dir") + File.separator + "baoData");
        File file = new File(configDir.getAbsolutePath() + File.separator + fileName);
        return loadEncryptedMasterKey(file);
    }

    public boolean loadEncryptedMasterKey(File file) {
        try {
            masterKeyEncrypted = new EncryptedMessage(file);
            if (preferenceRememberKey) {
                // TODO update active key file to mem
                // update active key to config file
            }
            return true;
        } catch (Exception ex) {
            /* TODO */
            masterKeyEncrypted = null;
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

    public boolean getPreferenceHideSitePass() {
        return preferenceHideSitePass;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }
}
