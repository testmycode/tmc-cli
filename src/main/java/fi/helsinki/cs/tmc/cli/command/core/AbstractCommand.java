package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.command.LoginCommand;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);

    /**
     * Method runs the command.
     *
     * @param args Command line arguments for this command.
     * @param io The terminal IO object
     */
    public abstract void run(CommandLine args, Io io);

    public abstract void getOptions(Options options);

    private Options getOptions() {
        Options options;

        options = new Options();
        options.addOption("h", "help", true, "Get this help message.");
        getOptions(options);

        return options;
    }

    public void execute(String[] stringArgs, Io io) {
        GnuParser parser = new GnuParser();
        CommandLine args;

        try {
            args = parser.parse(getOptions(), stringArgs);
        } catch (ParseException e) {
            logger.warn("Invalid command line arguments.", e);
            io.println("Invalid command line arguments.");
            return;
        }

        if (args.hasOption("h")) {
            io.println("TODO: Print help message");
            return;
        }

        run(args, io);
    }
}
