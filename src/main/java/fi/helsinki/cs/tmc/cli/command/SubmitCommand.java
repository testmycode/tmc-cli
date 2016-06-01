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
import fi.helsinki.cs.tmc.langs.domain.TestResult;

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
        for (TestResult testCase : result.getTestCases()) {
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

    private boolean printTestCase(TestResult testCase) {
        String status = testCase.isSuccessful() ? "Passed: " : "Failed: ";
        String infoMsg = status + testCase.getName();
        if (!testCase.isSuccessful()) {
            infoMsg += "\n        " + testCase.getMessage();
            io.println(Color.colorString(infoMsg, Color.ANSI_RED));
            if (showDetails && testCase.getDetailedMessage() != null) {
                io.println(testCase.getDetailedMessage().toString());
            }
        } else if (showAll) {
            io.println(Color.colorString(infoMsg, Color.ANSI_GREEN));
        }

        return testCase.isSuccessful();
    }
}
