package fi.helsinki.cs.tmc.cli.analytics;


import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.LoggableEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SettingsIo.class, TmcUtil.class})
public class AnalyticsFacadeTest {
        private Settings analyticsSettings;
        private AnalyticsFacade analyticsFacade;
        private EventSendBuffer eventSendBuffer;
        private Application app;
        private AccountList list;

    @Before
    public void setUp() {
        TestIo io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcCore core = new TmcCore(settings, tmcLangs);
        analyticsSettings = mock(Settings.class);
        eventSendBuffer = mock(EventSendBuffer.class);
        analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        app = new Application(new CliContext(io, core, new WorkDir(), settings, analyticsFacade));

        mockStatic(TmcUtil.class);
        list = new AccountList();
        list.addAccount(new Account("username", "pass"));

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);

    }

    @Test
    public void analyticsNotSentIfSpywareIsNotEnabled() {
        when(analyticsSettings.isSpywareEnabled()).thenReturn(false);
        app.run(new String[] {"submit"});
        verify(eventSendBuffer, never()).sendNow();
    }

    @Test
    public void analyticsisSentOnSubmitIfSpywareIsEnabled() {
        when(analyticsSettings.isSpywareEnabled()).thenReturn(true);
        app.run(new String[] {"submit"});
        verify(eventSendBuffer).sendNow();
    }

    @Test
    public void analyticsIsSavedIfSpywareIsEnabled() {
        when(analyticsSettings.isSpywareEnabled()).thenReturn(true);
        app.run(new String[] {"courses"});
        verify(eventSendBuffer, times(1)).receiveEvent(any(LoggableEvent.class));
    }

    @Test
    public void analyticsIsNotSavedIfSpywareIsNotEnabled() {
        when(analyticsSettings.isSpywareEnabled()).thenReturn(false);
        app.run(new String[] {"courses"});
        verify(eventSendBuffer, never()).receiveEvent(any(LoggableEvent.class));
    }
}
