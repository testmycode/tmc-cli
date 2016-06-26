package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
@PrepareForTest(TmcUtil.class)
public class RunTestsCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;
    static Path pathToDummyExerciseSrc;

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private RunResult runResult;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        pathToDummyExercise = pathToDummyCourse.resolve(EXERCISE1_NAME);
        assertNotNull(pathToDummyExercise);

        pathToDummyExerciseSrc = pathToDummyExercise.resolve("src");
        assertNotNull(pathToDummyExerciseSrc);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        RunResult.Status status = Status.PASSED;
        ImmutableList<TestResult> testResults = ImmutableList.of();
        ImmutableMap<String, byte[]> logs = ImmutableMap.of();
        runResult = new RunResult(status, testResults, logs);

        mockStatic(TmcUtil.class);
    }

    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        app.getWorkDir().setWorkdir(pathToDummyCourse);
        String[] args = {"test"};
        app.run(args);
        io.assertNotContains("Testing:");
    }

    @Test
    public void givesAnErrorMessageIfNotInCourseDirectory() {
        workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        app.setWorkdir(workDir);
        String[] args = {"test"};
        app.run(args);
        io.assertContains("You have to be in a course directory");
    }

    @Test
    public void worksInCourseDirectory() {
        when(TmcUtil.runLocalTests(any(TmcCore.class), any(Exercise.class)))
                .thenReturn(runResult);

        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        String[] args = {"test"};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
        io.assertContains("Testing: " + EXERCISE2_NAME);
    }

    @Test
    public void worksInCourseDirectoryIfExerciseIsGiven() {
        when(TmcUtil.runLocalTests(any(TmcCore.class), any(Exercise.class)))
                .thenReturn(runResult);

        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        String[] args = {"test", EXERCISE1_NAME};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
    }
}
