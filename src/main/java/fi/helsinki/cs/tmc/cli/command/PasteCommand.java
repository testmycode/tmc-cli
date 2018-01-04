package fi.helsinki.cs.tmc.cli.command;

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

    private CliContext ctx;
    private Io io;

    String message;
    boolean noMessage;
    boolean openInBrowser;

    @Override
    public String[] getUsages() {
        return new String[] {"tmc paste [-o] [-n] [-m MESSAGE] [EXERCISE]"};
    }

    @Override
    public void getOptions(Options options) {
        options.addOption("n", "no-message", false, "Don't send a message with your paste");
        options.addOption("m", "message", true, "Add a message to your paste as a parameter");
        options.addOption("o", "open", false, "Open the link to your paste in a web browser");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.ctx = context;
        this.io = context.getIo();
        WorkDir workdir = ctx.getWorkDir();

        if (!parseArgs(args)) {
            return;
        }

        if (!ctx.checkIsLoggedIn(false)) {
            return;
        }

        List<Exercise> exercises = workdir.getExercises();
        if (exercises.size() != 1) {
            io.errorln("Matched too many exercises.");
            printUsage(context);
            return;
        }

        Exercise exercise = exercises.get(0);
        this.ctx.getAnalyticsFacade().saveAnalytics(exercise, "paste");

        if (!noMessage && message == null) {
            if (io.readConfirmation("Attach a message to your paste?", true)) {
                message =
                        ExternalsUtil.getUserEditedMessage(
                            "\n"
                            + "#   Write a message for your paste in this file and save it.\n"
                            + "#   If you don't want to send a message with your paste, "
                            + "use the '-n' switch.\n"
                            + "#   Lines beginning with # are comments and will be ignored.",
                        "tmc-paste-message",
                        true);
            }
        }
        sendPaste(message, exercise);
    }

    private boolean parseArgs(CommandLine args) {
        WorkDir workdir = ctx.getWorkDir();
        String[] stringArgs = args.getArgs();

        this.message = args.getOptionValue("m");
        this.noMessage = args.hasOption("n");
        this.openInBrowser = args.hasOption("o");

        if (noMessage && message != null) {
            io.errorln("You can't have the no-message flag and message set at the same time.");
            printUsage(ctx);
            return false;
        }
        if (stringArgs.length > 1) {
            io.errorln("Error: Too many arguments.");
            printUsage(ctx);
            return false;
        }

        if (stringArgs.length == 1) {
            if (!workdir.addPath(stringArgs[0])) {
                io.errorln("The path '" + stringArgs[0] + "' is not valid exercise.");
                return false;
            }
        }
        if (workdir.getExercises().size() != 1) {
            io.errorln("You are not in exercise directory.");
            return false;
        }
        return true;
    }

    private void sendPaste(String message, Exercise exercise) {
        if (message == null) {
            message = "";
        }

        URI uri = TmcUtil.sendPaste(ctx, exercise, message);
        if (uri == null && exercise.hasDeadlinePassed()) {
            io.errorln(
                    "Unable to send the paste."
                            + " The deadline for submitting this exercise has passed");
            return;
        }
        if (uri == null) {
            io.errorln("Unable to send the paste");
            return;
        }

        io.println("Paste sent for exercise " + exercise.getName());
        io.println(uri.toString());
        if (openInBrowser) {
            ExternalsUtil.openInBrowser(uri);
        }
    }
}
