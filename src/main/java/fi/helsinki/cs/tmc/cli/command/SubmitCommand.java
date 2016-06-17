package fi.helsinki.cs.tmc.cli.command;

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
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;

        List<String> exercisesFromArgs = parseArgs(args);
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
                io.println("Error: '" + exercise + "' is not a valid exercise.");
                return;
            }
        }

        List<String> exerciseNames = workDir.getExerciseNames();
        if (exerciseNames.isEmpty()) {
            io.println("You have to be in a course directory to"
                    + " submit");
            return;
        }

        CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
        String courseName = info.getCourseName();
        Course course = TmcUtil.findCourse(core, courseName);

        if (course == null) {
            io.println("Could not fetch course info from server.");
            return;
        }

        ResultPrinter resultPrinter = new ResultPrinter(io, this.showDetails, this.showAll);
        int passed = 0;
        int total = 0;
        Boolean isOnlyExercise = exerciseNames.size() == 1;

        for (String exerciseName : exerciseNames) {
            io.println(Color.colorString("Submitting: " + exerciseName,
                    Color.AnsiColor.ANSI_YELLOW));
            SubmissionResult result = TmcUtil.submitExercise(core, course, exerciseName);
            if (result == null) {
                io.println("Submission failed.");
            } else {
                resultPrinter.printSubmissionResult(result, isOnlyExercise);
                total += result.getTestCases().size();
                passed += ResultPrinter.passedTests(result.getTestCases());
            }
        }
        if (total > 0 && !isOnlyExercise) {
            // Print a progress bar showing how the ratio of passed exercises
            io.println("");
            io.println("Total tests passed: " + passed + "/" + total);
            io.println(TmcCliProgressObserver.getPassedTestsBar(passed, total));
        }
        checkForExerciseUpdates(core, course);
    }

    public void checkForExerciseUpdates(TmcCore core, Course course) {
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

    private List<String> parseArgs(CommandLine args) {
        this.showAll = args.hasOption("a");
        this.showDetails = args.hasOption("d");
        return args.getArgList();
    }
}
