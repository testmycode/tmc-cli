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
    private Command command;

    /**
     * Override this method if you want longer description for the command than
     * the annotation description.
     *
     * @return Description text
     */
    protected String getDescription() {
        return null;
    }

    /**
     * Override this method if you want to give usage description.
     * Dont't add the 'tmc-cli COMMAND' prefix, because it is added
     * automatically to every line.
     *
     * @return Description text
     */
    public String[] getUsages() {
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

    protected CommandLine parseArgs(CliContext context, String[] stringArgs) {
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
            printHelp(context);
            return null;
        }
        return args;
    }

    protected void printUsage(CliContext context) {
        Io io = context.getIo();
        io.println(getUsageString());
    }

    private Command getCommand() {
        if (this.command == null) {
            Class<Command> klass;
            klass = CommandFactory.castToCommandClass(this.getClass());
            this.command = CommandFactory.getCommand(klass);
        }
        return this.command;
    }

    private void printHelp(CliContext context) {
        Io io = context.getIo();
        Options options = getOptions();

        String usage = getUsageString();
        String desc = getDescription();
        if (desc == null) {
            desc = getCommand().desc();
        }
        HelpGenerator.run(io, usage, desc, options);
    }

    /**
     * TODO print the "Usage:" and "Or:" prefixes to every line.
     * @return The printable usage string.
     */
    private String getUsageString() {
        String prefix = "tmc " + getCommand().name() + " ";
        String[] usages = getUsages();
        if (usages == null) {
            return prefix;
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String line : usages) {
            if (!first) {
                builder.append("\n");
            }
            builder.append(prefix);
            builder.append(line);
            first = false;
        }
        return builder.toString();
    }
}