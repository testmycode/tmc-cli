package fi.helsinki.cs.tmc.cli.command;

import static fi.helsinki.cs.tmc.cli.io.Color.AnsiColor.ANSI_BLUE;
import static fi.helsinki.cs.tmc.cli.io.Color.AnsiColor.ANSI_GREEN;
import static fi.helsinki.cs.tmc.cli.io.Color.AnsiColor.ANSI_RED;

import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.List;

@Command(name = "info", desc = "Show info about the current directory")
public class CourseInfoCommand extends AbstractCommand {
    private Course course;
    private Exercise exercise;
    private CourseInfo info;
    private WorkDir workDir;
    private Io io;
    private CliContext ctx;

    @Override
    public void getOptions(Options options) {
        options.addOption("a", false, "Show all information for a specific course");
        options.addOption("i", false, "Get the information from the server");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        this.ctx = getContext();
        workDir = ctx.getWorkDir();

        boolean fetchFromInternet = args.hasOption("i");

        if (! ctx.loadBackend()) {
            return;
        }

        if (!fetchFromInternet) {
            printLocalCourseOrExercise(args);
            return;
        }

        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0) {
            io.println("You must give a course as an argument.");
            return;
        }

        course = TmcUtil.findCourse(ctx, stringArgs[0]);
        if (course == null) {
            io.println("The course " + stringArgs[0] + " doesn't exist on this server.");
            return;
        }
        printCourse(args.hasOption("a"));
    }

    private void printLocalCourseOrExercise(CommandLine args) {
        info = ctx.getCourseInfo();
        if (info != null) {
            course = info.getCourse();
            printCourseOrExercise(args);
        } else {
            this.io.println("You have to be in a course directory"
                    + " or use the -i option with the course name "
                    + "to get the information from the server.");
        }
    }

    private void printCourseOrExercise(CommandLine args) {
        String[] stringArgs = args.getArgs();

        // if in exercise directory and no parameters given, print info for that exercise.
        if (workDir.getExerciseNames().size() == 1 && stringArgs.length == 0) {
            String currentExercise = workDir.getExerciseNames().get(0);
            exercise = info.getExercise(currentExercise);
            printOneExercise(args.hasOption("a"));
            return;
        }

        if (stringArgs.length != 0) {
            printCourseOrExerciseFromParameters(args);
            return;
        }
        printCourse(args.hasOption("a"));
    }

    private void printCourseOrExerciseFromParameters(CommandLine args) {
        String[] stringArgs = args.getArgs();
        // if parameter is given, check if it is an exercise or a course. If neither, print an error message.
        if (info.getExercise(stringArgs[0]) != null) {
            exercise = info.getExercise(stringArgs[0]);
            printOneExercise(args.hasOption("a"));
            return;

        }
        course = info.getCourse();
        if (course != null && course.getName().equals(stringArgs[0])) {
            printCourse(args.hasOption("a"));
        } else {
            this.io.println("Wrong course directory."
                    + " Navigate to the correct course directory or"
                    + " use the -i option with the course name"
                    + " to get the information from the server.");
        }
    }

    private void printCourse(boolean showAll) {
        printCourseShort();
        if (showAll) {
            printCourseDetails();
        }
        printExercises(showAll);
    }
    
    private void printCourseShort() {
        io.println("Course name: " + course.getName());
        io.println("Number of available exercises: " + course.getExercises().size());
        io.println("Number of completed exercises: " + completedExercises());
        io.println("Number of locked exercises: " + course.getUnlockables().size());
    }
    
    private void printCourseDetails() {
        io.println("Unlockables:" + course.getUnlockables().toString());
        io.println("Course id: " + course.getId());
        io.println("Details URL: " + course.getDetailsUrl());
        io.println("Reviews URL: " + course.getReviewsUrl());
        io.println("Statistics URLs:" + course.getSpywareUrls().toString());
        io.println("UnlockUrl: " + course.getUnlockUrl());
        io.println("CometUrl: " + course.getCometUrl());
    }

    private void printOneExercise(boolean showAll) {
        if (showAll) {
            printExercise(exercise);
        } else {
            printExerciseShort();
        }
    }

    private void printExerciseShort() {
        io.println("Exercise: " + exercise.getName());
        io.println("Deadline: " + getDeadline(exercise));
        io.println(formatString("completed", exercise.isCompleted()));
        if (!exercise.isCompleted() && exercise.isAttempted()) {
            io.println(Color.colorString("attempted", ANSI_BLUE));
        }
        if (exercise.requiresReview()) {
            io.println(formatString("reviewed", exercise.isReviewed()));
        }
    }

    private String formatString(String string, boolean color) {
        if (color) {
            return Color.colorString(string, ANSI_GREEN);
        } else {
            return Color.colorString("not " + string, ANSI_RED);
        }
    }

    private void printExercises(boolean showAll) {
        List<Exercise> exercises = course.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            io.println("Exercises: -");
            return;
        }

        io.println("Exercises: ");
        for (Exercise exercise : exercises) {
            if (showAll) {
                printExercise(exercise);
            } else {
                io.println("    " + exercise.getName());
            }
        }
    }
    
    private int completedExercises() {
        int completed = 0;
        for (Exercise exercise : course.getExercises()) {
            if (exercise.isCompleted()) {
                completed++;
            }
        }
        return completed;
    }
    
    private void printExercise(Exercise exercise) {
        io.println("    Exercise name: " + exercise.getName());
        io.println("    Exercise id: " + exercise.getId());
        io.println("    Is locked: " + exercise.isLocked());
        io.println("    Deadline description: " + exercise.getDeadlineDescription());
        io.println("    Deadline: " + exercise.getDeadline());
        io.println("    Deadline date: " + exercise.getDeadlineDate());
        io.println("    Deadline passed: " + exercise.hasDeadlinePassed());
        io.println("    Is returnable: " + exercise.isReturnable());
        io.println("    Review required: " + exercise.requiresReview());
        io.println("    Is attempted: " + exercise.isAttempted());
        io.println("    Is completed: " + exercise.isCompleted());
        io.println("    Is reviewed: " + exercise.isReviewed());
        io.println("    Is all review points given: " + exercise.isAllReviewPointsGiven());
        io.println("    Memory limit: " + exercise.getMemoryLimit());
        io.println("    Runtime parameters: " + Arrays.toString(exercise.getRuntimeParams()));
        io.println("    Is code review request enabled: " + exercise.isCodeReviewRequestsEnabled());
        io.println("    Are local tests enabled: " + exercise.isRunTestsLocallyActionEnabled());
        io.println("    Return URL: " + exercise.getReturnUrl());
        io.println("    Zip URL: " + exercise.getZipUrl());
        io.println("    Exercise submission URL: " + exercise.getExerciseSubmissionsUrl());
        io.println("    Download URL: " + exercise.getDownloadUrl());
        io.println("    Solution download URL: " + exercise.getSolutionDownloadUrl());
        io.println("    Checksum: " + exercise.getChecksum());
        io.println("");
    }

    private String getDeadline(Exercise exercise) {
        String deadline = exercise.getDeadline();
        if (deadline == null) {
            return "not available";
        }
        deadline = deadline.substring(0, 19);
        return deadline.replace("T", " at ");
    }
}
