package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsSettings;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.CliProgressObserver;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.ExerciseUpdater;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UpdateCommand.class, TmcUtil.class, SettingsIo.class})
public class UpdateCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";

    private static Path pathToDummyCourse;
    private static Path pathToDummyExercise;
    private static Path pathToDummyExerciseSrc;
    private static Path pathToNonCourseDir;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore core;
    private WorkDir workDir;
    private ExerciseUpdater exerciseUpdater;

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
    public void setUp() throws Exception {
        io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        AnalyticsSettings analyticsSettings = new AnalyticsSettings();
        EventSendBuffer eventSendBuffer = new EventSendBuffer(analyticsSettings, new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        ctx = new CliContext(io, this.core, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        mockStatic(TmcUtil.class);
        AccountList list = new AccountList();
        list.addAccount(new Account("username", "pass"));
        Account account = new Account("testuser", "password");
        account.setServerAddress("https://tmc.example.com");
        list.addAccount(account);

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);

        exerciseUpdater = PowerMockito.mock(ExerciseUpdater.class);
        PowerMockito.whenNew(ExerciseUpdater.class).withAnyArguments().thenReturn(exerciseUpdater);
    }

    @Test
    public void doNotRunIfNotLoggedIn() {
        ctx = spy(new CliContext(io, core, new WorkDir(pathToNonCourseDir), new Settings(), null));
        app = new Application(ctx);
        doReturn(false).when(ctx).checkIsLoggedIn();

        String[] args = {"update"};
        app.run(args);
        io.assertNotContains("Not a course directory");
    }

    @Test
    public void printsAnErrorMessageIfGivenCourseName() {
        String[] args = {"update", "course"};
        app.run(args);
        io.assertContains("Use in the course directory");
    }

    @Test
    public void printsAnErrorMessageIfUsedOutsideCourseDirectory() {
        workDir.setWorkdir(pathToNonCourseDir);
        String[] args = {"update"};
        app.run(args);
        io.assertContains("Not a course directory");
    }

    @Test
    public void worksRightIfAllExercisesAreUpToDate() {
        when(exerciseUpdater.updatesAvailable()).thenReturn(false);

        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"update"};
        app.run(args);

        io.assertContains("All exercises are up-to-date");
    }

    @Test
    public void updatesNewAndChangedExercises() {
        when(exerciseUpdater.updatesAvailable()).thenReturn(true);

        String newExerciseName = "new_exercise";
        Exercise newExercise = new Exercise(newExerciseName, COURSE_NAME);
        List<Exercise> newExercises = new ArrayList<>();
        newExercises.add(newExercise);

        String changedExerciseName = EXERCISE1_NAME;
        Exercise changedExercise = new Exercise(changedExerciseName, COURSE_NAME);
        List<Exercise> changedExercises = new ArrayList<>();
        changedExercises.add(changedExercise);

        when(exerciseUpdater.getNewExercises()).thenReturn(newExercises);
        when(exerciseUpdater.getUpdatedExercises()).thenReturn(changedExercises);

        List<Exercise> newAndChanged = new ArrayList<>();
        newAndChanged.addAll(newExercises);
        newAndChanged.addAll(changedExercises);
        when(exerciseUpdater.downloadUpdates(any(CliProgressObserver.class)))
                .thenReturn(newAndChanged);
        when(exerciseUpdater.updateCourseJson(any(CourseInfo.class), any(Path.class)))
                .thenReturn(true);
        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"update"};
        app.run(args);

        io.assertContains("New exercises:");
        io.assertContains(newExerciseName);

        io.assertContains("Modified exercises:");
        io.assertContains(changedExerciseName);

        verify(exerciseUpdater).updateCourseJson(any(CourseInfo.class), any(Path.class));
    }
}
