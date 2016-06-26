package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
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
import org.powermock.api.mockito.PowerMockito;
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
    private TestIo io;
    private TmcCore mockCore;
    private Course course;
    private WorkDir workDir;

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
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        course = new Course("test-course123");
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("exercise10"));
        exercises.get(0).setCompleted(true);
        course.setExercises(exercises);

        PowerMockito.mockStatic(TmcUtil.class);
    }

    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"info", "course", "-i"};
        app.run(args);
        assertFalse(io.out().contains("doesn't exist on this server."));
    }

    @Test
    public void showMessageIfCourseIsNotGiven() {
        String[] args = {"info"};
        app.run(args);
        assertTrue(io.out().contains("You have to be in a course directory"));
    }

    @Test
    public void showErrorMessageIfNoCourseGivenWithIOption() {
        String[] args = {"info", "-i"};
        app.run(args);
        assertTrue(io.out().contains("You must give a course as a parameter."));
    }

    @Test
    public void showMessageIfCourseDoesNotExistOnTheServer() {
        when(TmcUtil.findCourse(eq(mockCore), eq("foo"))).thenReturn(null);
        String[] args = {"info", "foo", "-i"};
        app.run(args);
        assertTrue(io.out().contains("The course foo doesn't exist on this server"));
    }

    @Test
    public void printCourseWithOptionI() {
        when(TmcUtil.findCourse(eq(mockCore), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        assertTrue(io.out().contains("Course name: test-course123"));
        assertFalse(io.out().contains("Statistics URLs"));
    }

    @Test
    public void printCourseWithOptionsIAndA() {
        when(TmcUtil.findCourse(eq(mockCore), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-a", "-i"};
        app.run(args);
        assertTrue(io.out().contains("Statistics URLs"));
    }

    @Test
    public void printCourseWithNoExercisesFromTheServer() {
        course.setExercises(new ArrayList<Exercise>());
        when(TmcUtil.findCourse(eq(mockCore), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        assertTrue(io.out().contains("Exercises: -"));
    }

    @Test
    public void printExerciseIfInExerciseDirectoryWithoutParameters() {
        workDir = new WorkDir(pathToDummyExercise);
        app.setWorkdir(workDir);
        String[] args = {"info"};
        app.run(args);
        assertTrue(io.out().contains(EXERCISE1_NAME));
        assertTrue(io.out().contains("Exercise"));
    }

    @Test
    public void printExerciseIfInCourseDirectoryAndGivenExerciseName() {
        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);
        String[] args = {"info", EXERCISE1_NAME};
        app.run(args);
        assertTrue(io.out().contains(EXERCISE1_NAME));
        assertTrue(io.out().contains("Exercise"));
    }

    @Test
    public void printCourseIfInCourseDirectoryWithoutParameters() {
        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);
        String[] args = {"info"};
        app.run(args);
        assertTrue(io.out().contains(("2016-aalto-c")));
    }

    @Test
    public void printErrorMessageIfNotInCourseDirectoryAndCourseDoesntExist() {
        workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        app.setWorkdir(workDir);
        String[] args = {"info", "notacourse"};
        app.run(args);
    }

    @Test
    public void printGivenCourseFromTheServerIfInCourseDirectoryAndGivenCourseName() {
        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        course.setExercises(new ArrayList<Exercise>());
        when(TmcUtil.findCourse(eq(mockCore), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        assertTrue(io.out().contains("test-course123"));
    }

    @Test
    public void printsErrorIfInCourseDirectoryAndGivenCourseNameThatDoesntExistOnTheServer() {
        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        when(TmcUtil.findCourse(eq(mockCore), eq("test-course123"))).thenReturn(course);

        String[] args = {"info", "notacourse", "-i"};
        app.run(args);
        assertTrue(io.out().contains("doesn't exist on this server."));
    }

    @Test
    public void printsLongExerciseInfoWithOption() {
        workDir = new WorkDir(pathToDummyExercise);
        app.setWorkdir(workDir);
        String[] args = {"info", "-a"};
        app.run(args);

        assertTrue(io.out().contains("Exercise name"));
        assertTrue(io.out().contains("Is returnable"));
    }
}
