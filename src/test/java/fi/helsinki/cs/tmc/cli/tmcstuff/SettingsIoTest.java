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
        String tempDir = System.getProperty("java.io.tmpdir");
        settingsio.setOverrideRoot(tempDir);
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
        String fs = System.getProperty("file.separator");
        assertTrue(path.toString().contains("tmc-cli"));
        assertTrue(path.toString().contains(fs));
        assertTrue(!path.toString().contains(fs + fs));
        assertTrue(path.toString().contains(System.getProperty("user.home")));
    }

    @Test
    public void savingToFileWorks() {
        String tempDir = System.getProperty("java.io.tmpdir");
        settingsio.save(settings);
        Path path = Paths.get(tempDir).resolve("tmc-cli").resolve("tmc.json");
        assertTrue(Files.exists(path));
    }

    @Test
    public void loadingFromFileWorks() {
        String tempDir = System.getProperty("java.io.tmpdir");
        settingsio.setOverrideRoot(tempDir);
        settingsio.save(settings);
        Path path = Paths.get(tempDir);
        TmcSettings loadedSettings = null;
        try {
            loadedSettings = settingsio.load(path);
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        assertEquals(settings.getUsername(), loadedSettings.getUsername());
        assertEquals(settings.getPassword(), loadedSettings.getPassword());
        assertEquals(settings.getServerAddress(), loadedSettings.getServerAddress());
    }

    @Test
    public void loadingWhenNoFilePresentReturnsNull() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir);
        TmcSettings loadedSettings = new Settings();
        try {
            loadedSettings = settingsio.load(path);
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        assertEquals(null, loadedSettings);
    }
}
