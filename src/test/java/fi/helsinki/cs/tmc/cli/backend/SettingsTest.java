package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;

import fi.helsinki.cs.tmc.core.domain.Organization;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class SettingsTest {

    private Settings settings;
    private Organization testOrganization;

    @Before
    public void setUp() {
        testOrganization = new Organization("test", "test", "hy", "test", false);
        settings = new Settings("testuser", "testpassword", testOrganization);
    }

    @Test
    public void constructorInitializesFields() {
        assertEquals("testuser", settings.getUsername().get());
        assertEquals("testpassword", settings.getPassword().get());
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
        Account account = new Account(null, "testpassword", testOrganization);
        settings.setAccount(account);
        assertTrue(!settings.userDataExists());
    }

    @Test
    public void userDataDoesNotExistIfPasswordIsNotSet() {
        Account account = new Account("testuser", null, testOrganization);
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
