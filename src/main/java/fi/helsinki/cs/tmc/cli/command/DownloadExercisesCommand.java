package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
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

    @Override
    public void getOptions(Options options) {
        options.addOption("a", "all", false,
                "Download all available exercises, including previously completed");

        // Not implemented in tmc-core yet
        //options.addOption("c", "completed", false, "Download previously completed exercises");
    }

    @Override
    public void run(CommandLine args, Io io) {
        String[] stringArgs = args.getArgs();
        if (stringArgs.length == 0) {
            io.println("You must give course name as an argument.");
            io.println("Usage: tmc download COURSE");
            return;
        }

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

        Course course = TmcUtil.findCourse(core, stringArgs[0]);
        if (course == null) {
            io.println("Course doesn't exist.");
            return;
        }
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(io);

        List<Exercise> filtered;
        if (args.hasOption("a")) {
            filtered = course.getExercises();
        } else {
            filtered = new ArrayList<>();
            for (Exercise exercise : course.getExercises()) {
                if (!exercise.isCompleted()) {
                    filtered.add(exercise);
                }
            }
        } // todo: use downloadCompletedExercises() when implemented in core

        List<Exercise> exercises = TmcUtil.downloadExercises(core, filtered, progobs);
        io.println(exercises.toString());

        Path configFile = app.getWorkDir().getWorkingDirectory()
                .resolve(stringArgs[0])
                .resolve(CourseInfoIo.COURSE_CONFIG);
        CourseInfo info = app.createCourseInfo(course);
        info.setExercises(exercises);
        CourseInfoIo.save(info, configFile);
    }
}
