package fi.helsinki.cs.tmc.cli.tmcstuff;

/**
 * Reads and writes to config files on the system.
 */
public class SettingsIO {
    private String configPath;
    private String configFileName;

    public SettingsIO() {
        String os = System.getProperty("os.name").toLowerCase();
        configFileName = "tmc.conf";

        if (os.contains("windows")) {
            //TODO: Use proper Windows config file location
            this.configPath = System.getProperty("user.home") + configFileName;
        } else {
            //Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null) {
                this.configPath = configEnv + configFileName;
            } else {
                this.configPath = System.getProperty("user.home") + ".config/tmc/" + configFileName;
            }
        }
    }
}
