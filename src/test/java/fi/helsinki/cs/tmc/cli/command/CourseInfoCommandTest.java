package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

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
public class CourseInfoCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private Course course;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        pathToDummyExercise = pathToDummyCourse.resolve(EXERCISE1_NAME);
        assertNotNull(pathToDummyExercise);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        mockCore = mock(TmcCore.class);
        ctx = new CliContext(io, mockCore);
        app = new Application(ctx);

        course = new Course("test-course123");
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("exercise10"));
        exercises.get(0).setCompleted(true);
        course.setExercises(exercises);

        mockStatic(TmcUtil.class);
    }

    @Test
    public void failIfBackendFails() {
        ctx = spy(new CliContext(io, mockCore));
        app = new Application(ctx);
        doReturn(false).when(ctx).loadBackend();

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

    @Test
    public void showMessageIfCourseDoesNotExistOnTheServer() {
        when(TmcUtil.findCourse(eq(ctx), eq("foo"))).thenReturn(null);
        String[] args = {"info", "foo", "-i"};
        app.run(args);
        io.assertContains("course foo doesn't exist");
    }

    @Test
    public void printCourseWithOptionI() {
        when(TmcUtil.findCourse(eq(ctx), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("Course name: test-course123");
        io.assertNotContains("Statistics URLs");
    }

    @Test
    public void printCourseWithOptionsIAndA() {
        when(TmcUtil.findCourse(eq(ctx), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-a", "-i"};
        app.run(args);
        io.assertContains("Statistics URLs");
    }

    @Test
    public void printCourseWithNoExercisesFromTheServer() {
        course.setExercises(new ArrayList<Exercise>());
        when(TmcUtil.findCourse(eq(ctx), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("Exercises: -");
    }

    @Test
    public void printExerciseIfInExerciseDirectoryWithoutParameters() {
        workDir = new WorkDir(pathToDummyExercise);
        ctx.setWorkdir(workDir);
        String[] args = {"info"};
        app.run(args);
        io.assertContains(EXERCISE1_NAME);
        io.assertContains("Exercise");
    }

    @Test
    public void printExerciseIfInCourseDirectoryAndGivenExerciseName() {
        workDir = new WorkDir(pathToDummyCourse);
        ctx.setWorkdir(workDir);
        String[] args = {"info", EXERCISE1_NAME};
        app.run(args);
        io.assertContains(EXERCISE1_NAME);
        io.assertContains("Exercise");
    }

    @Test
    public void printCourseIfInCourseDirectoryWithoutParameters() {
        workDir = new WorkDir(pathToDummyCourse);
        ctx.setWorkdir(workDir);
        String[] args = {"info"};
        app.run(args);
        io.assertContains("2016-aalto-c");
    }

    @Test
    public void printErrorMessageIfNotInCourseDirectoryAndCourseDoesntExist() {
        workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        ctx.setWorkdir(workDir);
        String[] args = {"info", "notacourse"};
        app.run(args);
    }

    @Test
    public void printGivenCourseFromTheServerIfInCourseDirectoryAndGivenCourseName() {
        workDir = new WorkDir(pathToDummyCourse);
        ctx.setWorkdir(workDir);

        course.setExercises(new ArrayList<Exercise>());
        when(TmcUtil.findCourse(eq(ctx), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        io.assertContains("test-course123");
    }

    @Test
    public void printsErrorIfInCourseDirectoryAndGivenCourseNameThatDoesntExistOnTheServer() {
        workDir = new WorkDir(pathToDummyCourse);
        ctx.setWorkdir(workDir);

        when(TmcUtil.findCourse(eq(ctx), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "notacourse", "-i"};
        app.run(args);
        io.assertContains("doesn't exist on this server.");
    }

    @Test
    public void printsLongExerciseInfoWithOption() {
        workDir = new WorkDir(pathToDummyExercise);
        ctx.setWorkdir(workDir);
        String[] args = {"info", "-a"};
        app.run(args);

        io.assertContains("Exercise name");
        io.assertContains("Is returnable");
    }
}
