package fi.helsinki.cs.tmc.cli.shared;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.TestResultStatus;
import fi.helsinki.cs.tmc.core.domain.submission.ValidationErrorImpl;
import fi.helsinki.cs.tmc.core.domain.submission.ValidationResultImpl;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultPrinterTest {

    private TestIo io;
    private ResultPrinter printer;
    private SubmissionResult mockSubResult;
    private RunResult runResult;
    private ImmutableList<TestResult> testResults;
    private ImmutableMap<String, byte[]> logs;
    private ImmutableList<ValidationError> validationErrors;

    @Before
    public void setUp() {
        io = new TestIo();
        printer = new ResultPrinter(io, true, true,
                Color.GREEN, Color.RED);
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

    @Test
    public void printValidationErrorsInLocalTests() {
        testResults = ImmutableList.of(new TestResult("test1", true, "Cool!"));
        runResult = new RunResult(Status.PASSED, testResults, logs);

        ValidationErrorImpl error = new ValidationErrorImpl();
        ValidationResultImpl valResult = new ValidationResultImpl();
        error.setMessage("validation error");
        validationErrors = ImmutableList.of((ValidationError) error);
        File file = new File("");
        Map<File, List<ValidationError>> map = new HashMap<>();
        map.put(file, validationErrors);
        valResult.setValidationErrors(map);

        printer.printLocalTestResult(runResult, valResult, false);
        io.assertContains("validation error");
    }

    @Test
    public void printValidationErrorsInSubmit() {
        ValidationErrorImpl error = new ValidationErrorImpl();
        error.setMessage("Incorrect indentation");

        File file = new File("Test.java");
        Map<File, List<ValidationError>> valErrors = new HashMap<>();
        valErrors.put(file, ImmutableList.of((ValidationError) error));

        ValidationResultImpl valResult = new ValidationResultImpl();
        valResult.setStrategy(Strategy.FAIL);
        valResult.setValidationErrors(valErrors);

        SubmissionResult subResult = new SubmissionResult();
        subResult.setStatus(SubmissionResult.Status.FAIL);
        subResult.setValidationResult(valResult);

        printer.printSubmissionResult(subResult, false);
        io.assertContains("Validation error:");
        io.assertContains("Incorrect indentation");
    }
}
