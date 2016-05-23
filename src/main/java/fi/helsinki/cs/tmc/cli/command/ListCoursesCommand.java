package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command for listing all available courses to user.
 */
public class ListCoursesCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ListCoursesCommand.class);
    private Application app;

    public ListCoursesCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "List the available courses.";
    }

    @Override
    public String getName() {
        return "list-courses";
    }

    @Override
    public void run(String[] args) {
        Callable<List<Course>> callable;
        List<Course> courses;
        TmcCore core;

        core = this.app.getTmcCore();
        callable = core.listCourses(ProgressObserver.NULL_OBSERVER);
        try {
            courses = callable.call();
        } catch (Exception e) {
            logger.warn("Failed to get courses to list", e);
            return;
        }
        for (Course course : courses) {
            System.out.println(course.getName());
        }
    }
}
