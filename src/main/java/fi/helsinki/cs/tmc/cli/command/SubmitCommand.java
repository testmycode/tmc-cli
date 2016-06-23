package fi.helsinki.cs.tmc.cli.command;

import static fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.TestResultStatus.NONE_FAILED;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ResultPrinter;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExerciseUpdater;
import fi.helsinki.cs.tmc.cli.tmcstuff.FeedbackHandler;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Command(name = "submit", desc = "Submit exercises")
public class SubmitCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCommand.class);

    private Io io;
    private boolean showAll;
    private boolean showDetails;

    @Override
    public void getOptions(Options options) {
        options.addOption("a", "all", false, "Show all test results");
        options.addOption("d", "details", false, "Show detailed error message");
        options.addOption("c", "completed", false,
                "Filter out exercises that haven't been locally tested");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;

        String[] exercisesFromArgs = parseArgs(args);
        if (exercisesFromArgs == null) {
            return;
        }

        TmcCore core = getApp().getTmcCore();
        if (core == null) {
            return;
        }

        Application app = getApp();
        WorkDir workDir = app.getWorkDir();
        for (String exercise : exercisesFromArgs) {
            if (!workDir.addPath(exercise)) {
                io.println("Error: " + exercise + " is not a valid exercise.");
                return;
            }
        }

        List<String> exerciseNames;
        if (args.hasOption("c")) {
            workDir.addPath(workDir.getCourseDirectory());
            exerciseNames = workDir.getExerciseNames(true, true, false);
        } else {
            exerciseNames = workDir.getExerciseNames();
        }

        if (exerciseNames.isEmpty()) {
            if (args.hasOption("c") && workDir.getCourseDirectory() != null) {
                io.println("No locally tested exercises.");
                return;
            }
            io.println("No exercises specified.");
            return;
        }

        CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
        Course currentCourse = info.getCourse();
        if (currentCourse == null) {
            return;
        }

        ResultPrinter resultPrinter = new ResultPrinter(io, this.showDetails, this.showAll);
        int passed = 0;
        int total = 0;
        Boolean isOnlyExercise = exerciseNames.size() == 1;

        Color.AnsiColor color1 = app.getColor("testresults-left");
        Color.AnsiColor color2 = app.getColor("testresults-right");

        List<Exercise> submitExercises = info.getExercises(exerciseNames);
        List<List<FeedbackQuestion>> feedbackLists
                = new ArrayList<List<FeedbackQuestion>>();
        List<String> exercisesWithFeedback = new ArrayList<String>();
        List<URI> feedbackUris = new ArrayList<URI>();

        for (Exercise exercise : submitExercises) {
            io.println(Color.colorString("Submitting: " + exercise.getName(),
                    Color.AnsiColor.ANSI_YELLOW));
            SubmissionResult result = TmcUtil.submitExercise(core, exercise);
            if (result == null) {
                io.println("Submission failed.");
            } else {
                resultPrinter.printSubmissionResult(result, isOnlyExercise, color1, color2);
                total += result.getTestCases().size();
                passed += ResultPrinter.passedTests(result.getTestCases());

                exercise.setAttempted(true);
                if (result.getTestResultStatus() == NONE_FAILED) {
                    if (info.getLocalCompletedExercises().contains(exercise.getName())) {
                        info.getLocalCompletedExercises().remove(exercise.getName());
                    }
                    exercise.setCompleted(true);
                }
                List<FeedbackQuestion> feedback = result.getFeedbackQuestions();
                if (feedback != null && feedback.size() > 0) {
                    feedbackLists.add(feedback);
                    exercisesWithFeedback.add(exercise.getName());
                    feedbackUris.add(URI.create(result.getFeedbackAnswerUrl()));
                }
            }
        }
        CourseInfoIo.save(info, workDir.getConfigFile());
        if (total > 0 && !isOnlyExercise) {
            // Print a progress bar showing how the ratio of passed exercises
            io.println("");
            io.println("Total tests passed: " + passed + "/" + total);
            io.println(TmcCliProgressObserver.getPassedTestsBar(passed, total, color1, color2));
        }

        io.println("Updating " + CourseInfoIo.COURSE_CONFIG);
        updateCourseJson(core, submitExercises, info, workDir.getConfigFile());
        checkForExerciseUpdates(core, currentCourse);
        for (int i = 0; i < exercisesWithFeedback.size(); i++) {
            if (io.readConfirmation(
                    "Send feedback for " + exercisesWithFeedback.get(i) + "?", true)) {
                FeedbackHandler fbh = new FeedbackHandler(io);
                Boolean success = fbh.sendFeedback(
                        core, feedbackLists.get(i), feedbackUris.get(i));
                if (success) {
                    io.println("Feedback sent.");
                } else {
                    io.println("Failed to send feedback.");
                }
            }
        }
    }

    protected void updateCourseJson(TmcCore core, List<Exercise> submittedExercises,
            CourseInfo courseInfo, Path courseInfoFile) {

        Course updatedCourse = TmcUtil.findCourse(core, courseInfo.getCourseName());
        if (updatedCourse == null) {
            io.println("Failed to update config file for course " + courseInfo.getCourseName());
            return;
        }

        for (Exercise submitted : submittedExercises) {
            Exercise updatedEx = TmcUtil.findExercise(updatedCourse, submitted.getName());
            if (updatedEx == null) {
                // Does this reaaally ever happen?
                io.println("Failed to update config file for exercise " + submitted.getName()
                        + ". The exercise doesn't exist in server anymore.");
                continue;
            }
            courseInfo.replaceOldExercise(updatedEx);
        }
        CourseInfoIo.save(courseInfo, courseInfoFile);
    }

    protected void checkForExerciseUpdates(TmcCore core, Course course) {
        ExerciseUpdater exerciseUpdater = new ExerciseUpdater(core, course);
        if (!exerciseUpdater.updatesAvailable()) {
            return;
        }

        int total = 0;
        String msg = "";
        if (exerciseUpdater.newExercisesAvailable()) {
            int count = exerciseUpdater.getNewExercises().size();
            String plural = count > 1 ? "s" : "";
            msg += count + " new exercise" + plural + " available!\n";
            total += count;
        }

        if (exerciseUpdater.updatedExercisesAvailable()) {
            int count = exerciseUpdater.getUpdatedExercises().size();
            String plural = count > 1 ? "s have" : " has";
            msg += count + " exercise" + plural + " been changed on TMC server.\n";
            total += count;
        }
        msg += "Use 'tmc update' to download " + (total > 1 ? "them." : "it.");

        io.println("");
        io.println(Color.colorString(msg, Color.AnsiColor.ANSI_YELLOW));
    }

    private String[] parseArgs(CommandLine args) {
        this.showAll = args.hasOption("a");
        this.showDetails = args.hasOption("d");
        return args.getArgs();
    }
}
