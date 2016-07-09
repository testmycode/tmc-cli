package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.core.domain.Course;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * Command for listing all available courses to user.
 */
@Command(name = "courses", desc = "List the available courses")
public class ListCoursesCommand extends AbstractCommand {

    private CliContext ctx;
    private Io io;

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.ctx = getContext();
        this.io = ctx.getIo();

        if (! getContext().loadBackend()) {
            return;
        }

        List<Settings> accountsList = SettingsIo.getSettingsList();
        boolean isFirst = true;

        for (Settings settings : accountsList) {
            if (!isFirst) {
                io.println("");
            }
            if (accountsList.size() > 1) {
                io.println(Color.colorString("Server " + settings.getServerAddress(),
                        Color.AnsiColor.ANSI_YELLOW));
            }
            printCourseList(settings);
            isFirst = false;
        }
    }

    private void printCourseList(Settings settings) {
        ctx.useSettings(settings);
        List<Course> courses = TmcUtil.listCourses(getContext());
        if (courses.isEmpty()) {
            io.println("No courses found from the server.");
            return;
        }
        for (Course course : courses) {
            io.println(course.getName());
        }
        io.println("\nFound " + courses.size() + " courses from the server.");
    }
}
