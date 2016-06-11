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

import java.util.List;

@Command(name = "update", desc = "Update exercises")
public class UpdateCommand extends AbstractCommand {

    private Application app;

    public UpdateCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {

        TmcCore core;

        if (args.length > 0) {
            io.println("Use in the course directory");
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }

        WorkDir dirUtil = new WorkDir();

        if (dirUtil.getCourseDirectory() == null) {
            io.println("Not a course directory");
            return;
        }

        CourseInfo info = CourseInfoIo.load(dirUtil.getConfigFile());
        Course course = info.getCourse();
        List<Exercise> exercises;

        try {
            exercises = core.getExerciseUpdates(new TmcCliProgressObserver(), course).call();
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        if (exercises.isEmpty()) {
            io.println("All exercises are up-to-date");
            return;
        }

        io.println("Updates available for:");
        for (Exercise exercise : exercises) {
            io.println(exercise.getName());
        }

        System.out.println(TmcUtil.downloadExercises(core, exercises));
    }
}
