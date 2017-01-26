package app;

import crypto.*;
import util.Notifications;
import util.Utils;

import javax.crypto.SecretKey;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import static util.Utils.*;

/** Contains state and convenience methods for key/pass generation, encryption, config, etc. */
public class CoreService {

    /* Dependencies. */
    private EntropyCollector entropyCollector;

    /* Properties. */
    private Configuration config;
    private EncryptedMessage masterKeyEncrypted;
    private char[] masterKeyPlainText;

    public CoreService(EntropyCollector entropyCollector) throws Exception {
        this.entropyCollector = entropyCollector;
        config = new Configuration().loadOrCreateConfig();
        loadEncryptedMasterKey(config.getActiveKeyFile());
    }

    /** Generates master key from 2 entropy sources, saves plain text key in memory. */
    public char[] generateMasterKey() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] keyPartFromMainEntropySource = requestRandomBytesFromOS(32);
        byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
        byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy, true);
        masterKeyPlainText = getUrlSafeCharsFromBytes(masterKey);
        return masterKeyPlainText;
    }

    /** Encrypts master key with given password and forgets its plain text representation. */
    public void encryptMasterKey(char[] passwordForEncryption) throws Exception {
        if (masterKeyPlainText == null || masterKeyPlainText.length == 0) {
            throw new RuntimeException("Master key not found!");
        }
        masterKeyEncrypted = AES.encrypt(Utils.getBytesFromChars(masterKeyPlainText), passwordForEncryption);
        forgetMasterKeyPlainText();
    }

    public String saveEncryptedMasterKey() throws IOException {
        String fileName = config.getNextAvailableNameForKeyFile();
        return saveEncryptedMasterKey(fileName);
    }

    public String saveEncryptedMasterKey(String fileName) throws IOException {
        String filePath = config.getDirPath() + File.separator + fileName;
        masterKeyEncrypted.saveToFile(filePath);
        if (getPreferenceRememberKey()) {
            config.setActiveKeyName(fileName);
            config.saveToFile();
        }
        return fileName;
    }

    public char[] generateSitePass(char[] keyword) throws Exception {
        char[] combined = combine(masterKeyPlainText, keyword, false);
        SecretKey siteKey = PBKDF2.generateKey(combined, Configuration.SITE_PASS_ITERATIONS, Configuration.SITE_PASS_BYTES);
        return getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    public boolean loadEncryptedMasterKey(File file) {
        try {
            masterKeyEncrypted = new EncryptedMessage(file);
            if (getPreferenceRememberKey()) {
                config.setActiveKeyName(file.getName()); //TODO: remember also from other dirs
                config.saveToFile();
            }
            return true;
        } catch (Exception ex) {
            masterKeyEncrypted = null;
            return false;
        }
    }

    public void decryptMasterKey(char[] masterPassword) throws Exception {
        byte[] bytes = AES.decrypt(masterKeyEncrypted, masterPassword);
        masterKeyPlainText = Utils.getCharsFromBytes(bytes);
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

    public String changeMPW(char[] newMPW) throws Exception {
        String activeKeyName = getActiveKeyName();
        String oldKeyName = config.getNextAvailableNameForOldKeyFile();
        Path pathActiveKeyFile = Paths.get(getConfigDirPath() + File.separator + getActiveKeyName());
        Path whereToMoveOldKeyFile = pathActiveKeyFile.resolveSibling(oldKeyName);
        Files.move(pathActiveKeyFile, whereToMoveOldKeyFile);

        encryptMasterKey(newMPW);
        saveEncryptedMasterKey(activeKeyName);
        //TODO: verify success
        return Notifications.MPWchange(oldKeyName, activeKeyName);
    }

    public boolean getPreferenceHideSitePass() {
        return config.getPreferenceRememberKey();
    }

    public void setPreferenceHideSitePass(boolean bool) {
        config.setPreferenceHideSitePass(bool);
        config.saveToFile();
    }

    public boolean getPreferenceRememberKey() {
        return config.getPreferenceRememberKey();
    }

    public void setPreferenceRememberKey(boolean bool) {
        config.setPreferenceRememberKey(bool);
        if (!bool) {
            config.setActiveKeyName("");
        }
        config.saveToFile();
    }

    public void flipPreferenceRememberKey() {
        setPreferenceRememberKey(!getPreferenceRememberKey());
    }

    public EncryptedMessage getMasterKeyEncrypted() {
        return masterKeyEncrypted;
    }

    public void setMasterKeyEncrypted(EncryptedMessage masterKeyEncrypted) {
        this.masterKeyEncrypted = masterKeyEncrypted;
    }

    public String getConfigDirPath() {
        return config.getDirPath();
    }

    public boolean hasActiveKeyFile() {
        return masterKeyEncrypted != null;
    }

    public String getActiveKeyName() {
        return config.getActiveKeyName();
    }

}
