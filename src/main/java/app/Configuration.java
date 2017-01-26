package app;

import util.MapKeys;
import util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Utils.getCurrentDate;
import static util.Utils.getCurrentDateTime;

public class Configuration {

    /* This variable determines generated site password length.
    *  Must be multiple of 3 for Base64 encoding. 9 bytes yields exactly 12 chars in Base64.
    *  If a non divisible byte amount is needed, this variable should be first rounded up
    *  to a multiple of 3 and the result should be truncated down AFTER Base64 encoding.
    *  Otherwise the last characters in the generated pass will have lower entropy. */
    public static final int SITE_PASS_BYTES = 9;

    /** Few iterations for site pass is ok, because master key has high entropy. */
    public static final int SITE_PASS_ITERATIONS = 3;

    /** Iterations for generateKey when generating encryption key from master password.
     *  Needs to be high, because user chosen passwords may be of low quality. */
    public static final int ENCRYPTION_ITERATIONS = 2000;

    /** Defines cipher instance. Message-padding not needed in GCM mode. */
    public static final String ENCRYPTION_CIPHER = "AES/GCM/NoPadding";

    /** GCM mode requires key size to be either 128, 196 or 256 bits.
     *  TODO: Fix cryptographic export restrictions and use 256bit key. */
    public static final int ENCRYPTION_KEY_LENGTH_BYTES = 16;

    /** In our use case, GCM Security tag size is related to the probability
     *  with which we detect incorrect passwords. 128 is the max size. */
    public static final int ENCRYPTION_GCM_TAG_SIZE = 128;

    /* Some configuration properties are saved in a configuration file,
     * which is redundantly kept in a Map to reduce disk access. */
    private File configFile;
    private Map<String, String> configMap;

    /** Using this instead of default constructor, because "new Configuration()" is misleading. */
    public Configuration loadOrCreateConfig() throws FileNotFoundException {
        File configDir = new File(getDirPath());
        if (configDir.exists() && !configDir.isDirectory()) {
            throw new RuntimeException("Unable to create config dir, because a file has reserved name baoData");
        }
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir.getAbsolutePath() + File.separator + "baoConfig.txt");
        if (configFile.exists() && configFile.isDirectory()) {
            throw new RuntimeException("Unable to create config file, because a directory has reserved name baoConfig.txt");
        }
        if (configFile.exists()) {
            configMap = Utils.getMapFromFile(configFile);
        } else {
            configMap = new HashMap<>();
            configMap.put(MapKeys.ACTIVE_KEY, "");
            configMap.put(MapKeys.PREFERENCE_REMEMBER_KEY, "true");
            configMap.put(MapKeys.PREFERENCE_HIDE_SITE_PASS, "false");
            saveToFile();
        }
        return this;
    }

    public String getActiveKeyName() {
        return configMap.get(MapKeys.ACTIVE_KEY);
    }

    public File getActiveKeyFile() {
        String fileName = getActiveKeyName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        File configDir = new File(getDirPath());
        File file = new File(configDir.getAbsolutePath() + File.separator + fileName);
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    public void setActiveKeyName(String fileName) {
        configMap.put(MapKeys.ACTIVE_KEY, fileName);
    }

    public String getDirPath() {
        return System.getProperty("user.dir") + File.separator + "baoData";
    }

    /** Saves data from config map to baoData/baoConfig.txt */
    public void saveToFile() {
        new Thread(saveToFile).run();
    }

    Runnable saveToFile = new Runnable() {
        @Override
        public void run() {
            if (configFile == null || !configFile.getParentFile().exists()) {
                throw new RuntimeException();
            }
            List<String> out = new ArrayList<>();
            out.add("Configuration file for BaoPass");
            for (String key : configMap.keySet()) {
                String val = configMap.get(key);
                out.add(key + ":" + val);
            }
            try {
                Files.write(Paths.get(configFile.getAbsolutePath()), out, Charset.forName("UTF-8"));
            } catch (Exception ex) {
                // TODO: error should be visible to user
                throw new RuntimeException("Unable to save config file to disk!");
            }
        }
    };

    public String getNextAvailableNameForKeyFile() {
        File configDir = new File(getDirPath());
        /* First try with just date in the filename. */
        String fileName = "key-" + getCurrentDate() + ".key";
        for (int i=2 ;; i++) {
            File file = new File(configDir + File.separator + fileName);
            if (!file.exists()) return fileName;
            /* Increment counter in name. */
            fileName = "key-" + getCurrentDate() + "__" + i + ".key";
        }
    }

    public String getNextAvailableNameForOldKeyFile() {
        File configDir = new File(getDirPath());
        String fileName = getActiveKeyName() + ".old";
        for (int i=2 ;; i++) {
            File file = new File(configDir + File.separator + fileName);
            if (!file.exists()) return fileName;
            /* Increment counter in name. */
            fileName = getActiveKeyName() + ".old" + i;
        }
    }

    public boolean getPreferenceRememberKey() {
        return Boolean.parseBoolean(configMap.get(MapKeys.PREFERENCE_REMEMBER_KEY));
    }

    public boolean getPreferenceHideSitePass() {
        return Boolean.parseBoolean(configMap.get(MapKeys.PREFERENCE_HIDE_SITE_PASS));
    }

    public void setPreferenceRememberKey(boolean bool) {
        configMap.put(MapKeys.PREFERENCE_REMEMBER_KEY, bool+"");
    }

    public void setPreferenceHideSitePass(boolean bool) {
        configMap.put(MapKeys.PREFERENCE_HIDE_SITE_PASS, bool+"");
    }
}
