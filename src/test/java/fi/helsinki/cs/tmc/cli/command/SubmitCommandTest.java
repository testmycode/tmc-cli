package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class SubmitCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;

    Application app;
    TestIo io;
    TmcCore mockCore;

    @BeforeClass
    public static void setUpClass() {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).getPath());
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
        PowerMockito.mockStatic(TmcUtil.class);
    }

    @Test
    public void testSuccessInExerciseRoot() {
        Course course = new Course(COURSE_NAME);
        SubmissionResult result = new SubmissionResult();
        when(TmcUtil.findCourse(mockCore, COURSE_NAME)).thenReturn(course);
        when(TmcUtil.submitExercise(mockCore, course, EXERCISE1_NAME)).thenReturn(result);

        app.setWorkdir(new WorkDir(pathToDummyExercise));
        app.run(new String[]{"submit"});
        assertThat(io.out(), containsString("Submitting: " + EXERCISE1_NAME));
    }

    @Test
    public void canSubmitFromCourseDirIfExerciseNameIsGiven() {
        Course course = new Course(COURSE_NAME);
        SubmissionResult result = new SubmissionResult();
        when(TmcUtil.findCourse(mockCore, COURSE_NAME)).thenReturn(course);
        when(TmcUtil.submitExercise(mockCore, course, EXERCISE1_NAME)).thenReturn(result);

        app.setWorkdir(new WorkDir(pathToDummyCourse));
        app.run(new String[]{"submit", EXERCISE1_NAME});
        assertThat(io.out(), containsString("Submitting: " + EXERCISE1_NAME));
    }
}
