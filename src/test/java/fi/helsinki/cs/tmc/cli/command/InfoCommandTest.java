package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.CourseFinder;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class InfoCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";

    private static Path pathToDummyCourse;
    private static Path pathToDummyExercise;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private Course course;

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
    }

    @Before
    public void setUp() {
        io = new TestIo();

        mockCore = new TmcCore(new Settings(), new TaskExecutorImpl());
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new Settings(), new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(new Settings(), eventSendBuffer);
        ctx = new CliContext(io, mockCore, new WorkDir(), new Settings(), analyticsFacade);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        course = new Course("test-course123");
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("exercise10"));
        exercises.get(0).setCompleted(true);
        course.setExercises(exercises);

        mockStatic(TmcUtil.class);
    }

    @Test
    public void failIfBackendFails() {
        app = new Application(ctx);

        String[] args = {"info", "course", "-i"};
        app.run(args);
        io.assertNotContains("doesn't exist on this server.");
    }

    @Test
    public void showMessageIfCourseIsNotGiven() {
        String[] args = {"info"};
        app.run(args);
        io.assertContains("You have to be in a course directory");
    }

    @Test
    public void showErrorMessageIfNoCourseGivenWithIOption() {
        String[] args = {"info", "-i"};
        app.run(args);
        io.assertContains("You must give a course");
    }

    //TODO we should test this in CourseFinder not here.
    @Test
    public void dontShowCourseInfoIfTheCourseDoesntExist() {
        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("foo"))).thenReturn(false);
        String[] args = {"info", "foo", "-i"};
        app.run(args);
        io.assertNotContains("Course name:");
    }

    @Test
    public void printCourseWithInternet() {
        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("test-course123"))).thenReturn(true);
        when(mockCourseFinder.getCourse()).thenReturn(course);
        when(mockCourseFinder.getAccount()).thenReturn(new Account());

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("Course name: test-course123");
        io.assertNotContains("Statistics URLs");
    }

    @Test
    public void printAllCourseExercisesWithInternet() {
        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("test-course123"))).thenReturn(true);
        when(mockCourseFinder.getCourse()).thenReturn(course);
        when(mockCourseFinder.getAccount()).thenReturn(new Account());

        String[] args = {"info", "test-course123", "-a", "-i"};
        app.run(args);
        io.assertContains("Statistics URLs");
    }

    @Test
    public void printCourseWithNoExercisesFromTheServer() {
        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("test-course123"))).thenReturn(true);
        when(mockCourseFinder.getCourse()).thenReturn(course);
        when(mockCourseFinder.getAccount()).thenReturn(new Account());

        course.setExercises(new ArrayList<Exercise>());

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("Exercises: -");
    }

    @Test
    public void printExerciseIfInExerciseDirectoryWithoutParameters() {
        workDir.setWorkdir(pathToDummyExercise);
        String[] args = {"info"};
        app.run(args);
        io.assertContains(EXERCISE1_NAME);
        io.assertContains("Exercise");
    }

    @Test
    public void printExerciseIfInCourseDirectoryAndGivenExerciseName() {
        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"info", EXERCISE1_NAME};
        app.run(args);
        io.assertContains(EXERCISE1_NAME);
        io.assertContains("Exercise");
    }

    @Test
    public void printCourseIfInCourseDirectoryWithoutParameters() {
        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"info"};
        app.run(args);
        io.assertContains("2016-aalto-c");
    }

    @Test
    public void printErrorMessageIfNotInCourseDirectoryAndCourseDoesntExist() {
        workDir.setWorkdir(Paths.get(System.getProperty("java.io.tmpdir")));
        String[] args = {"info", "notacourse"};
        app.run(args);
    }

    @Test
    public void printGivenCourseFromTheServerIfInCourseDirectoryAndGivenCourseName() {
        workDir.setWorkdir(pathToDummyCourse);

        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("test-course123"))).thenReturn(true);
        when(mockCourseFinder.getCourse()).thenReturn(course);
        when(mockCourseFinder.getAccount()).thenReturn(new Account());
        course.setExercises(new ArrayList<Exercise>());

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("test-course123");
    }

    @Test
    public void printsErrorIfInCourseDirectoryAndGivenCourseNameThatDoesntExistOnTheServer() {
        workDir.setWorkdir(pathToDummyCourse);

        ctx = spy(ctx);
        app = new Application(ctx);
        CourseFinder mockCourseFinder = mock(CourseFinder.class);
        doReturn(mockCourseFinder).when(ctx).createCourseFinder();
        when(mockCourseFinder.search(eq("test-course123"))).thenReturn(false);

        String[] args = {"info", "notacourse", "-i"};
        app.run(args);
        io.assertNotContains("Course name:");
    }

    @Test
    public void printsLongExerciseInfoWithOption() {
        workDir.setWorkdir(pathToDummyExercise);
        String[] args = {"info", "-a"};
        app.run(args);

        io.assertContains("Exercise name");
        io.assertContains("Is returnable");
    }
}
