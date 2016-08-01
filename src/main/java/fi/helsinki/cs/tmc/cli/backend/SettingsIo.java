package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Reads and writes to config files on the system.
 */
public class SettingsIo {

    private static final Logger logger = LoggerFactory.getLogger(SettingsIo.class);

    // CONFIG_DIR is the sub-directory located within the system specific
    // configuration folder, ex. /home/user/.config/CONFIG_DIR/
    public static final String CONFIG_DIR = "tmc-cli";

    // ACCOUNTS_CONFIG is the _global_ configuration file containing all
    // user login information including usernames, passwords (in plain text)
    // and servers. Is located under CONFIG_DIR
    public static final String ACCOUNTS_CONFIG = "accounts.json";

    // PROPERTIES_CONFIG is the _global_ configuration file containing
    // tmc-cli's configuration and data, such as when to update when the client
    //  was last updated. Is located under CONFIG_DIR
    public static final String PROPERTIES_CONFIG = "properties.json";

    public static AccountList loadAccountList() {
        return loadAccountList(getConfigDirectory());
    }

    public static AccountList loadAccountList(Path configRoot) {
        Path file = getAccountsFile(configRoot);
        if (!Files.exists(file)) {
            return new AccountList();
        }
        return getHolderFromJson(file);
    }

    public static boolean saveAccountList(AccountList list) {
        return saveAccountList(list, getConfigDirectory());
    }

    public static boolean saveAccountList(AccountList list, Path configRoot) {
        Path file = getAccountsFile(configRoot);
        return saveHolderToJson(list, file);
    }

    public static boolean delete() {
        Path file = getAccountsFile(getConfigDirectory());
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.error("Could not delete config file in " + file.toString(), e);
            return false;
        }
        return true;
    }

    public static HashMap<String, String> loadProperties() {
        return loadPropertiesFrom(getConfigDirectory());
    }

    public static HashMap<String, String> loadPropertiesFrom(Path path) {
        Path file = getPropertiesFile(path);
        HashMap<String, String> properties = getPropertiesFromJson(file);
        if (properties != null) {
            return properties;
        } else {
            return new HashMap<>();
        }
    }

    public static boolean saveProperties(HashMap<String, String> properties) {
        return savePropertiesTo(properties, getConfigDirectory());
    }

    public static boolean savePropertiesTo(HashMap<String, String> properties, Path path) {
        Path file = getPropertiesFile(path);
        return savePropertiesToJson(properties, file);
    }

    /**
     * Get the correct directory in which our config files go
     * ie /home/user/.config/tmc-cli/.
     */
    protected static Path getConfigDirectory() {
        Path configPath;
        if (EnvironmentUtil.isWindows()) {
            String appdata = System.getenv("APPDATA");
            if (appdata == null) {
                configPath = Paths.get(System.getProperty("user.home"));
            } else {
                configPath = Paths.get(appdata);
            }
        } else {
            //Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null && configEnv.length() > 0) {
                configPath = Paths.get(configEnv);
            } else {
                configPath = Paths.get(System.getProperty("user.home"))
                        .resolve(".config");
            }
        }
        return configPath.resolve(CONFIG_DIR);
    }

    private static Path getAccountsFile(Path configRoot) {
        Path file = configRoot.resolve(ACCOUNTS_CONFIG);
        if (!requireConfigDirectory(configRoot)) {
            return null;
        }
        return file;
    }

    private static Path getPropertiesFile(Path configRoot) {
        Path file = configRoot.resolve(PROPERTIES_CONFIG);
        if (!requireConfigDirectory(configRoot)) {
            return null;
        }
        return file;
    }

    /**
     * Create the config directory if it doesn't already exist.
     * @param configRoot config directory
     * @return false if directory can't be created.
     */
    private static boolean requireConfigDirectory(Path configRoot) {
        if (!Files.exists(configRoot)) {
            try {
                Files.createDirectories(configRoot);
            } catch (Exception e) {
                //TODO print error to user
                logger.error("Could not create config directory", e);
                return false;
            }
        }
        return true;
    }

    private static AccountList getHolderFromJson(Path file) {
        Gson gson = new Gson();
        Reader reader;
        try {
            reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            //TODO print error to user
            logger.error("Accounts file located, but failed to read from it", e);
            return null;
        }
        return gson.fromJson(reader, AccountList.class);
    }

    private static boolean saveHolderToJson(AccountList holder, Path file) {
        Gson gson = new Gson();
        byte[] json = gson.toJson(holder).getBytes();
        try {
            Files.write(file, json);
        } catch (IOException e) {
            //TODO print error to user
            logger.error("Could not write account to accounts file", e);
            return false;
        }
        return true;
    }

    private static HashMap<String, String> getPropertiesFromJson(Path file) {
        Gson gson = new Gson();
        Reader reader;
        try {
            reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            //TODO print error to user
            logger.error("Properties file located, but failed to read from it", e);
            return null;
        }
        @SuppressWarnings("unchecked")
        HashMap<String, String> map = gson.fromJson(reader, HashMap.class);
        return map;
    }

    private static boolean savePropertiesToJson(HashMap<String, String> properties, Path file) {
        Gson gson = new Gson();
        byte[] json = gson.toJson(properties).getBytes();
        try {
            Files.write(file, json);
        } catch (IOException e) {
            //TODO print error to user
            logger.error("Could not write properties to file", e);
            return false;
        }
        return true;
    }
}
