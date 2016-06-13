package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command for listing all available courses to user.
 */
@Command(name = "courses", desc = "List the available courses")
public class ListCoursesCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(ListCoursesCommand.class);

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        List<Course> courses;
        TmcCore core;

        core = getApp().getTmcCore();
        if (core == null) {
            return;
        }
        courses = TmcUtil.listCourses(core);
        if (courses.isEmpty()) {
            io.println("No courses found on this server.");
            return;
        }

        for (Course course : courses) {
            io.println(course.getName());
        }
        io.println("Found " + courses.size() + " courses on this server.");
        
    }
}
