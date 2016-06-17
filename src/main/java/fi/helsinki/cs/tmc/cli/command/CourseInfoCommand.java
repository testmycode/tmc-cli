package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.List;

@Command(name = "info", desc = "Show course info for a specific course")
public class CourseInfoCommand extends AbstractCommand {
    private Course course;
    private Exercise exercise;
    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("a", false, "Show all info for a specific course");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        TmcCore core = getApp().getTmcCore();
        if (core == null) {
            return;
        }

        WorkDir workDir = getApp().getWorkDir();
        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0 && workDir.getConfigFile() == null) {
            io.println("You must give the course name as a parameter.");
            return;
        }
        if (workDir.getConfigFile() != null) {
            CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
            course = info.getCourse();
        } else {
            course = TmcUtil.findCourse(core, stringArgs[0]);
        }

        if (course == null) {
            io.println("The course " + stringArgs[0] + " doesn't exist on this server.");
            return;
        }
        
        printCourse(args.hasOption("a"));
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

    private void printExerciseInfo() {
        io.println("Status: ");
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
    
}
