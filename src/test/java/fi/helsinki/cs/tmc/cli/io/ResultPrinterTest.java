package fi.helsinki.cs.tmc.cli.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.TestResultStatus;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class ResultPrinterTest {

    private Application app;
    private TestIo io;
    private ResultPrinter printer;
    private SubmissionResult mockSubResult;
    private RunResult runResult;
    private ImmutableList<TestResult> testResults;
    private ImmutableMap<String, byte[]> logs;

    @Before
    public void setUp() {
        io = new TestIo();
        printer = new ResultPrinter(io, true, true);
        mockSubResult = mock(SubmissionResult.class);
        logs = ImmutableMap.of();
        app = mock(Application.class);
        when(app.getProperties()).thenReturn(new HashMap<String, String>());
    }

    @Test
    public void printSubmissionResultWorksIfResultIsNull() {
        printer.printSubmissionResult(null, false, null, null);
        io.assertEquals("");
    }

    @Test
    public void printSubmissionResultWorksIfAllTestsPass() {
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.OK);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.NONE_FAILED);
        printer.printSubmissionResult(mockSubResult, false, null, null);
        io.assertContains("All tests passed on server!");
    }

    @Test
    public void printSubmissionResultWorksIfAllTestsFail() {
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.FAIL);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.ALL_FAILED);
        printer.printSubmissionResult(mockSubResult, false, null, null);
        io.assertContains("All tests failed on server.");
    }

    @Test
    public void printSubmissionResultWorksIfSomeTestsFail() {
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.FAIL);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.SOME_FAILED);
        printer.printSubmissionResult(mockSubResult, false, null, null);
        io.assertContains("Some tests failed on server.");
    }

    @Test
    public void printRunResultWorksIfTestsPass() {
        testResults = ImmutableList.of(new TestResult("test1", true, "Cool!"));
        runResult = new RunResult(Status.PASSED, testResults, logs);
        printer.printRunResult(runResult, false, false, null, null);
        io.assertContains("All tests passed!");
    }

    @Test
    public void printRunResultWorksIfTestsFail() {
        testResults = ImmutableList.of(new TestResult("test1", false, "Not good.",
                "Try harder", true));
        runResult = new RunResult(Status.TESTS_FAILED, testResults, logs);
        printer.printRunResult(runResult, false, false, null, null);
        io.assertContains("Please review your answer before submitting");
    }

    @Test
    public void printRunResultWorksIfTestsFailWithException() {
        ImmutableList<String> points = ImmutableList.of("1");
        ImmutableList<String> exceptions = ImmutableList.of("Some exceptional condition");
        testResults = ImmutableList.of(new TestResult("test1", false, points,
                "Not good.", exceptions));
        runResult = new RunResult(Status.TESTS_FAILED, testResults, logs);
        printer.printRunResult(runResult, false, false, null, null);
        io.assertContains("Please review your answer before submitting");
    }

    @Test
    public void printRunResultWorksIfCompilationFail() {
        testResults = ImmutableList.of();
        runResult = new RunResult(Status.COMPILE_FAILED, testResults, logs);
        printer.printRunResult(runResult, false, false, null, null);
        io.assertContains("Failed to compile project");
    }

}
