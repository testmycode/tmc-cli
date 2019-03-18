package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Mockito.mock;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;

import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
import org.junit.Before;
import org.junit.Test;

public class HelpCommandTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcCore core = new TmcCore(settings, tmcLangs);
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(eventSendBuffer);
        CliContext ctx = new CliContext(io, core, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);
    }

    @Test
    public void helpListsAllCommands() {
        String[] args = {"help"};
        app.run(args);
        io.assertContains("help");
    }
}
