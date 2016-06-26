package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@Command(name = "paste", desc = "Submit exercise to pastebin")
public class PasteCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(SubmitCommand.class);

    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("n", "no-message", false, "Don't send a message with your paste");
        options.addOption("m", "message", true, "Add a message to your paste as a parameter");
        options.addOption("o", "open", false, "Open the link to your paste in a web browser");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        Application app = getApp();
        TmcCore core = app.getTmcCore();
        if (core == null) {
            return;
        }
        WorkDir workdir = app.getWorkDir();
        String[] stringArgs = args.getArgs();

        Boolean valid;
        if (stringArgs.length == 0) {
            valid = workdir.addPath();
        } else if (stringArgs.length == 1) {
            valid = workdir.addPath(stringArgs[0]);
        } else {
            io.println(
                    "Error: Too many arguments. Expected 1, got " + stringArgs.length);
            return;
        }
        if (!valid) {
            io.println(
                    "No exercise specified. Please use this command in an exercise directory "
                    + "or pass the name of the exercise as an argument.");
            return;
        }

        String message;
        if (!args.hasOption("n")) {
            if (args.hasOption("m")) {
                message = args.getOptionValue("m");
            } else if (io.readConfirmation("Attach a message to your paste?", true)) {
                message = ExternalsUtil.getUserEditedMessage(
                        "\n"
                                + "#   Write a message for your paste in this file and save it.\n"
                                + "#   If you don't want to send a message with your paste, "
                                + "use the '-n' switch.\n"
                                + "#   Lines beginning with # are comments and will be ignored.",
                        "tmc-paste-message",
                        true);
            } else {
                message = "";
            }
        } else {
            message = "";
        }

        List<String> exercisenames = workdir.getExerciseNames();
        if (exercisenames.size() != 1) {
            io.println(
                    "Error: Matched too many exercises.");
            return;
        }

        String exerciseName = exercisenames.get(0);
        CourseInfo courseinfo = CourseInfoIo.load(app.getWorkDir().getConfigFile());
        Exercise exercise = courseinfo.getExercise(exerciseName);
        URI uri = TmcUtil.sendPaste(core, exercise, message);
        if (uri == null) {
            io.println("Unable to send the paste");
            return;
        }

        io.println("Paste sent for exercise " + exercise.getName());
        io.println(uri.toString());
        if (args.hasOption("o")) {
            ExternalsUtil.openInBrowser(uri);
        }
    }
}
