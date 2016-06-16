package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ResultPrinter;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

@Command(name = "test", desc = "Run local exercise tests")
public class RunTestsCommand extends AbstractCommand {

    private static final Logger logger
            = LoggerFactory.getLogger(RunTestsCommand.class);

    private Io io;
    private boolean showPassed;
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

        Application app = getApp();
        WorkDir workDir = app.getWorkDir();
        for (String exercise : exercisesFromArgs) {
            if (!workDir.addPath(exercise)) {
                io.println("Error: " + exercise + " is not a valid exercise.");
                return;
            }
        }
        String courseName = getCourseName(workDir);
        List<String> exerciseNames = workDir.getExerciseNames();

        if (exerciseNames.isEmpty()) {
            io.println("You have to be in a course directory to"
                    + " run tests");
            return;
        }

        // Local tests don't require login so make sure tmcCore is never null.
        app.createTmcCore(new Settings());

        TmcCore core = app.getTmcCore();
        if (core == null) {
            return;
        }

        ResultPrinter resultPrinter
                = new ResultPrinter(io, this.showDetails, this.showPassed);
        RunResult runResult;

        try {
            for (String name : exerciseNames) {
                io.println(Color.colorString("Testing: " + name, Color.AnsiColor.ANSI_YELLOW));
                //name = name.replace("-", File.separator);
                Exercise exercise = new Exercise(name, courseName);

                runResult = core.runTests(new TmcCliProgressObserver(), exercise).call();
                resultPrinter.printRunResult(runResult);
                io.println("");
            }

        } catch (Exception ex) {
            io.println("Failed to run tests.\n"
                    + ex.getMessage());
            logger.error("Failed to run tests.", ex);
        }
    }

    private String getCourseName(WorkDir dirUtil) {
        Path courseDir = dirUtil.getCourseDirectory();
        try {
            return courseDir.getName(courseDir.getNameCount() - 1).toString();
        } catch (Exception e) {
        }
        return null;
    }

    private List<String> parseArgs(CommandLine args) {
        this.showPassed = args.hasOption("a");
        this.showDetails = args.hasOption("d");
        return args.getArgList();
    }
}
