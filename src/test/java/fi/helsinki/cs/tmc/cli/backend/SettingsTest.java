package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;

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
    public void setAndGetAccount() {
        Account account = new Account();
        settings.setAccount(account);
        assertEquals(account, settings.getAccount());
    }

    @Test
    public void correctClientVersion() {
        assertEquals(EnvironmentUtil.getVersion(), settings.clientVersion());
    }

    @Test
    public void correctClientName() {
        assertEquals("tmc_cli", settings.clientName());
    }

    @Test
    public void userDataDoesntExistsIfUsernameAndPasswordAtStart() {
        settings = new Settings();
        assertFalse(settings.userDataExists());
    }

    @Test
    public void userDataExistsIfUsernameAndPasswordAreSet() {
        assertTrue(settings.userDataExists());
    }

    @Test
    public void userDataDoesNotExistIfUsernameIsNotSet() {
        Account account = new Account("testserver", null, "testpassword");
        settings.setAccount(account);
        assertTrue(!settings.userDataExists());
    }

    @Test
    public void formattedUserDataIsCorrectIfSet() {
        assertEquals("testuser:testpassword", settings.getFormattedUserData());
    }

    @Test
    public void formattedUserDataIsCorrectIfNotSet() {
        Account account = new Account("testserver", null, "testpassword");
        settings.setAccount(account);
        assertEquals("", settings.getFormattedUserData());
    }

    @Test
    public void userDataDoesNotExistIfPasswordIsNotSet() {
        Account account = new Account("testserver", "testuser", null);
        settings.setAccount(account);
        assertTrue(!settings.userDataExists());
    }

    @Test
    public void localeIsSetToEn() {
        assertTrue(new Locale("EN").equals(settings.getLocale()));
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
