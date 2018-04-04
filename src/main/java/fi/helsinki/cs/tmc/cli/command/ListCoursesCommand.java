package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
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
    public void getOptions(Options options) {}

    @Override
    public void run(CliContext context, CommandLine args) {
        this.ctx = context;
        this.io = ctx.getIo();


        if (!this.ctx.checkIsLoggedIn(false, true)) {
            return;
        }

        this.ctx.getAnalyticsFacade().saveAnalytics("list_courses");
       if (!TmcUtil.hasConnection(ctx)) {
            io.errorln("You don't have internet connection currently.");
            io.errorln("Check the tmc-cli logs if you disagree.");
            return;
        }

        AccountList accountsList = SettingsIo.loadAccountList();
        boolean isFirst = true;

        if (accountsList.getAccountCount() == 0) {
            io.errorln("You haven't logged in on any tmc server.");
            return;
        }

        for (Account settings : accountsList) {
            if (!isFirst) {
                io.println();
            }
            if (accountsList.getAccountCount() > 1) {
                io.println(
                        ColorUtil.colorString(
                                "Server " + settings.getServerAddress(), Color.YELLOW));
            }

            printCourseList(settings);
            isFirst = false;
        }
    }

    private void printCourseList(Account account) {
        ctx.useAccount(account);
        List<Course> courses = TmcUtil.listCourses(ctx);
        if (courses.isEmpty()) {
            io.errorln("No courses found from the server.");
            return;
        }
        for (Course course : courses) {
            io.println(course.getName());
        }
        io.println("\nFound " + courses.size() + " courses from the server.");
    }
}
