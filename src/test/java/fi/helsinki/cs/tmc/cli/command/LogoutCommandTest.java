package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SettingsIo.class)
public class LogoutCommandTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        TmcCore core = new TmcCore(new Settings(), new TaskExecutorImpl());
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(new EventSendBuffer(new EventStore()));
        app = new Application(new CliContext(io, core, new WorkDir(), new Settings(), analyticsFacade));

        mockStatic(SettingsIo.class);
        AccountList t = new AccountList();
        t.addAccount(new Account("username"));
        when(SettingsIo.loadAccountList()).thenReturn(t);
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
    }

    @Test
    public void logoutShouldDeleteSettings() {
        String[] args = {"logout"};
        app.run(args);

        verifyStatic(times(1));
        SettingsIo.delete();

        io.assertContains("Logged out.");
    }
}
