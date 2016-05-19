package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command for listing all available courses to user.
 */
public class ListCoursesCommand implements Command {
    @Override
    public String getDescription() {
        return "List the available courses.";
    }

    @Override
    public String getName() {
        return "list-courses";
    }

    @Override
    public void run() {
        Callable<List<Course>> callable;
        List<Course> courses;
        TaskExecutor tmcLangs;
        Settings settings = new Settings();
        TmcCore core;

        tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        callable = core.listCourses(ProgressObserver.NULL_OBSERVER);
        try {
            courses = callable.call();
        } catch (Exception e) {
            return;
        }
        for (Course course : courses) {
            System.out.println(course.getName());
        }
    }
}
