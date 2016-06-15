package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ResultPrinter;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
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

        List<String> exerciseNames = parseArgs(args);
        if (exerciseNames == null) {
            return;
        }
        TmcCore core = getApp().getTmcCore();
        if (core == null) {
            return;
        }

        WorkDir workDir = getApp().getWorkDir();

        Path courseDir = workDir.getCourseDirectory();

        for (String exercise : exerciseNames) {
            if (!workDir.addPath(exercise)) {
                io.println("Error: '" + exercise + "' is not a valid exercise.");
                return;
            }
        }
        if (courseDir == null) {
            io.println("Not a course directory");
            return;
        }

        CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
        String courseName = info.getCourseName();
        Course course = TmcUtil.findCourse(core, courseName);
        if (course == null) {
            io.println("Could not fetch course info from server.");
            return;
        }

        List<String> exercises = workDir.getExerciseNames();

        // Abort if user gave invalid exercise name as argument.
//        for (String exerciseName : exerciseNames) {
//            if (!exercises.contains(exerciseName)) {
//                io.println("Could not find exercise '" + exerciseName + "'");
//                return;
//            }
//        }

        ResultPrinter resultPrinter = new ResultPrinter(io, this.showDetails, this.showAll);

        for (String exerciseName : exercises) {
            io.println(Color.colorString("Submitting: " + exerciseName, Color.ANSI_YELLOW));
            SubmissionResult result = TmcUtil.submitExercise(core, course, exerciseName);
            if (result == null) {
                io.println("Submission failed.");
            } else {
                resultPrinter.printSubmissionResult(result);
            }
            io.println("");
        }
    }

    private List<String> parseArgs(CommandLine args) {
        this.showAll = args.hasOption("a");
        this.showDetails = args.hasOption("d");
        return args.getArgList();
    }
}
