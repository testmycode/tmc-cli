package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.CourseFinder;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.List;

@Command(name = "info", desc = "Show info about the current directory")
public class InfoCommand extends AbstractCommand {

    private CourseInfo info;
    private WorkDir workDir;
    private Io io;
    private CliContext ctx;
    private String courseName;

    private boolean useWorkingDirectory;
    private boolean fetchFromInternet;
    private boolean showAll;

    @Override
    public String[] getUsages() {
        return new String[] {"[-a] [-i] COURSE-OR-EXERSICE"};
    }

    @Override
    public void getOptions(Options options) {
        options.addOption("a", "all", false, "Show all information for a specific course");
        options.addOption("i", "internet", false, "Get the information from the server");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.ctx = context;
        this.workDir = ctx.getWorkDir();
        this.io = ctx.getIo();

        String[] stringArgs = args.getArgs();
        useWorkingDirectory = (stringArgs.length == 0);
        fetchFromInternet = args.hasOption("i");
        showAll = args.hasOption("a");

        if (fetchFromInternet) {
            if (useWorkingDirectory) {
                io.errorln("You must give a course as an argument.");
                printUsage(ctx);
                return;
            }
            courseName = stringArgs[0];
            printInfoFromInternet(courseName);
        } else {
            printLocalInfo(args.getArgs());
        }
        this.ctx.getAnalyticsFacade().saveAnalytics(courseName, "info");
    }

    private void printInfoFromInternet(String courseName) {
        if (!ctx.hasLogin()) {
            io.errorln("Loading a course from a server requires login.");
            return;
        }

        CourseFinder finder = ctx.createCourseFinder();

        if (finder.search(courseName)) {
            printCourse(finder.getCourse());
        }
    }

    private void printLocalInfo(String[] stringArgs) {
        info = ctx.getCourseInfo();
        if (info == null) {
            io.println(
                    "You have to be in a course directory"
                            + " or use the -i option with the course name "
                            + "to get the information from the server.");
            printUsage(ctx);
            return;
        }

        courseName = ctx.getCourseInfo().getCourseName();

        if (useWorkingDirectory) {
            // if in exercise directory, print info for that exercise.
            Exercise exercise = getCurrentExercise(workDir);
            if (exercise == null) {
                printCourse(info.getCourse());
            } else {
                printExercise(exercise);
            }
        } else {
            if (stringArgs.length != 1) {
                io.errorln("You can only give one path for this command.");
                printUsage(ctx);
                return;
            }
            String path = stringArgs[0];
            printInfoFromParameters(path);
        }
    }

    private void printInfoFromParameters(String pathName) {
        // Check if path pointed to exercise directory
        Exercise exercise = info.getExercise(pathName);
        if (exercise != null) {
            printExercise(exercise);
            return;
        }

        // Check if path pointed to course directory
        Course course = info.getCourse();
        if (course != null) {
            printCourse(course);
        } else {
            io.errorln("Not a course directory.");
            io.errorln("Use the -i option to get course from server.");
            printUsage(ctx);
        }
    }

    private void printCourse(Course course) {
        printCourseShort(course);
        if (showAll) {
            printCourseDetails(course);
        }
        printExerciseList(course);
    }

    private void printExerciseList(Course course) {
        List<Exercise> exercises = course.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            io.println("Exercises: -");
            return;
        }

        io.println("Exercises: ");
        for (Exercise exercise : exercises) {
            if (showAll) {
                printExerciseFull(exercise);
            } else {
                io.println("    " + exercise.getName());
            }
        }
    }

    private void printCourseShort(Course course) {
        io.println("Course name: " + course.getName());
        io.println("Number of available exercises: " + course.getExercises().size());
        io.println("Number of completed exercises: " + getCompletedExerciseCount(course));
        io.println("Number of locked exercises: " + course.getUnlockables().size());
    }

    private void printCourseDetails(Course course) {
        io.println("Unlockables:" + course.getUnlockables().toString());
        io.println("Course id: " + course.getId());
        io.println("Details URL: " + course.getDetailsUrl());
        io.println("Reviews URL: " + course.getReviewsUrl());
        io.println("Statistics URLs:" + course.getSpywareUrls().toString());
        io.println("UnlockUrl: " + course.getUnlockUrl());
        io.println("CometUrl: " + course.getCometUrl());
    }

    private void printExercise(Exercise exercise) {
        if (showAll) {
            printExerciseFull(exercise);
        } else {
            printExerciseShort(exercise);
        }
    }

    private void printExerciseShort(Exercise exercise) {
        io.println("Exercise: " + exercise.getName());
        if (exercise.getDeadline() != null) {
            io.println("Deadline: " + exercise.getDeadlineDate());
        }

        if (exercise.hasDeadlinePassed() && !exercise.isCompleted()) {
            io.println(ColorUtil.colorString("deadline passed", Color.PURPLE));
        } else {
            if (!exercise.isCompleted() && exercise.isAttempted()) {
                io.println(ColorUtil.colorString("attempted", Color.BLUE));
            } else {
                printFlag("completed", exercise.isCompleted());
            }
            if (exercise.requiresReview()) {
                printFlag("reviewed", exercise.isReviewed());
            }
        }
    }

    private int getCompletedExerciseCount(Course course) {
        int completed = 0;
        for (Exercise exercise : course.getExercises()) {
            if (exercise.isCompleted()) {
                completed++;
            }
        }
        return completed;
    }

    private Exercise getCurrentExercise(WorkDir workDir) {
        List<Exercise> exercises = workDir.getExercises(false, false);
        if (exercises.size() == 1) {
            return exercises.get(0);
        }
        return null;
    }

    private void printFlag(String string, boolean setting) {
        Color color;
        if (setting) {
            color = Color.GREEN;
        } else {
            color = Color.RED;
            string = "not " + string;
        }
        io.println(ColorUtil.colorString(string, color));
    }

    private void printExerciseFull(Exercise exercise) {
        io.println("    Exercise name: " + exercise.getName());
        io.println("    Exercise id: " + exercise.getId());
        io.println("    Is locked: " + exercise.isLocked());
        io.println("    Deadline description: " + exercise.getDeadlineDescription());
        io.println("    Deadline: " + CourseInfo.getExerciseDeadline(exercise));
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
        io.println();
    }
}
