package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
import fi.helsinki.cs.tmc.langs.domain.TestCase.Status;

import java.nio.file.Path;

@Command(name = "submit", desc = "Submit exercises")
public class SubmitCommand implements CommandInterface {

    private Application app;
    private Io io;

    public SubmitCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core;
        SubmissionResult submit;
        DirectoryUtil dirUtil;

        this.io = io;
        dirUtil = new DirectoryUtil();
        core = this.app.getTmcCore();
        Path dir = dirUtil.getCourseDirectory();

        if (dir == null) {
            io.println("You are not in course directory");
            return;
        }
        Course course = TmcUtil.findCourse(core, dir.getName(dir.getNameCount() - 1).toString());
        String exerciseName = dirUtil.getExerciseName();

        io.println("Submitting to server...");
        try {
            submit = core.submit(new TmcCliProgressObserver(),
                    TmcUtil.findExercise(course, exerciseName)).call();

        } catch (Exception e) {
            return;
        }

        printResults(submit);
    }

    private void printResults(SubmissionResult result) {

        int passedTestCases = 0;
        for (TestCase testCase : result.getTestCases()) {
            if (printTestCase(testCase)) {
                passedTestCases++;
            }
        }

        io.println("");

        if (result.isAllTestsPassed()) {
            io.println(Color.colorString("All tests passed on the server!", Color.ANSI_GREEN));
            io.println("Points permanently awarded: " + result.getPoints());
            io.println("Model solution: " + result.getSolutionUrl());
        } else {
            //io.println(Color.colorString("Exercise '" + result.getExerciseName() + "' failed.", Color.ANSI_RED));
            String msg;
            if (passedTestCases == 0) {
                msg = "All tests failed on the server.";
            } else {
                msg = "Some tests failed on the server.";
            }
            io.println(Color.colorString(msg, Color.ANSI_RED));
        }

        System.out.println("\n\n\n\n" + result.toString());
    }

    private boolean printTestCase(TestCase testCase) {
        String className = "MysteryTestClass"; //testCase.className;
        String methodName = "MysteryTestMethod"; //testCase.methodName
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
                io.println(Color.colorString(infoMsg, Color.ANSI_RED));
                io.println(Color.colorString("\t" + testCase.message, Color.ANSI_RED));
                break;
            case PASSED:
                io.println(Color.colorString(infoMsg, Color.ANSI_GREEN));
                break;
            default:
                io.println("wot?");
        }

        return status == Status.PASSED;
    }
}
