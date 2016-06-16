package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.LoginCommand;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

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
        options.addOption("h", "help", false, "Get this help message.");
        getOptions(options);

        return options;
    }

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
            HelpFormatter formatter = new HelpFormatter();
            Class<Command> klass = (Class<Command>)(Class)this.getClass();
            Command command = CommandFactory.getCommand(klass);

            String usage = "tmc " + command.name();
            String header = "\n" + command.desc() + "\nOptions:";
            String footer = "\nCopyright(C) 2016 TestMyCode\nSome rights reserved.";
            formatter.printHelp(new PrintWriter(io), 80, usage, header, options, 2, 2, footer);
            return;
        }

        run(args, io);
    }
}
