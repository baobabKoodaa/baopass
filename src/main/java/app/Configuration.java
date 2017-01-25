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
        while (true) {
            File file = new File(configDir + File.separator + fileName);
            if (!file.exists()) return fileName;
            /* Append current time to filename as well. */
            fileName = "key-" + getCurrentDateTime();
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
