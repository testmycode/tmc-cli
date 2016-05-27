package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DownloadExercisesCommand implements Command {
    private Application app;

    public DownloadExercisesCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "Download exercises for a specific course";
    }

    @Override
    public String getName() {
        return "download";
    }

    @Override
    public void run(String[] args) {
        Course course;
        TmcCore core;

        if (args.length == 0) {
            System.out.println("You must give course name as argument.");
            System.out.println("USAGE: tmc " + getName() + " COURSE");
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        DirectoryUtil dirUtil = new DirectoryUtil();
        course = TmcUtil.findCourse(core, args[0]);
        if (course == null) {
            System.out.println("Course doesn't exist.");
            return;
        }
        System.out.println(TmcUtil.downloadAllExercises(core, course));

        Path configFile = Paths.get(System.getProperty("user.dir"))
                .resolve(args[0])
                .resolve(CourseInfoIo.COURSE_CONFIG);
        CourseInfoIo infoIo = new CourseInfoIo(configFile);
        infoIo.save(app.createCourseInfo(args[0]));
    }
}
