package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;

import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Locale;
import java.util.concurrent.Callable;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcCore.class)
public class SettingsTest {

    private Settings settings;
    private Organization testOrganization;
    private CliContext context;
    private TestIo io;
    private TmcCore core;

    @Before
    public void setUp() {
        testOrganization = new Organization("test", "test", "hy", "test", false);
        settings = new Settings("testuser", "testpassword", testOrganization);
        core = spy(new TmcCore(settings, new TaskExecutorImpl()));
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(eventSendBuffer);
        io = new TestIo();
        context = new CliContext(io, core, new WorkDir(), settings, analyticsFacade);
    }

    @Test
    public void constructorInitializesFields() {
        assertEquals("testuser", settings.getUsername().get());
        assertEquals("testpassword", settings.getPassword().get());
    }

    @Test
    public void setAndGetAccount() {
        Account account = new Account();
        settings.setAccount(null, account);
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
    public void ifPasswordExistsSettingsAreMigrated() {
        doReturn(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        }).when(core).authenticate(any(ProgressObserver.class), any(String.class));
        Account account = new Account("username", "password", testOrganization);
        TmcSettingsHolder.set(settings);
        io.addLinePrompt(testOrganization.getSlug());
        io.addLinePrompt("y");
        io.addLinePrompt("y");
        settings.setAccount(this.context, account);
        assertTrue(!settings.getPassword().isPresent());
        assertTrue(settings.getServerAddress().equals("https://tmc.mooc.fi"));
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
