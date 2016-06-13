package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
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
import org.junit.Test;

import java.nio.file.Paths;
import java.util.concurrent.Callable;

public class RunTestsCommandTest {
    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private Callable<RunResult> callableRunResult;

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
    public void givesAnErrorMessageIfNotInCourseDirectory() {
        workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        app.setWorkdir(workDir);
        String[] args = {"run-tests"};
        app.run(args);
        assertTrue(io.getPrint().contains("You have to be in the exercise root"));
    }

    @Test
    public void worksInCourseDirectory()  {
        when(mockCore.runTests((ProgressObserver) anyObject(),
                (Exercise) anyObject())).thenReturn(callableRunResult);

        String pathToDummycourse = RunTestsCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c").getPath();

        workDir = new WorkDir(Paths.get(pathToDummycourse));
        app.setWorkdir(workDir);

        String[] args = {"run-tests"};
        app.run(args);
        assertTrue(io.getPrint().contains("Testing: Module_1-04_func"));
    }

    @Test
    public void worksInCourseDirectoryIfExerciseIsGiven()  {
        when(mockCore.runTests((ProgressObserver) anyObject(),
                (Exercise) anyObject())).thenReturn(callableRunResult);

        String pathToDummycourse = RunTestsCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c").getPath();

        workDir = new WorkDir(Paths.get(pathToDummycourse));
        app.setWorkdir(workDir);

        String[] args = {"run-tests", "Module_1-02_intro"};
        app.run(args);
        assertTrue(io.getPrint().contains("Testing: Module_1-02_intro"));
    }

}
