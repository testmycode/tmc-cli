package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

@Command(name = "update", desc = "Update exercises")
public class UpdateCommand extends AbstractCommand {

    private Io io;

    @Override
    public void getOptions(Options options) {
        // --all or --force
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        String[] stringArgs = args.getArgs();

        //TODO: Do this in all commands
        if (stringArgs.length > 0) {
            io.println("Use in the course directory");
            return;
        }

        TmcCore core = getApp().getTmcCore();
        if (core == null) {
            return;
        }

        WorkDir workDir = getApp().getWorkDir();

        if (workDir.getCourseDirectory() == null) {
            io.println("Not a course directory");
            return;
        }

        CourseInfo info = CourseInfoIo.load(workDir.getConfigFile());
        Course localCourse = info.getCourse();

        UpdateResult result = TmcUtil.getUpdatableExercises(core, localCourse);
        if (result == null) {
            return;
        }

        List<Exercise> newExercises = result.getNewExercises();
        List<Exercise> updatedExercises = result.getUpdatedExercises();

        if (newExercises.isEmpty() && updatedExercises.isEmpty()) {
            io.println("All exercises are up-to-date");
            return;
        }

        printExercises(newExercises, "New exercises:");
        printExercises(updatedExercises, "Modified exercises:");
        io.println("");

        //exercises.addAll(deletedExercises); What if user has deleted exercise folder?
        newExercises.addAll(updatedExercises);

        List<Exercise> downloadedExercises = TmcUtil.downloadExercises(core, newExercises,
                new TmcCliProgressObserver(io));
        if (downloadedExercises.isEmpty()) {
            io.println("Failed to download exercises");
            return;
        }

        Course course = TmcUtil.findCourse(core, localCourse.getName());
        if (course == null) {
            io.println("Failed to update course info");
            return;
        }
        info.setExercises(course.getExercises());
        CourseInfoIo.save(info, workDir.getConfigFile());
    }

    private void printExercises(List<Exercise> exercises, String message) {
        if (!exercises.isEmpty()) {
            io.println(message);
            for (Exercise exercise : exercises) {
                io.println(" " + exercise.getName());
            }
        }
    }
}
