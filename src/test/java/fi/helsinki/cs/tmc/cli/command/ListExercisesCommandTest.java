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
import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class ListExercisesCommandTest {
    
    private static final String COURSE_NAME = "2016-aalto-c";
    private static Path pathToDummyCourse;
    private static Path pathToNonCourseDir;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        pathToNonCourseDir = pathToDummyCourse.getParent();
        assertNotNull(pathToNonCourseDir);
    }
    
    @Before
    public void setUp() {
        io = new TestIo();
        mockCore = mock(TmcCore.class);
        ctx = new CliContext(io, mockCore);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        mockStatic(TmcUtil.class);
    }

    @Test
    public void failIfBackendFails() {
        ctx = spy(new CliContext(io, mockCore));
        app = new Application(ctx);
        doReturn(false).when(ctx).loadBackend();

        String[] args = {"exercises", "-n", "foo", "-i"};
        app.run(args);
        io.assertNotContains("Course 'foo' doesn't exist");
    }

    @Test
    public void worksLocallyIfNotInCourseDirectoryAndCourseIsSpecified() {
        workDir.setWorkdir(pathToNonCourseDir);
        String[] args = {"exercises", "fooCourse", "-n"};
        app.run(args);
        io.assertContains("You have to be in a course directory or use the -i");
    }
    
    @Test
    public void worksLocallyIfInCourseDirectoryAndRightCourseIsSpecified() {
        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"exercises", COURSE_NAME, "-n"};
        app.run(args);
        io.assertContains("Deadline:");
    }
    
    @Test
    public void worksLocallyIfInCourseDirectoryAndCourseIsNotSpecified() {
        workDir.setWorkdir(pathToDummyCourse);
        String[] args = {"exercises", "-n"};
        app.run(args);
        io.assertContains("Deadline:");
    }

    @Test
    public void giveMessageIfNoExercisesOnCourse() {
        Course course = new Course("test-course");
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("have any exercises");
    }

    @Test
    public void listExercisesGivesCorrectExercises() {
        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(new Exercise("first"), new Exercise("second")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("first");
        io.assertContains("second");
    }

    @Test
    public void emptyArgsGivesAnErrorMessage() {
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(null);
        String[] args = {"exercises", "-n"};
        app.run(args);
        io.assertContains("No course specified");
    }

    @Test
    public void failIfCourseDoesNotExist() {
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(null);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Course 'test-course' doesn't exist");
    }

    @Test
    public void exerciseIsCompletedButRequiresReview() {
        Exercise exercise = new Exercise("first-exercise");
        exercise.setRequiresReview(true);
        exercise.setReviewed(false);
        exercise.setCompleted(true);

        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(exercise,
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Requires review");
    }

    @Test
    public void exerciseIsCompletedAndReviewed() {
        Exercise exercise = new Exercise("first-exercise");
        exercise.setRequiresReview(true);
        exercise.setReviewed(true);
        exercise.setCompleted(true);

        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(exercise,
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Completed");
    }

    @Test
    public void exerciseIsCompleted() {
        Exercise exercise = new Exercise("first-exercise");
        exercise.setRequiresReview(false);
        exercise.setCompleted(true);

        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(exercise,
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Completed");
    }

    @Test
    public void exerciseIsNotCompleted() {
        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(
                new Exercise("first-exercise"),
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Not completed");
    }

    @Test
    public void exerciseHasBeenAttempted() {
        Exercise exercise = new Exercise("first-exercise");
        exercise.setAttempted(true);

        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(exercise,
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Attempted");
    }

    @Test
    public void exerciseDeadLinePassed() {
        Exercise exercise = new Exercise("first-exercise");
        exercise.setDeadline("2014-09-10T14:00:00.000+03:00");

        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(exercise,
                new Exercise("second-exercise")));
        when(TmcUtil.findCourse(eq(ctx), eq("test-course"))).thenReturn(course);

        String[] args = {"exercises", "-n", "test-course", "-i"};
        app.run(args);
        io.assertContains("Deadline passed");
    }
}
