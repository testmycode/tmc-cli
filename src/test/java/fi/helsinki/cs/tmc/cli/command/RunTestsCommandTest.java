package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/*TODO test the command line options */
public class RunTestsCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;
    static Path pathToDummyExerciseSrc;
    static Path pathToNonCourseDir;

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private Callable<RunResult> callableRunResult;

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
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        callableRunResult = new Callable<RunResult>() {
            @Override
            public RunResult call() throws Exception {
                RunResult.Status status = Status.PASSED;
                ImmutableList<TestResult> testResults = ImmutableList.of();
                ImmutableMap<String, byte[]> logs = ImmutableMap.of();
                RunResult result = new RunResult(status, testResults, logs);

                return result;
            }
        };
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
        when(mockCore.runTests((ProgressObserver) anyObject(),
                (Exercise) anyObject())).thenReturn(callableRunResult);

        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        String[] args = {"test"};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
        io.assertContains("Testing: " + EXERCISE2_NAME);
    }

    @Test
    public void worksInCourseDirectoryIfExerciseIsGiven() {
        when(mockCore.runTests((ProgressObserver) anyObject(),
                (Exercise) anyObject())).thenReturn(callableRunResult);

        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        String[] args = {"test", EXERCISE1_NAME};
        app.run(args);
        io.assertContains("Testing: " + EXERCISE1_NAME);
    }
}
