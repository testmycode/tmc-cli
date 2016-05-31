package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class RunTestsCommand implements Command {

    private static final Logger logger
            = LoggerFactory.getLogger(RunTestsCommand.class);

    private final Application app;
    private Io io;

    public RunTestsCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "Run local exercise tests";
    }

    @Override
    public String getName() {
        return "run-tests";
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core = app.getTmcCore();
        if (core == null) {
            return;
        }

        this.io = io;
        DirectoryUtil dirUtil = new DirectoryUtil();
        Path courseDir = dirUtil.getCourseDirectory();
        String courseName = courseDir.getName(courseDir.getNameCount() - 1).toString();
        String exerciseName = dirUtil.getExerciseName();

        Exercise exercise = new Exercise(exerciseName, courseName);
        RunResult runResult;

        io.println("Running tests...");

        try {
            runResult = core.runTests(new TmcCliProgressObserver(), exercise).call();
        } catch (Exception ex) {
            io.println("Failed to run tests. Please make sure you are in"
                    + " exercise directory.");
            logger.error("Failed to run tests.", ex);
            return;
        }

        printRunResult(runResult);
    }

    private void printRunResult(RunResult runResult) {
        for (TestResult testResult : runResult.testResults) {
            if (testResult.passed) {
                io.println(colorString("Passed: " + testResult.name, ANSI_GREEN));
            } else {
                io.println(colorString("Failed: " + testResult.name
                        + "\n\t" + testResult.errorMessage, ANSI_RED));
            }
        }

        if (runResult.status == RunResult.Status.PASSED) {
            io.println("All tests passed! Submit to server with 'tmc submit'.");
        } else if (runResult.status == RunResult.Status.TESTS_FAILED) {
            io.println("Some tests did not pass, please review your "
                    + "answer before submitting.");
        }
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String colorString(String string, String color) {
        return color + string + ANSI_RESET;
    }
}
