package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(name = "download", desc = "Download exercises for a specific course")
public class DownloadExercisesCommand implements CommandInterface {

    private Application app;

    public DownloadExercisesCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        Course course;
        TmcCore core;

        if (args.length == 0) {
            io.println("You must give course name as argument.");
            io.println("Usage: tmc download COURSE");
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        DirectoryUtil dirUtil = new DirectoryUtil();
        course = TmcUtil.findCourse(core, args[0]);
        if (course == null) {
            io.println("Course doesn't exist.");
            return;
        }

        List<Exercise> exercises = TmcUtil.downloadAllExercises(core, course);
        io.println(exercises.toString());

        Path configFile = Paths.get(System.getProperty("user.dir"))
                .resolve(args[0])
                .resolve(CourseInfoIo.COURSE_CONFIG);
        CourseInfoIo infoIo = new CourseInfoIo(configFile);
        CourseInfo info = app.createCourseInfo(args[0]);
        info.setExercises(exercises);
        infoIo.save(info);
    }
}
