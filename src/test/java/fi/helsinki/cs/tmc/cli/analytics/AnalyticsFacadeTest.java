package fi.helsinki.cs.tmc.cli.analytics;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.command.SubmitCommandTest;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.LoggableEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
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
    private static Path pathToDummyCourse;
    private WorkDir workDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse =
                Paths.get(
                        SubmitCommandTest.class
                                .getClassLoader()
                                .getResource("dummy-courses/2016-aalto-c")
                                .toURI());
        assertNotNull(pathToDummyCourse);
    }

        @Before
    public void setUp() {

        TestIo io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        TmcCore core = new TmcCore(settings, tmcLangs);
        analyticsSettings = mock(Settings.class);
        eventSendBuffer = mock(EventSendBuffer.class);
        analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        CliContext ctx = new CliContext(io, core, new WorkDir(), settings, analyticsFacade);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        mockStatic(TmcUtil.class);
        list = new AccountList();
        list.addAccount(new Account("username"));

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);

    }

    @Test
    public void analyticsNotSentIfSpywareIsNotEnabled() {
        workDir.setWorkdir(pathToDummyCourse);
        when(analyticsSettings.isSpywareEnabled()).thenReturn(false);
        app.run(new String[] {"submit", "Module_1-02_intro"});
        verify(eventSendBuffer, never()).sendNow();
    }

    @Test
    public void analyticsisSentOnSubmitIfSpywareIsEnabled() {
        workDir.setWorkdir(pathToDummyCourse);
        when(analyticsSettings.isSpywareEnabled()).thenReturn(true);
        app.run(new String[] {"submit", "Module_1-02_intro"});
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
