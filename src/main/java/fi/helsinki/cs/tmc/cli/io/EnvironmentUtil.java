package fi.helsinki.cs.tmc.cli.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvironmentUtil {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentUtil.class);

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("windows");
    }

    public static int getTerminalWidth() {
        String colEnv = System.getenv("COLUMNS");
        if (colEnv != null && !colEnv.equals("")) {
            // Determine the terminal width - this won't work on Windows
            // Let's just hope our Windows users won't narrow their command prompt
            // We'll also enforce a minimum size of 20 columns

            return Math.max(Integer.parseInt(colEnv), 20);
        } else {
            return 70;
        }
    }

    public static String getVersion() {
        String path = "/maven.prop";
        InputStream stream = EnvironmentUtil.class.getResourceAsStream(path);
        if (stream == null) {
            return "n/a";
        }

        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            logger.warn("Failed to get version", e);
            return "n/a";
        }
    }
}
