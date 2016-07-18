package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.ResultPrinter;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Command(name = "test", desc = "Run local exercise tests")
public class RunTestsCommand extends AbstractCommand {

    private static final Logger logger
            = LoggerFactory.getLogger(RunTestsCommand.class);

    private boolean showPassed;
    private boolean showDetails;

    @Override
    public void getOptions(Options options) {
        options.addOption("a", "all", false, "Show all test results");
        options.addOption("d", "details", false, "Show detailed error message");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        CliContext ctx = context;
        Io io = ctx.getIo();

        String[] exercisesFromArgs = parseArgs(args);
        if (exercisesFromArgs == null) {
            return;
        }

        if (!ctx.loadBackendWithoutLogin()) {
            return;
        }

        WorkDir workDir = ctx.getWorkDir();
        for (String exercise : exercisesFromArgs) {
            if (!workDir.addPath(exercise)) {
                io.println("Error: " + exercise + " is not a valid exercise.");
                return;
            }
        }

        List<String> exerciseNames = workDir.getExerciseNames();
        if (exerciseNames.isEmpty()) {
            io.println("You have to be in a course directory to run tests");
            return;
        }

        CourseInfo info = ctx.getCourseInfo();

        Color passedColor = ctx.getApp().getColor("testresults-left");
        Color failedColor = ctx.getApp().getColor("testresults-right");
        ResultPrinter resultPrinter = new ResultPrinter(io, showDetails, showPassed,
                passedColor, failedColor);

        boolean isOnlyExercise = (exerciseNames.size() == 1);

        for (String name : exerciseNames) {
            io.println(ColorUtil.colorString("Testing: " + name, Color.YELLOW));
            Exercise exercise = info.getExercise(name);

            RunResult runResult = TmcUtil.runLocalTests(ctx, exercise);
            if (runResult == null) {
                io.println("Failed to run test");
                resultPrinter.addFailedExercise();
                continue;
            }

            ValidationResult valResult = TmcUtil.runCheckStyle(ctx, exercise);
            boolean testsPassed = resultPrinter.printLocalTestResult(
                    runResult, valResult, isOnlyExercise);

            updateCourseInfo(info, exercise, testsPassed);
            io.println("");
        }
        CourseInfoIo.save(info, workDir.getConfigFile());

        if (!isOnlyExercise) {
            resultPrinter.printTotalExerciseResults();
        }
    }

    private String[] parseArgs(CommandLine args) {
        this.showPassed = args.hasOption("a");
        this.showDetails = args.hasOption("d");
        return args.getArgs();
    }

    private void updateCourseInfo(CourseInfo courseInfo, Exercise exercise, boolean testsPassed) {
        exercise.setAttempted(true);

        if (!exercise.isCompleted() && testsPassed) {
            // add exercise to locally tested exercises
            if (!courseInfo.getLocalCompletedExercises().contains(exercise.getName())) {
                courseInfo.getLocalCompletedExercises().add(exercise.getName());
            }
        } else if (courseInfo.getLocalCompletedExercises().contains(exercise.getName())) {
            courseInfo.getLocalCompletedExercises().remove(exercise.getName());
        }
    }
}
