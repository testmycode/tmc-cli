package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

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
import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TmcUtil.class, CourseInfoIo.class, SettingsIo.class})
public class SubmitCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";
    private static final String EXERCISE_WITH_DEADLINE_PASSED = "Module_1-05_calc";

    private static Path pathToDummyCourse;
    private static Path pathToDummyExercise;
    private static Path pathToDummyExerciseSrc;
    private static Path pathToNonCourseDir;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore core;
    private WorkDir workDir;
    private AnalyticsFacade analyticsFacade;

    private Course course;
    private SubmissionResult result;
    private SubmissionResult result2;

    private AccountList list;

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

        pathToNonCourseDir = pathToDummyCourse.getParent();
        assertNotNull(pathToNonCourseDir);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        SpywareSettings analyticsSettings = new Settings();
        analyticsFacade = spy(new AnalyticsFacade(analyticsSettings, new EventSendBuffer(analyticsSettings, new EventStore())));
        ctx = new CliContext(io, this.core, new WorkDir(), new Settings(), this.analyticsFacade);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        course = new Course(COURSE_NAME);
        result = new SubmissionResult();
        result2 = new SubmissionResult();

        mockStatic(TmcUtil.class);
        when(TmcUtil.findCourse(any(CliContext.class), any(String.class))).thenReturn(course);
        when(TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class)))
                .thenReturn(result)
                .thenReturn(result2);
        list = new AccountList();
        list.addAccount(new Account("username"));
        Account account = new Account("testuser");
        account.setServerAddress("https://tmc.example.com");
        list.addAccount(account);

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);

        mockStatic(CourseInfoIo.class);
        when(CourseInfoIo.load(any(Path.class))).thenCallRealMethod();
        when(CourseInfoIo.save(any(CourseInfo.class), any(Path.class))).thenReturn(true);
    }

    @Test
    public void doNotRunIfNotLoggedIn() {
        ctx = spy(new CliContext(io, core, workDir, new Settings(), analyticsFacade));
        app = new Application(ctx);
        doReturn(false).when(ctx).checkIsLoggedIn(false, true);

        String[] args = {"submit"};
        app.run(args);
        io.assertNotContains("Submitting");
    }

    @Test
    public void canSubmitFromCourseDirIfExerciseNameIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME});
        io.assertContains("Submitting: " + EXERCISE1_NAME);

        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void canSubmitMultipleExercisesIfNamesAreGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME, EXERCISE2_NAME});
        io.assertContains("Submitting: " + EXERCISE1_NAME);
        io.assertContains("Submitting: " + EXERCISE2_NAME);

        verifyStatic(times(2));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void submitsAllExercisesFromCourseDirIfNoNameIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME, EXERCISE2_NAME, EXERCISE_WITH_DEADLINE_PASSED});
        io.assertContains("Submitting: " + EXERCISE1_NAME);
        io.assertContains("Submitting: " + EXERCISE2_NAME);
        io.assertContains("Submitting: " + EXERCISE_WITH_DEADLINE_PASSED);
        assertEquals(3, countSubstring("Submitting: ", io.out()));

        // the third one's deadline is passed so it should not be submitted
        verifyStatic(times(2));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void doesNotSubmitExtraExercisesFromCourseDir() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME});
        assertEquals(1, countSubstring("Submitting: ", io.out()));
    }

    @Test
    public void abortIfInvalidCmdLineArgumentIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME, "-foo"});
        io.assertContains("Invalid command line argument");

        verifyStatic(times(0));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void abortIfInvalidExerciseNameIsGivenAsArgument() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", "foo"});
        io.assertContains("Error: foo is not a valid exercise.");
        assertEquals(0, countSubstring("Submitting: ", io.out()));
    }

    @Test
    public void showFailMsgIfSubmissionFailsInCore() {
        when(TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class))).thenReturn(null);
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE1_NAME});

        assertEquals(1, countSubstring("Submitting: ", io.out()));
        assertEquals(1, countSubstring("Submission failed.", io.out()));

        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void doesNotShowUpdateMessageIfNoUpdatesAvailable() {
        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[] {"submit", EXERCISE1_NAME});

        io.assertNotContains("available");
        io.assertNotContains("been changed on TMC server");
        io.assertNotContains("Use 'tmc update' to download");
    }

    @Test
    public void showsMessageIfNewExercisesAreAvailable() {
        UpdateResult updateResult = mock(UpdateResult.class);

        List<Exercise> newExercises = Collections.singletonList(new Exercise("new_exercise"));
        List<Exercise> updatedExercises =
                Collections.singletonList(new Exercise("updated_exercise"));

        when(updateResult.getNewExercises()).thenReturn(newExercises);
        when(updateResult.getUpdatedExercises()).thenReturn(updatedExercises);

        when(TmcUtil.getUpdatableExercises(any(CliContext.class), any(Course.class)))
                .thenReturn(updateResult);

        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE2_NAME});

        io.assertContains("1 new exercise available");
        io.assertContains("1 exercise has been changed on TMC server");
        io.assertContains("Use 'tmc update' to download them");
    }

    @Test
    public void notifyUserAndDontSubmitIfDeadlinePassed() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[] {"submit", EXERCISE_WITH_DEADLINE_PASSED});
        io.assertContains("Deadline has passed for this exercise");
        verifyStatic(times(0));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void sendAnalyticsIsCalledOnSubmit() {
        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[] {"submit"});
        verify(analyticsFacade).sendAnalytics();
    }

    private static int countSubstring(String subStr, String str) {
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }
}
