package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Command(name = "download", desc = "Download exercises for a specific course")
public class DownloadExercisesCommand extends AbstractCommand {

    private CliContext ctx;
    private boolean showAll;

    @Override
    public void getOptions(Options options) {
        options.addOption("a", "all", false,
                "Download all available exercises, including previously completed");

        // Download old submissions. Not implemented in tmc-core yet
        //options.addOption("c", "completed", false, "Download previously completed exercises");
    }

    @Override
    public void run(CommandLine args, Io io) {
        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0 || stringArgs.length > 1) {
            io.println("You must give course name as an argument.");
            io.println("Usage: tmc download COURSE");
            return;
        }

        ctx = getContext();
        showAll = args.hasOption("a");

        if (!ctx.loadBackend()) {
            return;
        }

        WorkDir workDir = ctx.getWorkDir();
        if (workDir.getConfigFile() != null) {
            io.println("Can't download a course inside a course directory.");
            return;
        }

        String courseName = stringArgs[0];
        Course course = findCourse(courseName);
        if (course == null) {
            return;
        }
        List<Exercise> filtered = getFilteredExercises(course);
        // todo: If -c switch, use core.downloadCompletedExercises() to download user's old
        //       submissions. Not yet implemented in tmc-core.

        Color.AnsiColor color1 = ctx.getApp().getColor("progressbar-left");
        Color.AnsiColor color2 = ctx.getApp().getColor("progressbar-right");
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(io, color1, color2);

        List<Exercise> exercises = TmcUtil.downloadExercises(ctx, filtered, progobs);
        if (exercises == null) {
            io.println("Failed to download exercises");
            return;
        }

        printStatistics(course, filtered.size(), exercises.size());
        createNewCourse(course);
    }

    // TODO This method could be moved somewhere else.
    private Course findCourse(String courseName) {
        Io io = ctx.getIo();

        List<Settings> accountsList = SettingsIo.getSettingsList();
        // LinkedHashMap is used here to preserve ordering.
        Map<Settings, Course> matches = new LinkedHashMap<>();

        for (Settings settings : accountsList) {
            ctx.useSettings(settings);
            Course course = TmcUtil.findCourse(ctx, courseName);
            if (course != null) {
                matches.put(settings, course);
            }
        }

        if (matches.size() == 1) {
            Entry<Settings, Course> firstEntry = matches.entrySet().iterator().next();
            ctx.useSettings(firstEntry.getKey());
            return firstEntry.getValue();
        } else if (matches.isEmpty()) {
            io.println("Course doesn't exist.");
            return null;
        }

        io.println("There is " + matches.size()
                + " courses with same name at different servers.");

        for (Entry<Settings, Course> entrySet : matches.entrySet()) {
            Settings settings = entrySet.getKey();
            Course course = entrySet.getValue();

            if (io.readConfirmation("Download course from "
                    + settings.getServerAddress() + " with '"
                    + settings.getUsername() + "' account", false)) {
                ctx.useSettings(settings);
                return course;
            }
        }
        io.println("The previous course was last that matched.");
        return null;
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
            io.println("The '" + courseName + "' course has "
                    + course.getExercises().size() + " exercises");

            int failedCount = (requestCount - downloadCount);
            if (failedCount > 0) {
                io.println("  of which " + (requestCount - failedCount)
                        + " exercises were succesfully downloaded");
                io.println(Color.colorString("  and of which " + failedCount + " failed.",
                        Color.AnsiColor.ANSI_RED));
                //TODO we could print the names of the not downloaded exercises here
            } else {
                io.println("  of which "
                        + downloadCount + " exercises were downloaded.");
            }
            io.println("Use -a/--all to download completed exercises as well.");
        }
    }

    private void createNewCourse(Course course) {
        WorkDir workDir = ctx.getWorkDir();
        Path configFile = workDir.getWorkingDirectory()
                .resolve(course.getName())
                .resolve(CourseInfoIo.COURSE_CONFIG);

        CourseInfo info = ctx.createCourseInfo(course);
        info.setExercises(course.getExercises());
        CourseInfoIo.save(info, configFile);
    }
}
