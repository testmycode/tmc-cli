package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads and writes to config files on the system.
 */
public class SettingsIo {

    private static final Logger logger = LoggerFactory.getLogger(SettingsIo.class);
    
    // CONFIG_DIR is the sub-directory located within the system specific
    // configuration folder, ex. /home/user/.config/CONFIG_DIR/
    private static final String CONFIG_DIR = "tmc-cli";
    
    // ACCOUNTS_CONFIG is the _global_ configuration file containing all
    // user login information including usernames, passwords (in plain text)
    // and servers. Is located under CONFIG_DIR
    private static final String ACCOUNTS_CONFIG = "accounts.json";
    
    // COURSE_CONFIG is the _local_ configuration file containing course
    // information and is located in the root of each different course.
    // Contains username, server and course name.
    private static final String COURSE_CONFIG = ".tmc.json";
    //The overrideRoot variable is intended only for testing
    private String overrideRoot;

    public static Path getDefaultConfigRoot() {
        String fileSeparator;
        String configPath;
        String os = System.getProperty("os.name").toLowerCase();
        fileSeparator = System.getProperty("file.separator");



        if (os.contains("windows")) {
            //TODO: Use proper Windows config file location
            configPath = System.getProperty("user.home")
                    + fileSeparator;
        } else {
            //Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null && configEnv.length() > 0) {
                configPath = configEnv
                        + fileSeparator;
            } else {
                configPath = System.getProperty("user.home")
                        + fileSeparator + ".config"
                        + fileSeparator;
            }
        }
        configPath = configPath + CONFIG_DIR + fileSeparator;
        return Paths.get(configPath);
    }

    private Path getAccountsFile(Path path) {
        if (this.overrideRoot != null) {
            path = Paths.get(this.overrideRoot).resolve(CONFIG_DIR);
        }
        Path file = path.resolve(ACCOUNTS_CONFIG);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) { }
            try {
                Files.createFile(path);
            } catch (Exception e) { }
        }
        return file;
    }

    public Boolean save(TmcSettings settings) {
        //Temporarily always use the default directory
        Path file = getAccountsFile(getDefaultConfigRoot());
        Gson gson = new Gson();
        byte[] json = gson.toJson(settings).getBytes();
        try {
            Files.write(file, json);
        } catch (IOException e) {
            logger.error("Could not write settings to configuration file", e);
            return false;
        }
        return true;
    }

    public TmcSettings load(Path configRoot) {
        Path file = getAccountsFile(configRoot);
        Gson gson = new Gson();
        if (!Files.exists(file)) {
            //Return null if file is not found, this is normal behaviour
            return null;
        }
        Reader reader = null;
        try {
            reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.error("Configuration file located, but failed to read from it", e);
            return null;
        }
        return gson.fromJson(reader, Settings.class);
    }

    public TmcSettings load() throws IOException {
        return load((getDefaultConfigRoot()));
    }

    public Boolean delete() {
        Path file = getAccountsFile(getDefaultConfigRoot());
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.error("Could not delete config file in " + file.toString(), e);
            return false;
        }
        return true;
    }

    /**
     * This is for testing purposes only. Otherwise use the default config path.
     */
    protected void setOverrideRoot(String override) {
        this.overrideRoot = override;
    }
}
