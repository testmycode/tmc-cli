package fi.helsinki.cs.tmc.cli.tmcstuff;

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        settingsio.setOverrideRoot("/tmp");
    }

    @After
    public void cleanUp() {
        try {
            Files.delete(Paths.get("/tmp/tmc-cli/tmc.json"));
        } catch (Exception e) { }
        try {
            Files.delete(Paths.get("/tmp/tmc-cli"));
        } catch (Exception e) { }
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getDefaultConfigRoot();
        //System.out.println(path.toString());
        String fs = System.getProperty("file.separator");
        assertTrue(path.toString().contains("tmc-cli"));
        assertTrue(path.toString().contains(fs));
        assertTrue(!path.toString().contains(fs + fs));
    }

    @Test
    public void savingToFileWorks() {
        //TODO: make tests work properly on Windows
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            //String fs = System.getProperty("file.separator");
            settingsio.save(settings);
            Path path = Paths.get("/tmp/tmc-cli/tmc.json");
            //settingsio.getDefaultConfigRoot().resolve("tmc-cli" + fs + "tmc.json");
            assertTrue(Files.exists(path));
        } else {
            assertTrue(TRUE);
        }
    }

    @Test
    public void loadingFromFileWorks() {
        //TODO: make tests work properly on Windows
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            settingsio.save(settings);
            //String fs = System.getProperty("file.separator");
            Path path = Paths.get("/tmp");
            TmcSettings loadedSettings = null;
            loadedSettings = settingsio.load(path);
            assertEquals(settings.getUsername(), loadedSettings.getUsername());
            assertEquals(settings.getPassword(), loadedSettings.getPassword());
            assertEquals(settings.getServerAddress(), loadedSettings.getServerAddress());
            //settingsio.getDefaultConfigRoot().resolve("tmc-cli" + fs + "tmc.json");
        } else {
            assertTrue(TRUE);
        }
    }

    @Test
    public void loadingWhenNoFilePresentReturnsNull() {
        //TODO: make tests work properly on Windows
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            Path path = Paths.get("/tmp");
            TmcSettings loadedSettings = new Settings();
            loadedSettings = settingsio.load(path);
            assertEquals(loadedSettings, null);
        } else {
            assertTrue(TRUE);
        }
    }
}
