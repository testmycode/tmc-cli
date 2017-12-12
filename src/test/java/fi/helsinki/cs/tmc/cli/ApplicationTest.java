package fi.helsinki.cs.tmc.cli;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsSettings;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.updater.AutoUpdater;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AutoUpdater.class)
public class ApplicationTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcCore core = new TmcCore(settings, tmcLangs);
        AnalyticsSettings analyticsSettings = new AnalyticsSettings();
        EventSendBuffer eventSendBuffer = new EventSendBuffer(analyticsSettings, new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        app = new Application(new CliContext(io, core, new WorkDir(), settings, analyticsFacade));
        mockStatic(AutoUpdater.class);
    }

    @Test
    public void versionWorksWithRightParameter() {
        String[] args = {"-v", "foo"};
        app.run(args);
        io.assertContains("TMC-CLI version");
        io.assertNotContains("Command foo doesn't exist.");
    }

    @Test
    public void failWhenInvalidOption() {
        String[] args = {"-a34t3"};
        app.run(args);
        io.assertContains("Unrecognized option");
    }

    @Test
    public void helpWorksWithRightParameter() {
        String[] args = {"-h"};
        app.run(args);
        io.assertContains("Usage: tmc");
        io.assertContains("--help-all");
        io.assertContains("--help");
    }

    @Test
    public void helpDoesntHaveHiddenCommands() {
        String[] args = {"-h"};
        app.run(args);
        io.assertContains("Usage: tmc");
        io.assertNotContains("shell-helper");
    }

    @Test
    public void helpAllHasHiddenCommands() {
        String[] args = {"--help-all"};
        app.run(args);
        io.assertContains("Usage: tmc");
        io.assertContains("shell-helper");
    }

    @Test
    public void helpOfHelpCommandIsNotGiven() {
        String[] args = {"-h", "help"};
        app.run(args);
        io.assertContains("Usage: tmc");
        io.assertNotContains("Usage: tmc help");
    }

    @Test
    public void listOfEveryCommand() {
        String[] args = {"--help-all"};
        app.run(args);
        io.assertContains("TMC commands in all");
    }

    @Test
    public void helpOptionForAdminCommands() {
        String[] args = {"--help-admin"};
        app.run(args);
        io.assertContains("TMC commands in admin");
    }

    @Test
    public void helpOptionForHiddenCommandsDoesntExist() {
        String[] args = {"--help-hidden"};
        app.run(args);
        io.assertContains("Unrecognized option");
    }

    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        app.run(args);
        io.assertContains("Command foo doesn't exist");
    }

    @Test
    public void runAutoUpdate() {
        String[] args = {"help"};
        AutoUpdater mockUpdater = mock(AutoUpdater.class);
        when(AutoUpdater.createUpdater(any(Io.class), anyString(), any(Boolean.class)))
                .thenReturn(mockUpdater);
        when(mockUpdater.run()).thenReturn(true);

        CliContext ctx = spy(new CliContext(null, null, new WorkDir(), new Settings(), null));
        when(ctx.getProperties()).thenReturn(new HashMap<String, String>());

        app = new Application(ctx);
        app.run(args);

        verifyStatic(times(1));
        AutoUpdater.createUpdater(any(Io.class), anyString(), any(Boolean.class));
    }

    @Test
    public void runWithForceUpdate() {
        String[] args = {"--force-update", "foo"};
        app = spy(app);
        doReturn(true).when(app).runAutoUpdate();
        app.run(args);
        io.assertNotContains("Command foo doesn't exist.");
        verify(app, times(1)).runAutoUpdate();
    }
}
