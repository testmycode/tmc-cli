package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

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
    public void run(CliContext context, CommandLine args) {
        this.io = context.getIo();
        if (!context.loadBackend()) {
            return;
        }
        WorkDir workdir = context.getWorkDir();
        String[] stringArgs = args.getArgs();

        Boolean valid;
        if (stringArgs.length == 0) {
            valid = workdir.addPath();
        } else if (stringArgs.length == 1) {
            valid = workdir.addPath(stringArgs[0]);
        } else {
            io.println("Error: Too many arguments. Expected 1, got " + stringArgs.length);
            return;
        }
        if (!valid) {
            io.println("The command can be used in an exercise directory without the exercise name"
                    + " or in a course directory with the name as an argument.");
            return;
        }
        
        List<String> exercisenames = workdir.getExerciseNames();
        if (exercisenames.size() != 1) {
            io.println("Error: Matched too many exercises.");
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

        String exerciseName = exercisenames.get(0);
        CourseInfo info = context.getCourseInfo();
        Exercise exercise = info.getExercise(exerciseName);
        URI uri = TmcUtil.sendPaste(context, exercise, message);
        if (uri == null && exercise.hasDeadlinePassed()) {
            io.println("Unable to send the paste."
                    + " The deadline for submitting this exercise has passed");
            return;
        }
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
