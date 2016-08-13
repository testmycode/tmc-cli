package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.CliProgressObserver;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.CourseFinder;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.List;

@Command(name = "download", desc = "Download exercises for a specific course")
public class DownloadExercisesCommand extends AbstractCommand {

    private CliContext ctx;
    private boolean showAll;

    @Override
    public String[] getUsages() {
        return new String[] {"[-a] COURSE"};
    }

    @Override
    public void getOptions(Options options) {
        options.addOption(
                "a",
                "all",
                false,
                "Download all available exercises, including previously completed");

        // Download old submissions. Not implemented in tmc-core yet
        //options.addOption("c", "completed", false, "Download previously completed exercises");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        Io io = context.getIo();
        ctx = context;

        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0 || stringArgs.length > 1) {
            io.errorln("You must give a course name as an argument.");
            printUsage(ctx);
            return;
        }

        ctx = context;
        showAll = args.hasOption("a");

        if (!ctx.loadBackend()) {
            return;
        }

        WorkDir workDir = ctx.getWorkDir();
        if (workDir.getConfigFile() != null) {
            io.errorln("Can't download a course inside a course directory.");
            return;
        }

        String courseName = stringArgs[0];
        CourseFinder finder = new CourseFinder(ctx);
        if (!finder.search(courseName)) {
            return;
        }
        Course course = finder.getCourse();
        List<Exercise> filtered = getFilteredExercises(course);
        // todo: If -c switch, use core.downloadCompletedExercises() to download user's old
        //       submissions. Not yet implemented in tmc-core.

        Color color1 = ctx.getApp().getColor("progressbar-left");
        Color color2 = ctx.getApp().getColor("progressbar-right");
        CliProgressObserver progobs = new CliProgressObserver(io, color1, color2);

        ctx.useAccount(finder.getAccount());
        List<Exercise> exercises = TmcUtil.downloadExercises(ctx, filtered, progobs);
        if (exercises == null) {
            io.errorln("Failed to download exercises");
            return;
        }

        printStatistics(course, filtered.size(), exercises.size());

        CourseInfoIo.createNewCourse(course, finder.getAccount(), workDir.getWorkingDirectory());
    }

    private List<Exercise> getFilteredExercises(Course course) {
        if (showAll) {
            return course.getExercises();
        }

        List<Exercise> filtered = new ArrayList<>();
        for (Exercise exercise : course.getExercises()) {
            // Teachers may get a list of locked exercises but core still refuses to
            // download them. Filter locked exercises out.
            if (!exercise.isCompleted() && !exercise.isLocked()) {
                filtered.add(exercise);
            }
        }
        return filtered;
    }

    private void printStatistics(Course course, int requestCount, int downloadCount) {
        Io io = ctx.getIo();
        String courseName = course.getName();

        if (course.getExercises().isEmpty()) {
            io.println("The '" + courseName + "' course doesn't have any exercises.");
        } else {
            int totalCount = course.getExercises().size();
            io.println(
                    "The '" + courseName + "' course has " + totalCount + " exercises available");

            int failedCount = (requestCount - downloadCount);
            if (failedCount > 0) {
                io.println(
                        "  of which "
                                + (requestCount - failedCount)
                                + " exercises were succesfully downloaded");
                io.println(
                        ColorUtil.colorString(
                                "  and of which " + failedCount + " failed.", Color.RED));
                //TODO we could print the names of the not downloaded exercises here
            } else {
                io.println("  of which " + downloadCount + " exercises were downloaded.");
            }
            if (!showAll && totalCount != downloadCount) {
                io.println("Use -a/--all to download completed exercises as well.");
            }
        }
    }
}
