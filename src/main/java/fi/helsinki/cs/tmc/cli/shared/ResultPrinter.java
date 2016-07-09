package fi.helsinki.cs.tmc.cli.shared;

import fi.helsinki.cs.tmc.cli.io.CliProgressObserver;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResultPrinter {

    private static final String COMPILE_ERROR_MESSAGE
            = Color.colorString("Failed to compile project", Color.AnsiColor.ANSI_PURPLE);
    private static final String FAIL_MESSAGE = "Failed: ";
    private static final String PASS_MESSAGE = "Passed: ";
    private static final String PADDING = createPaddingString(PASS_MESSAGE.length());

    private final Io io;
    private final Color.AnsiColor passedColor;
    private final Color.AnsiColor failedColor;

    private boolean showDetails;
    private boolean showPassed;
    private int totalExercises;
    private int passedExercises;

    public ResultPrinter(Io io, boolean showDetails, boolean showPassed,
            Color.AnsiColor passedColor, Color.AnsiColor failedColor) {
        this.io = io;
        this.passedColor = passedColor;
        this.failedColor = failedColor;

        this.showDetails = showDetails;
        this.showPassed = showPassed;
        this.totalExercises = 0;
        this.passedExercises = 0;
    }

    public boolean showDetails() {
        return showDetails;
    }

    public boolean showPassed() {
        return showPassed;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    public void setShowPassed(boolean showPassed) {
        this.showPassed = showPassed;
    }

    public void addFailedExercise() {
        totalExercises++;
    }

    public boolean printSubmissionResult(SubmissionResult submResult, boolean printResultBar) {
        if (submResult == null) {
            return false;
        }
        totalExercises++;

        switch (submResult.getStatus()) {
            case OK:
                printPassedSubmissionResult(submResult, printResultBar);
                return true;

            case FAIL:
                printFailedSubmissionResult(submResult, printResultBar);
                return false;

            case ERROR:
                io.println(submResult.getError());
                return false;

            case PROCESSING:
                io.println("Processing");
                return false;

            default:
                return false;
        }
    }

    public boolean printLocalTestResult(RunResult runResult, ValidationResult valResult,
            boolean printResultBar) {
        if (runResult == null) {
            return false;
        }

        totalExercises++;

        switch (runResult.status) {
            case PASSED: // fall through
            case TESTS_FAILED:
                printTestCases(runResult.testResults);
                int passedTests = passedTests(runResult.testResults);
                int totalTests = runResult.testResults.size();

                boolean validationsPassed = validationsPassed(valResult);
                if (!validationsPassed) {
                    printValidationErrors(valResult);
                    totalTests++;
                }

                io.println("Test results: " + passedTests + "/" + totalTests + " tests passed");
                if (printResultBar) {
                    printResultBar(passedTests, totalTests);
                }

                if (runResult.status == RunResult.Status.PASSED && validationsPassed) {
                    io.print(Color.colorString("All tests passed!", Color.AnsiColor.ANSI_GREEN));
                    io.println(" Submit to server with 'tmc submit'");
                    passedExercises++;
                    return true;
                }
                return false;

            case COMPILE_FAILED:
                io.println(COMPILE_ERROR_MESSAGE);
                return false;

            case GENERIC_ERROR:
                byte[] log = runResult.logs.get(SpecialLogs.GENERIC_ERROR_MESSAGE);
                if (log == null) {
                    log = new byte[0];
                }
                io.println(new String(log));
                return false;

            case TESTRUN_INTERRUPTED:
                io.println("Testrun interrupted");
                return false;

            default:
                return false;
        }
    }

    public void printTotalExerciseResults() {
        if (totalExercises == 0) {
            return;
        }
        io.println("Total results: "
                + passedExercises + "/" + totalExercises
                + " exercises passed");
        printResultBar(passedExercises, totalExercises);
    }

    private void printResultBar(int passed, int total) {
        if (total == 0) {
            return;
        }
        io.println(
                CliProgressObserver.getPassedTestsBar(passed, total, passedColor, failedColor)
        );
    }

    private boolean validationsPassed(ValidationResult result) {
        if (result == null || result.getStrategy() == Strategy.DISABLED) {
            return true;
        }
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        return errors == null || errors.isEmpty();
    }

    private void printValidationErrors(ValidationResult result) {
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        io.println(Color.colorString("Validation error:", failedColor));

        for (Map.Entry<File, List<ValidationError>> entry : errors.entrySet()) {
            io.println("File: " + entry.getKey());

            for (ValidationError error : entry.getValue()) {
                io.println("  Line " + error.getLine() + ": " + error.getMessage());
            }
            io.println("");
        }
    }

    private void printTestCases(List<TestResult> testResults) {
        for (TestResult testResult : testResults) {
            if (!testResult.isSuccessful()) {
                printFailedTest(testResult);
                io.println("");
            } else if (showPassed) {
                printPassedTest(testResult);
                io.println("");
            }
        }
    }

    private void printFailedTest(TestResult testResult) {
        io.print(Color.colorString(FAIL_MESSAGE, failedColor));
        io.println(testResult.getName());
        io.println(PADDING + testResult.getMessage());

        if (showDetails) {
            String details = joinStrings(testResult.getDetailedMessage(), "\n");
            if (details != null) {
                io.println("");
                io.println("Detailed message:");
                io.println(details);
            }

            String exception = joinStrings(testResult.getException(), "\n");
            if (exception != null) {
                io.println("");
                io.println("Exception:");
                io.println(exception);
            }
        }
    }

    private void printPassedTest(TestResult testResult) {
        io.print(Color.colorString(PASS_MESSAGE, passedColor));
        io.println(testResult.getName());
    }

    private void printPassedSubmissionResult(SubmissionResult submResult, boolean printResultBar) {
        printTestCases(submResult.getTestCases());
        int passedTests = passedTests(submResult.getTestCases());
        int totalTests = submResult.getTestCases().size();

        io.println("Test results: " + passedTests + "/" + totalTests + " tests passed");
        if (printResultBar) {
            printResultBar(passedTests, totalTests);
        }

        io.println(Color.colorString("All tests passed on server!", passedColor));
        passedExercises++;

        if (!submResult.getPoints().isEmpty()) {
            io.println("Points permanently awarded: " + submResult.getPoints());
        }
        if (submResult.getSolutionUrl() != null && !submResult.getSolutionUrl().isEmpty()) {
            io.println("Model solution: " + submResult.getSolutionUrl());
        }
    }

    private void printFailedSubmissionResult(SubmissionResult submResult, boolean printResultBar) {
        printTestCases(submResult.getTestCases());
        int passedTests = passedTests(submResult.getTestCases());
        int totalTests = submResult.getTestCases().size();

        String valgrind = submResult.getValgrind();
        if (valgrind != null && !valgrind.isEmpty()) {
            io.println(Color.colorString("Valgrind error:", failedColor));
            io.println(valgrind);
            totalTests++;
        }

        if (submResult.validationsFailed()) {
            printValidationErrors(submResult.getValidationResult());
            totalTests++;
        }

        io.println("Test results: " + passedTests + "/" + totalTests + " tests passed");
        if (printResultBar) {
            printResultBar(passedTests, totalTests);
        }
    }

    private int passedTests(List<TestResult> testResults) {
        int passed = 0;
        for (TestResult testResult : testResults) {
            if (testResult.isSuccessful()) {
                passed++;
            }
        }
        return passed;
    }

    private String joinStrings(List<String> strings, String delimiter) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string).append(delimiter);
        }

        if (sb.length() > 0) {
            // remove last delimiter
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    private static String createPaddingString(int size) {
        char[] charArray = new char[size];
        Arrays.fill(charArray, ' ');
        return new String(charArray);
    }
}
