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
import java.util.List;

@Command(name = "download", desc = "Download exercises for a specific course")
public class DownloadExercisesCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
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

        Color.AnsiColor color1 = app.getColor("progressbar-left");
        Color.AnsiColor color2 = app.getColor("progressbar-right");
        io.println(Color.colorString("foo", color1) + Color.colorString("bar", color2));
        TmcCliProgressObserver progobs = new TmcCliProgressObserver(io, color1, color2);
        List<Exercise> exercises = TmcUtil.downloadAllExercises(core, course, progobs);
        io.println(exercises.toString());

        Path configFile = app.getWorkDir().getWorkingDirectory()
                .resolve(stringArgs[0])
                .resolve(CourseInfoIo.COURSE_CONFIG);
        CourseInfo info = app.createCourseInfo(course);
        info.setExercises(exercises);
        CourseInfoIo.save(info, configFile);
    }
}
