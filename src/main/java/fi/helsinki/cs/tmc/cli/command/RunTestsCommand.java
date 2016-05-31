package fi.helsinki.cs.tmc.cli.command;

import static fi.helsinki.cs.tmc.cli.io.Color.ANSI_GREEN;
import static fi.helsinki.cs.tmc.cli.io.Color.ANSI_RED;
import static fi.helsinki.cs.tmc.cli.io.Color.colorString;

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
        this.io = io;
        DirectoryUtil dirUtil = new DirectoryUtil();
        String courseName = getCourseName(dirUtil);
        String exerciseName = dirUtil.getExerciseName();

        io.println("Running tests...");
        RunResult runResult;
        try {
            TmcCore core = app.getTmcCore();
            Exercise exercise = new Exercise(exerciseName, courseName);
            runResult = core.runTests(new TmcCliProgressObserver(), exercise).call();
        } catch (Exception ex) {
            io.println("Failed to run tests. Please make sure you are in"
                    + " course and exercise directory.");
            logger.error("Failed to run tests.", ex);
            return;
        }

        printRunResult(runResult);
    }

    private String getCourseName(DirectoryUtil dirUtil) {
        Path courseDir = dirUtil.getCourseDirectory();
        try {
            return courseDir.getName(courseDir.getNameCount() - 1).toString();
        } catch (Exception e) {
        }
        return null;
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

}
