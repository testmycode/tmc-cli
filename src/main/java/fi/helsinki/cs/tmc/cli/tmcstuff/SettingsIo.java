package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import com.google.gson.Gson;

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

    private static final String CONFIGFILE = "tmc.conf";

    public static Path getDefaultConfigRoot() {
        String fileSeparator;
        String configPath;
        String os = System.getProperty("os.name").toLowerCase();
        fileSeparator = System.getProperty("file.separator");

        if (os.contains("windows")) {
            //TODO: Use proper Windows config file location
            configPath = System.getProperty("user.home") + fileSeparator;
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
        configPath = configPath + "tmc-cli" + fileSeparator;
        return Paths.get(configPath);
    }

    public void save(TmcSettings settings) throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        Path location = settings.getConfigRoot().resolve(fileSeparator + CONFIGFILE);
        Gson gson = new Gson();
        byte[] json = gson.toJson(settings).getBytes();
        Files.write(location, json);
    }

    public TmcSettings load(Path configRoot) throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        Path location = configRoot.resolve(fileSeparator + CONFIGFILE);
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(location, Charset.forName("UTF-8"));
        return gson.fromJson(reader, Settings.class);
    }

    public TmcSettings load() throws IOException {
        return load((getDefaultConfigRoot()));
    }
}
