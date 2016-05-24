package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

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
        List<Exercise> exercises;
        List<Exercise> downloaded;
        Course course;
        TmcCore core;

        if (args.length == 0) {
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            System.out.println("You are not logged in. Log in using: tmc login");
            return;
        }
        course = TmcUtil.findCourse(core, args[0]);
        exercises = course.getExercises();
        System.out.println(TmcUtil.downloadExercises(core, exercises));
    }
}
