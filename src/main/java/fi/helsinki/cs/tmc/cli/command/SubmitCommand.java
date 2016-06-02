package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
import fi.helsinki.cs.tmc.langs.domain.TestCase.Status;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

@Command(name = "submit", desc = "Submit exercises")
public class SubmitCommand implements CommandInterface {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCommand.class);
    private final Options options;

    private Application app;
    private Io io;
    private boolean showAll;
    private boolean showDetails;

    public SubmitCommand(Application app) {
        this.app = app;
        this.options = new Options();

        options.addOption("a", "all", false, "Show all test results");
        options.addOption("d", "details", false, "Show detailed error message");
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core;
        DirectoryUtil dirUtil;

        String[] exerciseNames = parseArgs(args);

        if (exerciseNames == null) {
            return;
        }
        this.io = io;
        dirUtil = new DirectoryUtil();
        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        Path dir = dirUtil.getCourseDirectory();
        Path courseDir = dirUtil.getCourseDirectory();

        if (courseDir == null) {
            System.out.println("Not a course directory");
            return;
        }

        CourseInfoIo infoIo = new CourseInfoIo(dirUtil.getConfigFile());
        CourseInfo info = infoIo.load();
        String courseName = info.getCourse();
        Course course = TmcUtil.findCourse(core, courseName);

        List<String> exercises;
        exercises = dirUtil.getExerciseNames(exerciseNames);
        SubmissionResult result;

        for (String exerciseName : exercises) {
            io.println("Submitting: " + exerciseName);
            result = TmcUtil.submitExercise(core, course, exerciseName);
            printResults(result);
        }
    }

    private String[] parseArgs(String[] args) {
        GnuParser parser = new GnuParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            logger.warn("Unable to parse arguments.", e);
            return null;
        }
        this.showAll = line.hasOption("a");
        this.showDetails = line.hasOption("d");
        return line.getArgs();
    }

    private void printResults(SubmissionResult result) {

        int passedTestCases = 0;
        for (TestCase testCase : result.getTestCases()) {
            if (printTestCase(testCase)) {
                passedTestCases++;
            }
        }
        int failedTestCases = result.getTestCases().size() - passedTestCases;

        if (failedTestCases != 0 || (passedTestCases != 0 && showAll)) {
            io.println("");
        }

        if (result.isAllTestsPassed()) {
            io.println(Color.colorString("All tests passed on the server!", Color.ANSI_GREEN));
            io.println("Points permanently awarded: " + result.getPoints());
            io.println("Model solution: " + result.getSolutionUrl());
        } else {
            //io.println(Color.colorString("Exercise '" + result.getExerciseName() + "' failed.", Color.ANSI_RED));
            String msg;
            if (passedTestCases == 0) {
                io.println(Color.colorString("All tests failed on the server.",
                        Color.ANSI_RED));
            } else {
                io.println(Color.colorString(passedTestCases
                        + " tests passed on the server.", Color.ANSI_GREEN));
                io.println(Color.colorString(failedTestCases
                        + " tests failed on the server.", Color.ANSI_RED));
            }

        }
    }

    // We get a broken TestCase obj atm so there's lots of weird stuff here...
    private boolean printTestCase(TestCase testCase) {
        String className = "TestClassName"; //testCase.className;
        String methodName = "TestMethodName"; //testCase.methodName
        Status status; //testCase.status;

        // TEMP: Until JSON -> TestCase parsing is fixed, non empty message
        // indicates the test is failed. Empty message means it passed.
        if (testCase.message == null || testCase.message.equals("")) {
            status = Status.PASSED;
        } else {
            status = Status.FAILED;
        }

        String infoMsg = status.name() + ": " + className + " " + methodName;
        switch (status) {
            case FAILED:
                infoMsg += "\n        " + testCase.message;
                io.println(Color.colorString(infoMsg, Color.ANSI_RED));
                if (showDetails && testCase.exception != null) {
                    io.println(testCase.exception.toString());
                }
                break;
            case PASSED:
                if (showAll) {
                    io.println(Color.colorString(infoMsg, Color.ANSI_GREEN));
                }
                break;
            default:
                io.println("wot?");
        }

        return status == Status.PASSED;
    }
}
