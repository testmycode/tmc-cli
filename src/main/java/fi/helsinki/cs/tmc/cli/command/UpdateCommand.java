package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.CliProgressObserver;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.cli.shared.ExerciseUpdater;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.List;

@Command(name = "update", desc = "Update exercises")
public class UpdateCommand extends AbstractCommand {

    private CliContext ctx;
    private Io io;

    @Override
    public void getOptions(Options options) {
        //TODO --all or --force
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.ctx = getContext();
        this.io = io;
        String[] stringArgs = args.getArgs();

        //TODO: Do this in all commands
        if (stringArgs.length > 0) {
            io.println("Use in the course directory");
            return;
        }

        if (!ctx.loadBackend()) {
            return;
        }

        WorkDir workDir = ctx.getWorkDir();

        if (workDir.getCourseDirectory() == null) {
            io.println("Not a course directory");
            return;
        }

        CourseInfo info = ctx.getCourseInfo();
        updateExercises(info, workDir.getConfigFile());
    }

    private void updateExercises(CourseInfo info, Path configFile) {
        ExerciseUpdater exerciseUpdater = new ExerciseUpdater(ctx, info.getCourse());
        if (!exerciseUpdater.updatesAvailable()) {
            io.println("All exercises are up-to-date");
            return;
        }

        printExercises(exerciseUpdater.getNewExercises(), "New exercises:");
        printExercises(exerciseUpdater.getUpdatedExercises(), "Modified exercises:");
        io.println("");

        Color.AnsiColor color1 = getContext().getApp().getColor("progressbar-left");
        Color.AnsiColor color2 = getContext().getApp().getColor("progressbar-right");
        List<Exercise> downloaded = exerciseUpdater.downloadUpdates(
                new CliProgressObserver(io, color1, color2));
        if (downloaded.isEmpty()) {
            io.println("Failed to download exercises");
            return;
        }

        if (!exerciseUpdater.updateCourseJson(info, configFile)) {
            io.println("Failed to update course config file");
        }
    }

    private void printExercises(List<Exercise> exercises, String message) {
        if (!exercises.isEmpty()) {
            io.println(message);
            for (Exercise exercise : exercises) {
                if (exercise.isCompleted()) {
                    // already released and completed on another computer/folder
                    io.println(" " + exercise.getName() + " (already completed)");
                } else {
                    io.println(" " + exercise.getName());
                }
            }
        }
    }
}
