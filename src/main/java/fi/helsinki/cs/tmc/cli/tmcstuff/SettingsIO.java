package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
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
                        + fileSeparator + "tmccli"
                        + fileSeparator + configFileName;
            } else {
                configPath = System.getProperty("user.home")
                        + fileSeparator + ".config"
                        + fileSeparator + "tmccli"
                        + fileSeparator + configFileName;
            }
        }
        return configPath;
    }

    private Path getConfigFile() {

    }

    public void save(TmcSettings settings) throws IOException {
        Path location = null;
        //the above line is temporary!!!
        Gson gson = new Gson();
        byte[] json = gson.toJson(settings).getBytes();
        Files.write(location, json);
    }

    public TmcSettings load() throws IOException {
        Path location = null;
        //the above line is temporary!!!
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(location, Charset.forName("UTF-8"));
        return gson.fromJson(reader, Settings.class);
    }
}
