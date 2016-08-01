package fi.helsinki.cs.tmc.cli.core;

import fi.helsinki.cs.tmc.cli.io.HelpGenerator;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

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
     * TODO io param isn't needed anymore!!!!
     *
     * @param args Command line arguments for this command.
     * @param ctx The context object.
     */
    public abstract void run(CliContext ctx, CommandLine args);

    public void execute(CliContext context, String[] stringArgs) {
        CommandLine args = parseArgs(context, stringArgs);
        if (args != null) {
            run(context, args);
        }
    }

    public CommandLine parseArgs(CliContext context, String[] stringArgs) {
        GnuParser parser = new GnuParser();
        CommandLine args;
        Options options = getOptions();

        Io io = context.getIo();

        try {
            args = parser.parse(options, stringArgs);
        } catch (ParseException e) {
            logger.warn("Invalid command line arguments.", e);
            io.errorln("Invalid command line arguments.");
            io.errorln(e.getMessage());
            return null;
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
            return null;
        }
        return args;
    }
}
