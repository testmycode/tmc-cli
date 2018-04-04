package fi.helsinki.cs.tmc.cli.command.hidden;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import fi.helsinki.cs.tmc.spyware.SpywareSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class DocumentCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        SpywareSettings analyticsSettings = new Settings();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcCore core = new TmcCore(settings, tmcLangs);

        EventSendBuffer eventSendBuffer = new EventSendBuffer(analyticsSettings, new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        ctx = new CliContext(io, core, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);

        mockStatic(EnvironmentUtil.class);
        when(EnvironmentUtil.getTerminalWidth()).thenReturn(100);
    }

    @Test
    public void run() throws InterruptedException {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        app = new Application(ctx);

        String[] args = {"document", "-s", "0"};
        app.run(args);
        io.assertContains("Original dev team");
    }

    @Test
    public void failOnWindows() throws InterruptedException {
        when(EnvironmentUtil.isWindows()).thenReturn(true);
        app = new Application(ctx);

        String[] args = {"document", "-s", "0"};
        app.run(args);
        io.assertContains("Command document doesn't exist");
    }
}
