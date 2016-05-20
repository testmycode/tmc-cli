package fi.helsinki.cs.tmc.cli.tmcstuff;

import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes to config files on the system.
 */
public class SettingsIO {

    private String getConfigFilePath() {
        String fileSeparator;
        String configFileName;
        String configPath;
        String os = System.getProperty("os.name").toLowerCase();
        fileSeparator = System.getProperty("file.separator");
        configFileName = "tmc.conf";

        if (os.contains("windows")) {
            //TODO: Use proper Windows config file location
            configPath = System.getProperty("user.home") + fileSeparator + configFileName;
        } else {
            //Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null && configEnv.length() > 0) {
                configPath = configEnv
                        + fileSeparator + "tmc"
                        + fileSeparator + configFileName;
            } else {
                configPath = System.getProperty("user.home")
                        + fileSeparator + ".config"
                        + fileSeparator + "tmc"
                        + fileSeparator + configFileName;
            }
        }
        return configPath;
    }

    private Path getConfigFile() {
        Path configFile = null;
        
    }
}
