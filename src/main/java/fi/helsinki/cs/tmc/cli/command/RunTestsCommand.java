package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ResultPrinter;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Command(name = "run-tests", desc = "Run local exercise tests")
public class RunTestsCommand implements CommandInterface {

    private static final Logger logger
            = LoggerFactory.getLogger(RunTestsCommand.class);

    private final Application app;
    private final Options options;

    private Io io;
    private Course course;
    private boolean showPassed;
    private boolean showDetails;

    public RunTestsCommand(Application app) {
        this.app = app;
        this.options = new Options();

        options.addOption("a", "all", false, "Show all test results");
        options.addOption("d", "details", false, "Show detailed error message");
    }

    @Override
    public void run(String[] args, Io io) {
        this.io = io;

        String[] exercisesFromArgs = parseArgs(args);
        if (exercisesFromArgs == null) {
            return;
        }

        DirectoryUtil dirUtil = new DirectoryUtil();
        String courseName = getCourseName(dirUtil);
        List<String> exerciseNames = dirUtil.getExerciseNames(exercisesFromArgs);

        TmcCore core = app.getTmcCore();
        if (core == null) {
            return;
        }
        course = TmcUtil.findCourse(core, courseName);

        ResultPrinter resultPrinter
                = new ResultPrinter(io, this.showDetails, this.showPassed);
        RunResult runResult;

        try {
            for (String name : exerciseNames) {
                io.println(Color.colorString("Testing: " + name, Color.ANSI_YELLOW));
                name = name.replace("-", File.separator);
                Exercise exercise = new Exercise(name, courseName);

                runResult = core.runTests(new TmcCliProgressObserver(), exercise).call();
                resultPrinter.printRunResult(runResult);
                io.println("");
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

    private String[] parseArgs(String[] args) {
        GnuParser parser = new GnuParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            io.println("Invalid command line arguments.");
            io.println(e.getMessage());
            return null;
        }
        this.showPassed = line.hasOption("a");
        this.showDetails = line.hasOption("d");
        return line.getArgs();
    }
}
