package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@Command(name = "info", desc = "Show course info for a specific course")
public class CourseInfoCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(ListCoursesCommand.class);
    private Application app;
    private final Options options;
    private boolean showAll = true;
    private Course course;
    private Io io;

    public CourseInfoCommand(Application app) {
        this.app = app;
        this.options = new Options();
        options.addOption("a", false, "Show all info for a specific course");
    }

    @Override
    public void run(String[] args, Io io) {
        this.io = io;
        TmcCore core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        
        if (args.length == 0) {
            io.println("You must give the course name as a parameter.");
            return;
        }
        
        course = TmcUtil.findCourse(core, args[0]);
        if (course == null) {
            io.println("The course doesn't exist on this server.");
        }
        printCourse();
    }
    
    private void printCourse() {
        printCourseShort();
        if (showAll) {
            printCourseDetails();
            //printExerciseDetails();
        } else {
            printExerciseNames();
        }
    }
    
    private void printCourseShort() {
        io.println("Course name: " + course.getName());
        io.println("Number of exercises: " + course.getExercises().size());
    }
    
    private void printCourseDetails() {
        io.println("Course id: " + course.getId());
        io.println("Details URL: " + course.getDetailsUrl());
        io.println("Reviews URL: " + course.getReviewsUrl());
        io.print("Statistics URLs:");
        printSpywareUrls();
        io.println("UnlockUrl: " + course.getUnlockUrl());
        io.println("CometUrl: " + course.getCometUrl());
        //io.println("Exercises loaded: " + course.isExercisesLoaded());
        io.print("Unlockables:");
        printUnlockables();
    }
    
    private void printSpywareUrls() {
        List<URI> urls = course.getSpywareUrls();
        if (urls == null) {
            io.println(" -");
            return;
        }
        for (URI url : urls) {
            io.println(" " + url);
        }
    }
    
    private void printUnlockables() {
        List<String> unlockables = course.getUnlockables();
        if (unlockables == null) {
            io.println(" -");
            return;
        }
        for (String unlockable : unlockables) {
            io.println(" " + unlockable);
        }
    }
    
    private void printExerciseNames() {
        io.print("List of exercises: ");
        List<Exercise> exercises = course.getExercises();
        if (exercises == null) {
            io.println("-");
            return;
        }
        
        for (Exercise exercise : exercises) {
            io.print(exercise.getName() + " ");
        }
        io.println("");
    }
    
    private void printExerciseDetails() {
//        io.print("  " + exercise.getName());
//        io.print(", " + exercise.getDeadline());
//        io.print(", " + exercise.getReturnUrl());
    }
    
}
