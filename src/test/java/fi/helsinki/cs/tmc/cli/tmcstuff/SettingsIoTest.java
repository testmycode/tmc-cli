package fi.helsinki.cs.tmc.cli.tmcstuff;

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import org.junit.*;

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

    @AfterClass
    public static void cleanUp() {
        try {
            Files.delete(Paths.get("/tmp/tmc-cli/tmc.json"));
        } catch (Exception e) {
            return;
        }
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getDefaultConfigRoot();
        System.out.println(path.toString());
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
            try {
                settingsio.save(settings);
            } catch (IOException e) {
                Assert.fail(e.toString());
            }
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
            //String fs = System.getProperty("file.separator");
            Path path = Paths.get("/tmp/tmc-cli");
            TmcSettings loadedSettings = null;
            try {
                loadedSettings = settingsio.load(path);
            } catch (IOException e) {
                Assert.fail(e.toString());
                return;
            }
            assertEquals(settings.getUsername(), loadedSettings.getUsername());
            assertEquals(settings.getPassword(), loadedSettings.getPassword());
            assertEquals(settings.getServerAddress(), loadedSettings.getServerAddress());
            //settingsio.getDefaultConfigRoot().resolve("tmc-cli" + fs + "tmc.json");
        } else {
            assertTrue(TRUE);
        }
    }
}
