package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "paste", desc = "Submit exercise to pastebin")
public class PasteCommand implements CommandInterface {
    private static final Logger logger = LoggerFactory.getLogger(SubmitCommand.class);
    private final Options options;

    private Application app;
    private Io io;

    public PasteCommand(Application app) {
        this.app = app;
        this.options = new Options();
        this.options.addOption("n", "no-message", false, "Don't send a message with your paste");
        this.options.addOption("m", "message", true, "Add a message to your paste as a parameter");
        this.options.addOption("o", "open", false, "Open the link to your paste in a web browser");
    }

    @Override
    public void run(String[] args, Io io) {
        this.io = io;
        CommandLine line;
        try {
            line = parseData(args);
        } catch (ParseException e) {
            io.println("Unable to parse arguments: " + e.getMessage());
            return;
        }
        TmcCore core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
//        List<String> exerciseNames = app.getWorkDir().getExerciseNames(line.getArgs());
//        if (exerciseNames == null || exerciseNames.size() != 1) {
//            io.println(
//                    "No exercise specified. Please use this command from an exercise directory or "
//                    + "pass the name of the exercise as an argument.");
//            return;
//        }
        WorkDir workdir = app.getWorkDir();
        List<String> argsList = line.getArgList();
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
        if (!line.hasOption("n")) {
            if (!line.hasOption("m")) {
                message = ExternalsUtil.getUserEditedMessage(
                        "\n"
                                + "#   Write a message for your paste in this file and save it.\n"
                                + "#   If you don't want to send a message with your paste, "
                                + "use the '-n' switch.\n"
                                + "#   Lines beginning with # are comments and will be ignored.",
                        "tmc_paste_message.txt",
                        true);
            } else {
                message = line.getOptionValue("m");
            }
        } else {
            message = "";
        }

        List<String> exercisenames = workdir.getExerciseNames();
        if (exercisenames.size() != 0) {
            io.println(
                    "Error: Matched too many exercises.");
            return;
        }

        String exerciseName = exercisenames.get(0);
        CourseInfo courseinfo = CourseInfoIo.load(app.getWorkDir().getConfigFile());
        Exercise exercise = courseinfo.getExercise(exerciseName);
        Callable<URI> callable = core.pasteWithComment(
                new TmcCliProgressObserver(), exercise, message);
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
        if (line.hasOption("o")) {
            ExternalsUtil.openInBrowser(uri);
        }
    }

    private CommandLine parseData(String[] args) throws ParseException {
        GnuParser parser = new GnuParser();
        return parser.parse(options, args);
    }


}
