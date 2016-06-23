package fi.helsinki.cs.tmc.cli.io;

import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.util.Arrays;
import java.util.List;

public class ResultPrinter {

    private static final String COMPILE_ERROR_MESSAGE
            = Color.colorString("Failed to compile project", Color.AnsiColor.ANSI_PURPLE);
    private static final String FAIL_MESSAGE = "Failed: ";
    private static final String PASS_MESSAGE = "Passed: ";
    private final String tab;

    private final Io io;

    private boolean showDetails;
    private boolean showPassed;
    private int passed;
    private int total;

    public ResultPrinter(Io io, boolean showDetails, boolean showPassed) {
        this.io = io;
        this.showDetails = showDetails;
        this.showPassed = showPassed;

        this.tab = createPaddingString(PASS_MESSAGE.length());
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public boolean isShowPassed() {
        return showPassed;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    public void setShowPassed(boolean showPassed) {
        this.showPassed = showPassed;
    }

    public void printSubmissionResult(SubmissionResult result, Boolean printProgressBar,
                                      Color.AnsiColor color1, Color.AnsiColor color2) {
        if (result == null) {
            return;
        }

        this.total = result.getTestCases().size();
        this.passed = passedTests(result.getTestCases());

        printTestResults(result.getTestCases());

        switch (result.getStatus()) {
            case ERROR:
                io.println("");
                io.println(result.getError());
                break;
            case FAIL:
                String valgrind = result.getValgrind();
                if (valgrind != null && !valgrind.isEmpty()) {
                    io.println(Color.colorString("Valgrind error:",
                            Color.AnsiColor.ANSI_RED));
                    io.println(valgrind);
                    return;
                }
                break;
            case PROCESSING:
                io.println("PROCESSING");
                break;
            case OK:
                //io.println("OK");
                break;
            default:
        }

        if (printProgressBar && this.total > 0) {
            io.println(TmcCliProgressObserver.getPassedTestsBar(passed, total, color1, color2));
        }
        String msg = null;
        switch (    result.getTestResultStatus()) {
            case NONE_FAILED:
                msg = "All tests passed on server!";
                msg = Color.colorString(msg, Color.AnsiColor.ANSI_GREEN)
                        + "\nPoints permanently awarded: " + result.getPoints()
                        + "\nModel solution: " + result.getSolutionUrl();
                break;
            case ALL_FAILED:
                msg = Color.colorString("All tests failed on server.", Color.AnsiColor.ANSI_RED)
                        + " Please review your answer";
                break;
            case SOME_FAILED:
                msg = Color.colorString("Some tests failed on server.", Color.AnsiColor.ANSI_RED)
                        + " Please review your answer";
                break;
            default:
        }
        if (msg != null) {
            io.println(msg);
        }
    }

    public void printRunResult(RunResult result, Boolean submitted, Boolean printProgressBar,
                               Color.AnsiColor color1, Color.AnsiColor color2) {
        printTestResults(result.testResults);
        this.total = result.testResults.size();
        this.passed = passedTests(result.testResults);

        if (printProgressBar && this.total > 0) {
            io.println(TmcCliProgressObserver.getPassedTestsBar(passed, total, color1, color2));
        }

        String msg = null;
        switch (result.status) {
            case PASSED:
                msg = Color.colorString("All tests passed!", Color.AnsiColor.ANSI_GREEN);
                if (!submitted) {
                    msg += " Submit to server with 'tmc submit'";
                }
                break;
            case TESTS_FAILED:
                msg = "Please review your answer before submitting";
                break;
            case COMPILE_FAILED:
                msg = ResultPrinter.COMPILE_ERROR_MESSAGE;
                break;
            case TESTRUN_INTERRUPTED:
                msg = "Testrun interrupted";
                break;
            case GENERIC_ERROR:
                msg = new String(result.logs.get(SpecialLogs.GENERIC_ERROR_MESSAGE));
                break;
            default:
        }
        if (msg != null) {
            io.println(msg);
        }
    }

    public static int passedTests(List<TestResult> testResults) {
        int passed = 0;
        for (TestResult testResult : testResults) {
            if (testResult.isSuccessful()) {
                passed++;
            }
        }
        return passed;
    }

    private void printTestResults(List<TestResult> testResults) {
        for (TestResult testResult : testResults) {
            if (!testResult.isSuccessful()) {
                printFailMessage(testResult);
            } else if (showPassed) {
                printPassMessage(testResult);
            }
        }
        io.println("Test results: "
                + passedTests(testResults) + "/"
                + testResults.size() + " tests passed");

    }

    private void printFailMessage(TestResult testResult) {
        io.print(Color.colorString(FAIL_MESSAGE, Color.AnsiColor.ANSI_RED));
        io.println(testResult.getName());
        io.println(this.tab + testResult.getMessage());

        if (showDetails) {
            String details = listToString(testResult.getDetailedMessage());
            if (details != null) {
                io.println("\nDetailed message:");
                io.println(details);
            }

            String exception = listToString(testResult.getException());
            if (exception != null) {
                io.println("\nException:");
                io.println(exception);
            }
        }
    }

    private void printPassMessage(TestResult testResult) {
        io.print(Color.colorString(PASS_MESSAGE, Color.AnsiColor.ANSI_GREEN));
        io.println(testResult.getName());
    }

    private String listToString(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string).append("\n");
        }
        return sb.toString();
    }

    private String createPaddingString(int size) {
        char[] charArray = new char[size];
        Arrays.fill(charArray, ' ');
        return new String(charArray);
    }
}