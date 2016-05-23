package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class ListExercisesCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private Application app;

    public ListExercisesCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "List the exercises for a specific course";
    }

    @Override
    public String getName() {
        return "list-exercises";
    }

    @Override
    public void run(String[] args) {
        Callable<List<Course>> callable;
        List<Course> courses;
        TmcCore core;
        Course course = null;
        if (args.length == 0) {
            return;
        }
        core = this.app.getTmcCore();
        callable = core.listCourses(ProgressObserver.NULL_OBSERVER);

        try {
            courses = callable.call();
        } catch (Exception e) {
            logger.warn("Failed to get courses to list the exercises", e);
            return;
        }

        for (Course item : courses) {
            if (item.getName().equals(args[0])) {
                course = item;
            }
        }

        try {
            course = core.getCourseDetails(ProgressObserver.NULL_OBSERVER, course).call();
        } catch (Exception e) {
            logger.warn("Failed to get course details to list the exercises", e);
            return;
        }
        List<Exercise> exercises = course.getExercises();

        for (Exercise exercise : exercises) {
            System.out.println(exercise.getName());
        }
    }
}