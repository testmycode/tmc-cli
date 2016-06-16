package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

@Command(name = "update", desc = "Update exercises")
public class UpdateCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
    }

    // flag --all
    @Override
    public void run(CommandLine args, Io io) {
        String[] stringArgs = args.getArgs();
        TmcCore core;

        //TODO: Do this in all commands
        if (stringArgs.length > 0) {
            io.println("Use in the course directory");
            return;
        }

        core = getApp().getTmcCore();
        if (core == null) {
            return;
        }

        WorkDir workDir = getApp().getWorkDir();

        if (workDir.getCourseDirectory() == null) {
            io.println("Not a course directory");
            return;
        }

        CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
        Course course = info.getCourse();

        GetUpdatableExercises.UpdateResult result;
        result = TmcUtil.getUpdatableExercises(core, course);
        if (result == null) {
            return;
        }

        boolean hasNewExercises = !result.getNewExercises().isEmpty();
        boolean hasUpdatedExercises = !result.getUpdatedExercises().isEmpty();

        if (!hasNewExercises && !hasUpdatedExercises) {
            io.println("All exercises are up-to-date");
            return;
        }

        if (hasNewExercises) {
            io.println("New exercises:");
        }
        for (Exercise exercise : result.getNewExercises()) {
            io.println(" " + exercise.getName());
        }

        if (hasUpdatedExercises) {
            io.println("Modified exercises:");
        }
        for (Exercise exercise : result.getUpdatedExercises()) {
            io.println(" " + exercise.getName());
        }

        io.println("");

        List<Exercise> exercises = result.getNewExercises();
        exercises.addAll(result.getUpdatedExercises());
        //exercises.addAll(Deleted exercises ???); todo

        TmcUtil.downloadExercises(core, exercises);

        info.setExercises(TmcUtil.findCourse(core, course.getName()).getExercises());

        CourseInfoIo.save(info, workDir.getConfigFile());
    }
}
