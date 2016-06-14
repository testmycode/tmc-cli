package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Command(name = "info", desc = "Show course info for a specific course")
public class CourseInfoCommand extends AbstractCommand {
    //private static final Logger logger = LoggerFactory.getLogger(ListCoursesCommand.class);
    private Course course;
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
        
        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0) {
            io.println("You must give the course name as a parameter.");
            return;
        }
        
        course = TmcUtil.findCourse(core, stringArgs[0]);
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
    }
    
    private void printCourseDetails() {
        io.println("Course id: " + course.getId());
        io.println("Details URL: " + course.getDetailsUrl());
        io.println("Reviews URL: " + course.getReviewsUrl());
        io.println("Statistics URLs:" + course.getSpywareUrls().toString());
        io.println("UnlockUrl: " + course.getUnlockUrl());
        io.println("CometUrl: " + course.getCometUrl());
        //io.println("Exercises loaded: " + course.isExercisesLoaded());
        io.println("Unlockables:" + course.getUnlockables().toString());
    }
    
//    private String getSpywareUrls() {
//        List<URI> urls = course.getSpywareUrls();
//        if (urls == null || urls.isEmpty()) {
//            return " -";
//        }
//        
//        StringBuilder temp = new StringBuilder();
//        for (URI url : urls) {
//            temp.append(" " + url);
//        }
//        return temp.toString();
//    }
    
//    private String getUnlockables() {
//        List<String> unlockables = course.getUnlockables();
//        if (unlockables == null || unlockables.isEmpty()) {
//            return " -";
//        }
//        
//        StringBuilder temp = new StringBuilder();
//        for (String unlockable : unlockables) {
//            temp.append(" " + unlockable);
//        }
//        return temp.toString();
//    }
    
    private void printExercises(boolean showAll) {
        List<Exercise> exercises = course.getExercises();
        if (exercises == null || exercises.isEmpty()) {
            io.println("List of exercises: -");
            return;
        }
        if (showAll) {
            printExerciseDetails(exercises);
        } else {
            printExerciseNames(exercises);
        }
    }
    
    private void printExerciseNames(List<Exercise> exercises) {
        io.print("List of exercises: ");
        for (Exercise exercise : exercises) {
            io.print(exercise.getName() + " ");
        }
        io.println("");
    }
    
    private void printExerciseDetails(List<Exercise> exercises) {
        io.println("Info on course exercises:");
        for (Exercise exercise : exercises) {
            printExercise(exercise);
        }
        
//        io.print(", " + exercise.getDeadline());
//        io.print(", " + exercise.getReturnUrl());
    }
    
    private void printExercise(Exercise exercise) {
        io.println("Exercise name: " + exercise.getName());
        io.println("Exercise id: " + exercise.getId());
        io.println("Is locked: " + exercise.isLocked());
        io.println("Deadline description: " + exercise.getDeadlineDescription());
        io.println("Deadline: " + exercise.getDeadline());
        io.println("Deadline date: " + exercise.getDeadlineDate());
        io.println("Checksum: " + exercise.getChecksum());
        io.println("Return URL: " + exercise.getReturnUrl());
        io.println("Zip URL: " + exercise.getZipUrl());
        io.println("Is returnable: " + exercise.isReturnable());
        io.println("Review required: " + exercise.isRequiresReview());
        io.println("Is attempted: " + exercise.isAttempted());
        io.println("Is completed: " + exercise.isCompleted());
        io.println("Is reviewed: " + exercise.isReviewed());
        io.println("Is all review points given: " + exercise.isAllReviewPointsGiven());
        io.println("Memory limit: " + exercise.getMemoryLimit());
        io.println("Runtime parameters: " + Arrays.toString(exercise.getRuntimeParams()));
        io.println("Is code review request enabled: " + exercise.isCodeReviewRequestsEnabled());
        
    }
    
}
