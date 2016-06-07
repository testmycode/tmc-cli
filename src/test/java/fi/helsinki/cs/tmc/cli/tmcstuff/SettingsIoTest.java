package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



/**
 * Created by jclakkis on 20.5.2016.
 */
public class SettingsIoTest {
    private Settings settings;
    private Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve(SettingsIo.CONFIG_DIR);
        this.settings = new Settings("testserver", "testuser", "testpassword");
        try {
            FileUtils.deleteDirectory(tempDir.resolve(SettingsIo.CONFIG_DIR).toFile());
        } catch (Exception e) { }
    }

    @After
    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(tempDir.resolve(SettingsIo.CONFIG_DIR).toFile());
        } catch (Exception e) { }
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getDefaultConfigRoot();
        String fs = System.getProperty("file.separator");
        assertTrue(path.toString().contains(SettingsIo.CONFIG_DIR));
        assertTrue(path.toString().contains(fs));
        assertTrue(!path.toString().contains(fs + fs));
        assertTrue(path.toString().contains(System.getProperty("user.home")));
    }

    @Test
    public void savingToFileWorks() {
        Boolean success = SettingsIo.saveTo(settings, tempDir);
        assertTrue(success);
        Path path = tempDir
                .resolve(SettingsIo.ACCOUNTS_CONFIG);
        assertTrue(Files.exists(path));
    }

    @Test
    public void loadingFromFileWorks() {
        SettingsIo.saveTo(this.settings, tempDir);
        TmcSettings loadedSettings;
        loadedSettings = SettingsIo.loadFrom(tempDir);
        assertNotNull(loadedSettings);
        assertEquals(settings.getUsername(), loadedSettings.getUsername());
        assertEquals(settings.getPassword(), loadedSettings.getPassword());
        assertEquals(settings.getServerAddress(), loadedSettings.getServerAddress());
    }

    @Test
    public void loadingWhenNoFilePresentReturnsNull() {
        Path path = tempDir.getParent();
        TmcSettings loadedSettings = SettingsIo.loadFrom(path);
        assertEquals(null, loadedSettings);
    }

    @Test
    public void newHolderIsEmpty() {
        SettingsHolder holder = new SettingsHolder();
        assertEquals(0, holder.settingsCount());
    }

    @Test
    public void addingSettingsIncreasesHolderCount() {
        SettingsHolder holder = new SettingsHolder();
        holder.addSettings(new Settings("eee", "aaa", "ooo"));
        assertEquals(1, holder.settingsCount());
    }

    @Test
    public void loadingFromHolderWorks() {
        SettingsHolder holder = new SettingsHolder();
        holder.addSettings(this.settings);
        Settings loaded = holder.getSettings();
        assertSame(this.settings, loaded);
    }

    @Test
    public void addingMoreThanOneSettingWorks() {
        SettingsHolder holder = new SettingsHolder();
        holder.addSettings(this.settings);
        holder.addSettings(new Settings("1", "2", "e"));
        holder.addSettings(new Settings(":", "-", "D"));
        assertEquals(3, holder.settingsCount());
    }

    @Test
    public void loadingLatestSettingsWorks() {
        SettingsHolder holder = new SettingsHolder();
        holder.addSettings(new Settings(":", "-", "D"));
        holder.addSettings(new Settings("1", "2", "e"));
        holder.addSettings(this.settings);
        Settings latest = holder.getSettings();
        assertSame(this.settings, latest);
    }

    @Test
    public void gettingSettingsByNameAndServerWorks() {
        SettingsHolder holder = new SettingsHolder();
        Settings wanted = new Settings("1", "2", "e");
        holder.addSettings(new Settings(":", "-", "D"));
        holder.addSettings(wanted);
        holder.addSettings(new Settings("344", "wc", "fffssshhhh aaahhh"));
        Settings get = holder.getSettings("2", "1");
        assertSame(wanted, get);
    }

    @Test
    public void gettingLatestSettingsSetsItToTheTop() {
        SettingsHolder holder = new SettingsHolder();
        Settings wanted = new Settings("1", "2", "e");
        holder.addSettings(wanted);
        holder.addSettings(new Settings(":", "-", "D"));
        holder.getSettings("2", "1");
        Settings get = holder.getSettings();
        assertSame(wanted, get);
    }
}
