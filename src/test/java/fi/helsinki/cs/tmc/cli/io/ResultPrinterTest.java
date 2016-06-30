package fi.helsinki.cs.tmc.cli.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.TestResultStatus;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

public class ResultPrinterTest {

    private TestIo io;
    private ResultPrinter printer;
    private SubmissionResult mockSubResult;
    private RunResult runResult;
    private ValidationResult valResult;
    private ImmutableList<TestResult> testResults;
    private ImmutableMap<String, byte[]> logs;

    @Before
    public void setUp() {
        io = new TestIo();
        printer = new ResultPrinter(io, true, true,
                Color.AnsiColor.ANSI_GREEN, Color.AnsiColor.ANSI_RED);
        mockSubResult = mock(SubmissionResult.class);
        logs = ImmutableMap.of();
    }

    @Test
    public void printSubmissionResultWorksIfResultIsNull() {
        printer.printSubmissionResult(null, false);
        io.assertEquals("");
    }

    @Test
    public void printSubmissionResultWorksIfAllTestsPass() {
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.OK);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.NONE_FAILED);
        printer.printSubmissionResult(mockSubResult, false);
        io.assertContains("All tests passed on server!");
    }

    @Test
    public void printSubmissionResultWorksIfAllTestsFail() {
        testResults = ImmutableList.of(new TestResult("test1", false, "Not good."));
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.FAIL);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.ALL_FAILED);
        when(mockSubResult.getTestCases()).thenReturn(testResults);
        printer.printSubmissionResult(mockSubResult, false);
        io.assertContains("Failed:");
        io.assertContains("test1");
        io.assertContains("Not good.");
        io.assertContains("Test results: 0/1 tests passed");
    }

    @Test
    public void printSubmissionResultWorksIfSomeTestsFail() {
        testResults = ImmutableList.of(
                new TestResult("test1", false, "Not good."),
                new TestResult("test2", true, "Was good."));
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.FAIL);
        when(mockSubResult.getTestResultStatus()).thenReturn(TestResultStatus.SOME_FAILED);
        when(mockSubResult.getTestCases()).thenReturn(testResults);
        printer.printSubmissionResult(mockSubResult, false);
        io.assertContains("Failed:");
        io.assertContains("test1");
        io.assertContains("Not good.");
        io.assertContains("Passed:");
        io.assertContains("test2");
        io.assertContains("Test results: 1/2 tests passed");
    }

    @Test
    public void printSubmissionResultWorksIfTestsPassButValgrindFails() {
        testResults = ImmutableList.of(new TestResult("test1", true, "Was good."));
        when(mockSubResult.getStatus()).thenReturn(SubmissionResult.Status.FAIL);
        when(mockSubResult.getValgrind()).thenReturn("This is a valgrind error");
        when(mockSubResult.getTestCases()).thenReturn(testResults);
        printer.printSubmissionResult(mockSubResult, false);
        io.assertContains("Valgrind error:");
        io.assertContains("This is a valgrind error");
        io.assertContains("Test results: 1/2 tests passed");
    }

    @Test
    public void printLocalTestResultWorksIfTestsPass() {
        testResults = ImmutableList.of(new TestResult("test1", true, "Cool!"));
        runResult = new RunResult(Status.PASSED, testResults, logs);
        printer.printLocalTestResult(runResult, null, false);
        io.assertContains("All tests passed!");
    }

    @Test
    public void printLocalTestResultWorksIfTestsFail() {
        testResults = ImmutableList.of(new TestResult("test1", false, "Not good.",
                "Try harder", true));
        runResult = new RunResult(Status.TESTS_FAILED, testResults, logs);
        printer.printLocalTestResult(runResult, null, false);
        io.assertContains("Failed:");
        io.assertContains("test1");
        io.assertContains("Not good.");
    }

    @Test
    public void printLocalTestResultWorksIfTestsFailWithException() {
        ImmutableList<String> points = ImmutableList.of("1");
        ImmutableList<String> exceptions = ImmutableList.of("Some exceptional condition");
        testResults = ImmutableList.of(new TestResult("test1", false, points,
                "Not good.", exceptions));
        runResult = new RunResult(Status.TESTS_FAILED, testResults, logs);
        printer.printLocalTestResult(runResult, null, false);
        io.assertContains("Failed:");
        io.assertContains("test1");
    }

    @Test
    public void printLocalTestResultIfCompilationFail() {
        testResults = ImmutableList.of();
        runResult = new RunResult(Status.COMPILE_FAILED, testResults, logs);
        printer.printLocalTestResult(runResult, null, false);
        io.assertContains("Failed to compile project");
    }

}
