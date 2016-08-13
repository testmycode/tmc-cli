package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.HelpGenerator;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ShutdownHandler;
import fi.helsinki.cs.tmc.cli.updater.AutoUpdater;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The application class for the program.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String previousUpdateDateKey = "update-date";
    private static final long defaultUpdateInterval = 60 * 60 * 1000;
    private static final String usage = "tmc [args] COMMAND [command-args]";

    private ShutdownHandler shutdownHandler;
    private final CliContext context;
    private final Io io;

    private final Options options;
    private final GnuParser parser;
    private String commandName;

    public Application(CliContext context) {
        this.parser = new GnuParser();
        this.options = new Options();

        this.context = context;
        this.io = context.getIo();

        options.addOption("h", "help", false, "Display help information about tmc-cli");
        options.addOption("v", "version", false, "Give the version of the tmc-cli");
        options.addOption("u", "force-update", false, "Force the auto-update");

        //TODO implement the inTests as context.property
        if (!context.inTests()) {
            shutdownHandler = new ShutdownHandler(context.getIo());
            shutdownHandler.enable();
        }
    }

    private boolean runCommand(String name, String[] args) {
        AbstractCommand command = CommandFactory.createCommand(name);
        if (command == null) {
            io.println("Command " + name + " doesn't exist.");
            return false;
        }

        command.execute(context, args);
        return true;
    }

    private String[] parseArgs(String[] args) {
        CommandLine line;
        try {
            line = this.parser.parse(this.options, args, true);
        } catch (ParseException e) {
            io.println(e.getMessage());
            return null;
        }

        List<String> subArgs = new ArrayList<>(Arrays.asList(line.getArgs()));
        if (subArgs.size() > 0) {
            commandName = subArgs.remove(0);
        } else {
            commandName = "help";
        }

        if (commandName.startsWith("-")) {
            io.println("Unrecognized option: " + commandName);
            return null;
        }

        boolean showHelp = line.hasOption("h");
        boolean showVersion = line.hasOption("v");
        boolean forceUpdate = line.hasOption("u");

        if (showHelp) {
            // don't run the help sub-command with -h switch
            if (commandName.equals("help")) {
                runCommand("help", new String[0]);
                return null;
            }
            runCommand(commandName, new String[]{"-h"});
            return null;
        }
        if (showVersion) {
            io.println("TMC-CLI version " + EnvironmentUtil.getVersion());
            return null;
        }
        if (forceUpdate) {
            runAutoUpdate();
            return null;
        }
        return subArgs.toArray(new String[subArgs.size()]);
    }

    public void printHelp(String description) {
        HelpGenerator.run(io, usage, description, this.options);
    }

    public void run(String[] args) {
        context.setApp(this);

        String[] commandArgs = parseArgs(args);
        if (commandArgs == null) {
            return;
        }

        if (!context.inTests() && versionCheck()) {
            return;
        }

        runCommand(commandName, commandArgs);

        if (!context.inTests()) {
            shutdownHandler.disable();
        }
    }

    public static void main(String[] args) {
        Application app = new Application(new CliContext(null));
        app.run(args);
    }

    private boolean versionCheck() {
        Map<String, String> properties = context.getProperties();
        String previousTimestamp = properties.get(previousUpdateDateKey);
        Date previous = null;

        if (previousTimestamp != null) {
            long time;
            try {
                time = Long.parseLong(previousTimestamp);
            } catch (NumberFormatException ex) {
                io.println("The previous update date isn't number.");
                logger.warn("The previous update date isn't number.", ex);
                return false;
            }
            previous = new Date(time);
        }

        Date now = new Date();
        return !(previous != null && previous.getTime() + defaultUpdateInterval > now.getTime()) && runAutoUpdate();

    }

    public boolean runAutoUpdate() {
        Map<String, String> properties = context.getProperties();
        Date now = new Date();
        AutoUpdater update = AutoUpdater.createUpdater(io,
                EnvironmentUtil.getVersion(), EnvironmentUtil.isWindows());
        boolean updated = update.run();

        long timestamp = now.getTime();
        properties.put(previousUpdateDateKey, Long.toString(timestamp));
        context.saveProperties();

        return updated;
    }

    //TODO rename this as getColorProperty and move it somewhere else
    public Color getColor(String propertyName) {
        String propertyValue = context.getProperties().get(propertyName);
        Color color = ColorUtil.getColor(propertyValue);
        if (color == null) {
            switch (propertyName) {
                case "progressbar-left":    return Color.CYAN;
                case "progressbar-right":   return Color.CYAN;
                case "testresults-left":    return Color.GREEN;
                case "testresults-right":   return Color.RED;
                default:    return Color.NONE;
            }
        }
        return color;
    }
}
