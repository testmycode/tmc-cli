package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.domain.TestCase;

import java.nio.file.Path;

public class SubmitCommand implements Command {

    private final Io io;
    private Application app;

    public SubmitCommand(Application app) {
        this.app = app;
        this.io = new TerminalIo(); // should be injected?
    }

    @Override
    public String getDescription() {
        return "Submit exercises";
    }

    @Override
    public String getName() {
        return "submit";
    }

    @Override
    public void run(String[] args) {
        TmcCore core;
        Course course;
        SubmissionResult result;
        DirectoryUtil dirUtil;

        dirUtil = new DirectoryUtil();
        core = this.app.getTmcCore();
        Path dir = dirUtil.getCourseDirectory();

        if (dir == null) {
            io.println("You are not in course directory");
            return;
        }
        course = TmcUtil.findCourse(core, dir.getName(dir.getNameCount() - 1).toString());
        String exerciseName = dirUtil.getExerciseName();

        try {
            result = core.submit(ProgressObserver.NULL_OBSERVER,
                    TmcUtil.findExercise(course, exerciseName)).call();

        } catch (Exception e) {
            return;
        }

        printResult(result);
    }

    private void printResult(SubmissionResult result) {
        printEverything(result);
    }

    private void printEverything(SubmissionResult result) {
        io.println("!Course: " + result.getCourse());
        io.println("!Error: " + result.getError());
        io.println("!Exercise name: " + result.getExerciseName());
        io.println("!Feedback Answer URL: " + result.getFeedbackAnswerUrl());
        io.println("!Message for paste: " + result.getMessageForPaste());
        io.println("!Solution URL: " + result.getSolutionUrl());
        io.println("!Submitted at: " + result.getSubmittedAt());
        io.println("!Valgrind " + result.getValgrind());
        io.println("!Api version: " + result.getApiVersion());
        io.println("!Processing time: " + result.getProcessingTime());
        io.println("!Status: " + result.getStatus());
        io.println("!Test result status: " + result.getTestResultStatus());
        io.println("!User id: " + result.getUserId());
        io.println("!Velidation result: " + result.getValidationResult());
        io.println("!Validations: " + result.getValidations());
        io.println("!All tests passed: " + result.isAllTestsPassed());
        io.println("!Requests review: " + result.isRequestsReview());
        io.println("!Is reviewd: " + result.isReviewed());
        io.println("!Validations failed: " + result.validationsFailed());
        io.println("");

        io.println("!FeedbackQuestions ->:");
        for (FeedbackQuestion question : result.getFeedbackQuestions()) {
            io.println("\t!Kind: " + question.getKind());
            io.println("\t!Question: " + question.getQuestion());
            io.println("\t!Id: " + question.getId());
            io.println("\t!Range max: " + question.getIntRangeMax());
            io.println("\t!Range min: " + question.getIntRangeMin());
            io.println("\t!Is int range: " + question.isIntRange());
            io.println("\t!Is text: " + question.isText());
            io.println("\t!toString: " + question.toString());
            io.println("\t");
        }

        io.println("!Missing review points ->:");
        for (String missinReviewPoint : result.getMissingReviewPoints()) {
            io.println("\t!: " + missinReviewPoint);
            io.println("");
        }

        io.println("!Points ->:");
        for (String point : result.getPoints()) {
            io.println("\t!: " + point);
            io.println("");
        }

        io.println("!Test cases ->:");
        for (TestCase testCase : result.getTestCases()) {
            io.println("\t!Class name: " + testCase.className);
            io.println("\t!Message: " + testCase.message);
            io.println("\t!Method name: " + testCase.methodName);
            io.println("\t!Exception: " + testCase.exception);
            io.println("\t!Status: " + testCase.status);

            io.println("\tPoint names ->:");
            if (testCase.pointNames != null) {
                for (String pointName : testCase.pointNames) {
                    io.println("\t\t!:" + pointName);
                }
            } else {
                io.println("\t\t!: null array");
            }
            io.println("\t!toString: " + testCase.toString());
            io.println("");
        }

        io.println("\n!toString: " + result);
    }
}
