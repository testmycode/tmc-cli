package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class is a test command class
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
        TaskExecutor tmcLangs;
        TmcCore core;
        String filename = System.getProperty("user.home") + file.separator + ".tmc";

        tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        callable = core.listCourses(ProgressObserver.NULL_OBSERVER);
        List<Course> list = callable.call();
    }
}
