package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsSettings;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;

import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TmcUtil.class, SettingsIo.class})
public class ListCoursesCommandTest {

    private Application app;
    private CliContext ctx;
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
        ctx = new CliContext(io, core, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);
        Account account = new Account( "", "");
        AccountList accountList = new AccountList();
        accountList.addAccount(account);

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(TmcUtil.hasConnection(eq(ctx))).thenReturn(true);
        when(SettingsIo.loadAccountList()).thenReturn(accountList);
    }

    @Test
    public void failIfThereIsNoConnection() {
        when(TmcUtil.hasConnection(eq(ctx))).thenReturn(false);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("don't have internet connection");
    }

    @Test
    public void listCoursesWorksWithNoCourses() {
        List<Course> list = Collections.emptyList();
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(list);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("No courses found");
    }

    @Test
    public void listCoursesWorksWithCourses() {
        List<Course> list = Arrays.asList(new Course("course1"), new Course("course2"));
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(list);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("Found 2 courses");
    }
}
