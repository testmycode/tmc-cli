package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.LoginCommand;
import fi.helsinki.cs.tmc.cli.io.HelpGenerator;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);

    private Application app;

    protected void setApplication(Application app) {
        this.app = app;
    }

    protected Application getApp() {
        return this.app;
    }

    /**
     * Override this method if you want longer description for the command than
     * the annotation description.
     *
     * @return Description text
     */
    public String getDescription() {
        return null;
    }

    public abstract void getOptions(Options options);

    private Options getOptions() {
        Options options;

        options = new Options();
        options.addOption("h", "help", false, "Get this help message.");
        getOptions(options);

        return options;
    }

    /**
     * Method runs the command.
     *
     * @param args Command line arguments for this command.
     * @param io The terminal IO object
     */
    public abstract void run(CommandLine args, Io io);

    public void execute(String[] stringArgs, Io io) {
        GnuParser parser = new GnuParser();
        CommandLine args;
        Options options = getOptions();

        try {
            args = parser.parse(options, stringArgs);
        } catch (ParseException e) {
            logger.warn("Invalid command line arguments.", e);
            io.println("Invalid command line arguments.");
            io.println(e.getMessage());
            return;
        }

        if (args.hasOption("h")) {
            Class<Command> klass;
            klass = CommandFactory.castToCommandClass(this.getClass());
            Command command = CommandFactory.getCommand(klass);

            String usage = "tmc " + command.name();
            String desc = getDescription();
            if (desc == null) {
                desc = command.desc();
            }
            HelpGenerator.run(io, usage, desc, options);
            return;
        }

        run(args, io);
    }
}
