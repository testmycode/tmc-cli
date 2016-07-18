package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TmcUtil.class, CourseInfoIo.class})
public class SubmitCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    private static Path pathToDummyCourse;
    private static Path pathToDummyExercise;
    private static Path pathToDummyExerciseSrc;
    private static Path pathToNonCourseDir;

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;

    private Course course;
    private SubmissionResult result;
    private SubmissionResult result2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
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
        mockCore = mock(TmcCore.class);
        ctx = new CliContext(io, mockCore);
        app = new Application(ctx);
        workDir = ctx.getWorkDir();

        course = new Course(COURSE_NAME);
        result = new SubmissionResult();
        result2 = new SubmissionResult();

        mockStatic(TmcUtil.class);
        when(TmcUtil.findCourse(any(CliContext.class), any(String.class))).thenReturn(course);
        when(TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class)))
                .thenReturn(result).thenReturn(result2);

        mockStatic(CourseInfoIo.class);
        when(CourseInfoIo.load(any(Path.class))).thenCallRealMethod();
        when(CourseInfoIo.save(any(CourseInfo.class), any(Path.class))).thenReturn(true);
    }

    @Test
    public void testSuccessInExerciseRoot() {
        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[]{"submit"});
        io.assertContains("Submitting: " + EXERCISE1_NAME);

        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void canSubmitFromCourseDirIfExerciseNameIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", EXERCISE1_NAME});
        io.assertContains("Submitting: " + EXERCISE1_NAME);

        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void canSubmitMultipleExercisesIfNamesAreGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", EXERCISE1_NAME, EXERCISE2_NAME});
        io.assertContains("Submitting: " + EXERCISE1_NAME);
        io.assertContains("Submitting: " + EXERCISE2_NAME);

        verifyStatic(times(2));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void submitsAllExercisesFromCourseDirIfNoNameIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit"});
        io.assertContains("Submitting: " + EXERCISE1_NAME);
        io.assertContains("Submitting: " + EXERCISE2_NAME);
        assertEquals(2, countSubstring("Submitting: ", io.out()));

        verifyStatic(times(2));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void doesNotSubmitExtraExercisesFromExerciseRoot() {
        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[]{"submit"});
        assertEquals(1, countSubstring("Submitting: ", io.out()));
    }

    @Test
    public void doesNotSubmitExtraExercisesFromCourseDir() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", EXERCISE1_NAME});
        assertEquals(1, countSubstring("Submitting: ", io.out()));
    }

    @Test
    public void abortIfInvalidCmdLineArgumentIsGiven() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", EXERCISE1_NAME, "-foo"});
        io.assertContains("Invalid command line argument");

        verifyStatic(times(0));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void abortIfInvalidExerciseNameIsGivenAsArgument() {
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", "foo"});
        io.assertContains("Error: foo is not a valid exercise.");
        assertEquals(0, countSubstring("Submitting: ", io.out()));
    }

    @Test
    public void abortGracefullyIfNotInCourseDir() {
        workDir.setWorkdir(pathToNonCourseDir);
        app.run(new String[]{"submit"});
        io.assertContains("No exercises specified.");

        verifyStatic(times(0));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void showFailMsgIfSubmissionFailsInCore() {
        when(TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class))).thenReturn(null);
        workDir.setWorkdir(pathToDummyCourse);
        app.run(new String[]{"submit", EXERCISE1_NAME});

        assertEquals(1, countSubstring("Submitting: ", io.out()));
        assertEquals(1, countSubstring("Submission failed.", io.out()));

        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void canSubmitFromExerciseSubdirs() {
        workDir.setWorkdir(pathToDummyExerciseSrc);
        app.run(new String[]{"submit"});

        io.assertContains("Submitting: " + EXERCISE1_NAME);
        verifyStatic(times(1));
        TmcUtil.submitExercise(any(CliContext.class), any(Exercise.class));
    }

    @Test
    public void doesNotShowUpdateMessageIfNoUpdatesAvailable() {
        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[]{"submit"});

        io.assertNotContains("available");
        io.assertNotContains("been changed on TMC server");
        io.assertNotContains("Use 'tmc update' to download");
    }

    @Test
    public void showsMessageIfNewExercisesAreAvailable() {
        UpdateResult updateResult = mock(UpdateResult.class);

        List<Exercise> newExercises = Arrays.asList(new Exercise("new_exercise"));
        List<Exercise> updatedExercises = Arrays.asList(new Exercise("updated_exercise"));

        when(updateResult.getNewExercises()).thenReturn(newExercises);
        when(updateResult.getUpdatedExercises()).thenReturn(updatedExercises);

        when(TmcUtil.getUpdatableExercises(any(CliContext.class), any(Course.class)))
                .thenReturn(updateResult);

        workDir.setWorkdir(pathToDummyExercise);
        app.run(new String[]{"submit"});

        io.assertContains("1 new exercise available");
        io.assertContains("1 exercise has been changed on TMC server");
        io.assertContains("Use 'tmc update' to download them");
    }

    private static int countSubstring(String subStr, String str) {
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }
}
