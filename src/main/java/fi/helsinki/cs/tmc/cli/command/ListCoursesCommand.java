package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.domain.Course;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * Command for listing all available courses to user.
 */
@Command(name = "courses", desc = "List the available courses")
public class ListCoursesCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        if (! getContext().loadBackend()) {
            return;
        }
        List<Course> courses = TmcUtil.listCourses(getContext());
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
