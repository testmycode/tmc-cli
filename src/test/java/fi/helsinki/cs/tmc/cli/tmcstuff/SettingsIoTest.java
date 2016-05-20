package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by jclakkis on 20.5.2016.
 */
public class SettingsIoTest {
    private Settings settings;
    private SettingsIo settingsio;
    private final PrintStream stdout = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        this.settings = new Settings("testserver", "testuser", "testpassword");
        this.settingsio = new SettingsIo();
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getDefaultConfigRoot();
        System.out.println(path.toString());
        String fs = System.getProperty("file.separator");
        //assertTrue(path.toString().contains("tmc-cli"));
        assertTrue(path.toString().contains(fs));
        //assertTrue(!path.toString().contains(fs + fs));
    }

    @Test
    public void savingToFileWorks() {
        try {
            settingsio.save(settings);
        } catch(IOException e) {
            Assert.fail();
        }
        Path path = settings.getConfigRoot().resolve("tmc.conf");
        assertTrue(Files.exists(path));
    }
}
