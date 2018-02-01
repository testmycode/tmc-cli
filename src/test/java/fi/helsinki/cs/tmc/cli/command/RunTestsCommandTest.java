package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import fi.helsinki.cs.tmc.spyware.SpywareSettings;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

/*TODO test the command line options */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ TmcUtil.class, SettingsIo.class })
public class RunTestsCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    private static Path pathToDummyCourse;
    private static Path pathToDummyExercise;
    private static Path pathToDummyExerciseSrc;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private RunResult runResult;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse =
                Paths.get(
                        SubmitCommandTest.class
                                .getClassLoader()
                                .getResource("dummy-courses/" + COURSE_NAME)
                                .toURI());
        assertNotNull(pathToDummyCourse);

        pathToDummyExercise = pathToDummyCourse.resolve(EXERCISE1_NAME);
        assertNotNull(pathToDummyExercise);

        pathToDummyExerciseSrc = pathToDummyExercise.resolve("src");
        assertNotNull(pathToDummyExerciseSrc);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        mockCore = new TmcCore(settings, tmcLangs);
        SpywareSettings analyticsSettings = new Settings();
        EventSendBuffer eventSendBuffer = new EventSendBuffer(analyticsSettings, new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        ctx = new CliContext(io, mockCore, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        RunResult.Status status = Status.PASSED;
        ImmutableList<TestResult> testResults = ImmutableList.of();
        ImmutableMap<String, byte[]> logs = ImmutableMap.of();
        runResult = new RunResult(status, testResults, logs);

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(TmcUtil.hasConnection(eq(ctx))).thenReturn(true);
        AccountList t = new AccountList();
        t.addAccount(new Account("testuser"));
        when(SettingsIo.loadAccountList()).thenReturn(t);
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
    }

    @Test
    public void doNotRunIfNotLoggedIn() {
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        app = new Application(ctx);

        String[] args = {"test"};
        app.run(args);
        io.assertContains("You are not logged in");
    }

    @Test
    public void givesAnErrorMessageIfNotInCourseDirectory() {
        workDir.setWorkdir(Paths.get(System.getProperty("java.io.tmpdir")));
        String[] args = {"test"};
        app.run(args);
        io.assertContains("No exercises specified");
    }

    @Test
    public void worksInCourseDirectory() {
        when(TmcUtil.runLocalTests(eq(ctx), any(Exercise.class))).thenReturn(runResult);

        workDir.setWorkdir(pathToDummyCourse);

        String[] args = {"test"};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
        io.assertContains("Testing: " + EXERCISE2_NAME);
    }

    @Test
    public void worksInCourseDirectoryIfExerciseIsGiven() {
        when(TmcUtil.runLocalTests(eq(ctx), any(Exercise.class))).thenReturn(runResult);

        workDir.setWorkdir(pathToDummyCourse);

        String[] args = {"test", EXERCISE1_NAME};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
    }
}
