package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

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
        List<String> argsList = args.getArgList();
        if (argsList.isEmpty()) {
            // adds the current working directory
            if (!workdir.addPath()) {
                // if addPath() returns false, we're not in a course directory
                io.println(
                        "No exercise specified. Please use this command in an exercise directory "
                        + "or pass the name of the exercise as an argument.");
                return;
            }
        } else if (argsList.size() > 1) {
            io.println(
                    "Error: Too many arguments. Pass the name of the exercise you wish to send to "
                            + "the pastebin as the only argument.");
            return;
        }

        String message;
        if (!args.hasOption("n")) {
            if (!args.hasOption("m")) {
                message = ExternalsUtil.getUserEditedMessage(
                        "\n"
                                + "#   Write a message for your paste in this file and save it.\n"
                                + "#   If you don't want to send a message with your paste, "
                                + "use the '-n' switch.\n"
                                + "#   Lines beginning with # are comments and will be ignored.",
                        "tmc-paste-message",
                        true);
            } else {
                message = args.getOptionValue("m");
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
        Callable<URI> callable = core.pasteWithComment(
                new TmcCliProgressObserver(io), exercise, message);
        URI uri;

        try {
            uri = callable.call();
        } catch (Exception e) {
            logger.error("Unable to connect to server", e);
            io.println("Unable to connect to server:");
            e.printStackTrace();
            return;
        }

        io.println("Paste sent for exercise " + exercise.getName());
        io.println(uri.toString());
        if (args.hasOption("o")) {
            ExternalsUtil.openInBrowser(uri);
        }
    }
}
