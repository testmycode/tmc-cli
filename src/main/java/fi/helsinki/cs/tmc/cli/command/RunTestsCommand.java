package fi.helsinki.cs.tmc.cli.command;

import static fi.helsinki.cs.tmc.cli.io.Color.ANSI_GREEN;
import static fi.helsinki.cs.tmc.cli.io.Color.ANSI_RED;
import static fi.helsinki.cs.tmc.cli.io.Color.colorString;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Command(name = "run-tests", desc = "Run local exercise tests.")
public class RunTestsCommand implements CommandInterface {

    private static final Logger logger
            = LoggerFactory.getLogger(RunTestsCommand.class);

    private final Application app;
    private Io io;
    private Course course;

    public RunTestsCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        this.io = io;
        DirectoryUtil dirUtil = new DirectoryUtil();
        String courseName = getCourseName(dirUtil);
        List<String> exerciseNames = dirUtil.getExerciseNames(args);

        TmcCore core = app.getTmcCore();
        course = TmcUtil.findCourse(core, courseName);

        io.println("Running tests...");
        RunResult runResult;

        try {
            for (String name : exerciseNames) {
                io.println("Testing: " + name);
                name = name.replace("-", File.separator);
                Exercise exercise = new Exercise(name, courseName);

                runResult = core.runTests(new TmcCliProgressObserver(), exercise).call();
                printRunResult(runResult);
            }

        } catch (Exception ex) {
            io.println("Failed to run tests. Please make sure you are in"
                    + " course and exercise directory.");
            logger.error("Failed to run tests.", ex);
        }
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
