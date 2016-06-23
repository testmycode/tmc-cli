package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class CourseInfoCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private Course course;
    private Callable<List<Course>> callableList;
    private Callable<Course> callableCourse;
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

        callableList = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(course);
            }
        };

        callableCourse = new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                return course;
            }
        };
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
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        String[] args = {"info", "foo", "-i"};
        app.run(args);
        assertTrue(io.out().contains("The course foo doesn't exist on this server"));
    }

    @Test
    public void printCourseWithOptionI() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        assertTrue(io.out().contains("Course name: test-course123"));
        assertFalse(io.out().contains("Statistics URLs"));
    }

    @Test
    public void printCourseWithOptionsIAndA() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);

        String[] args = {"info", "test-course123", "-a", "-i"};
        app.run(args);
        assertTrue(io.out().contains("Statistics URLs"));
    }

    @Test
    public void printCourseWithNoExercisesFromTheServer() {
        course.setExercises(new ArrayList<Exercise>());
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);

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
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);

        String[] args = {"info", "test-course123", "-i"};
        app.run(args);
        assertTrue(io.out().contains("test-course123"));
    }

    @Test
    public void printsErrorIfInCourseDirectoryAndGivenCourseNameThatDoesntExistOnTheServer() {
        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);

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
