package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExerciseUpdater;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
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
        updateExercises(core, info, workDir.getConfigFile());
    }

    public void updateExercises(TmcCore core, CourseInfo info, Path configFile) {
        ExerciseUpdater exerciseUpdater = new ExerciseUpdater(core, info.getCourse());
        if (!exerciseUpdater.updatesAvailable()) {
            io.println("All exercises are up-to-date");
            return;
        }

        printExercises(exerciseUpdater.getNewExercises(), "New exercises:");
        printExercises(exerciseUpdater.getUpdatedExercises(), "Modified exercises:");
        io.println("");

        List<Exercise> downloaded = exerciseUpdater.downloadUpdates();
        if (downloaded.isEmpty()) {
            io.println("Failed to download exercises");
            return;
        }

        if (!exerciseUpdater.updateCourseJson(info, configFile)) {
            io.println("Failed to update course info");
        }
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
