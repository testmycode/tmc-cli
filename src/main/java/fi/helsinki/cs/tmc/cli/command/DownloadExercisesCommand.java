package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Command(name = "download", desc = "Download exercises for a specific course")
public class DownloadExercisesCommand extends AbstractCommand {

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

        showAll = args.hasOption("a");

        Application app = getApp();
        TmcCore core = app.getTmcCore();
        WorkDir workDir = getApp().getWorkDir();
        if (core == null) {
            return;
        }

        if (workDir.getConfigFile() != null) {
            io.println("Can't download a course inside a course directory.");
            return;
        }

        String courseName = stringArgs[0];
        Course course = TmcUtil.findCourse(core, courseName);
        if (course == null) {
            io.println("Course doesn't exist.");
            return;
        }
        List<Exercise> filtered = getFilteredExercises(course);
        // todo: If -c switch, use core.downloadCompletedExercises() to download user's old
        //       submissions. Not yet implemented in tmc-core.

        Color.AnsiColor color1 = app.getColor("progressbar-left");
        Color.AnsiColor color2 = app.getColor("progressbar-right");
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(io, color1, color2);

        List<Exercise> exercises = TmcUtil.downloadExercises(core, filtered, progobs);
        if (exercises == null) {
            io.println("Failed to download exercises");
            return;
        }

        if (course.getExercises().isEmpty()) {
            io.println("The '" + courseName + "' course doesn't have any exercises.");
        } else {
            io.println("The '" + courseName + "' course has " +
                    course.getExercises().size() + " exercises");

            int failedCount = (filtered.size() - exercises.size());
            if (failedCount > 0) {
                io.println("  from which " +
                        exercises.size() + " exercises were succesfully downloaded");
                io.println(Color.colorString("  and of which " + failedCount + " failed.",
                        Color.AnsiColor.ANSI_RED));
                //TODO we could print the names of the not downloaded exercises here
            } else {
                io.println("  from which " +
                        exercises.size() + " exercises were downloaded.");
            }
            io.println("Use -a flag to download also your completed exercises.");
        }

        Path configFile = app.getWorkDir().getWorkingDirectory()
                .resolve(courseName)
                .resolve(CourseInfoIo.COURSE_CONFIG);
        CourseInfo info = app.createCourseInfo(course);
        info.setExercises(course.getExercises());
        CourseInfoIo.save(info, configFile);
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
}
