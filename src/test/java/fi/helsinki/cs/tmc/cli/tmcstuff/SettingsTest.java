package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class SettingsTest {

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings("testserver", "testuser", "testpassword");
    }

    @Test
    public void constructorInitializesFields() {
        assertEquals("testserver", settings.getServerAddress());
        assertEquals("testuser", settings.getUsername());
        assertEquals("testpassword", settings.getPassword());
    }

    @Test
    public void correctApiVersion() {
        assertEquals("7", settings.apiVersion());
    }

    @Test
    public void correctClientVersion() {
        assertEquals("0.1.0", settings.clientVersion());
    }

    @Test
    public void correctClientName() {
        assertEquals("", settings.clientName());
    }

    @Test
    public void userDataExistsIfUsernameAndPasswordAreSet() {
        assertTrue(settings.userDataExists());
    }

    @Test
    public void userDataDoesNotExistIfUsernameIsNotSet() {
        settings = new Settings("testserver", null, "testpassword");
        assertTrue(!settings.userDataExists());

        settings = new Settings("testserver", "", "testpassword");
        assertTrue(!settings.userDataExists());
    }

    @Test
    public void formattedUserDataIsCorrectIfSet() {
        assertEquals("testuser:testpassword", settings.getFormattedUserData());
    }
    
    @Test
    public void formattedUserDataIsCorrectIfNotSet() {
        settings = new Settings("testserver", null, "testpassword");
        assertEquals("", settings.getFormattedUserData());
    }

    @Test
    public void userDataDoesNotExistIfPasswordIsNotSet() {
        settings = new Settings("testserver", "testuser", null);
        assertTrue(!settings.userDataExists());

        settings = new Settings("testserver", "testuser", "");
        assertTrue(!settings.userDataExists());
    }

    @Test
    public void localeIsSetToEn() {
        assertTrue(new Locale("EN").equals(settings.getLocale()));
    }

    @Test
    public void tmcProjectDirectoryIsSetCorrectly() {
        assertTrue("/tmp/tmc-cli".equals(
                settings.getTmcProjectDirectory().toString()));
    }
    
    @Test
    public void configRootIsSetCorrectly() {
        assertTrue("/tmp/tmc-cli".equals(
                settings.getConfigRoot().toString()));
    }

    @Test
    public void noCourseIsSetByDefault() {
        assertTrue(!settings.getCurrentCourse().isPresent());
    }
    
    @Test
    public void noProxyByDefault() {
        assertEquals(null, settings.proxy());
    }
}
