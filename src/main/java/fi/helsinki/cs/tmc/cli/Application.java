package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.CommandFactory;
import fi.helsinki.cs.tmc.cli.command.CommandInterface;

import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * The application class for the program.
 * TODO: we should move all the command line related code to
 * somewhere else from here.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private CommandFactory commands;
    private TmcCore tmcCore;
    private Settings settings;

    private Options options;
    private GnuParser parser;

    public Application() {
        this.parser = new GnuParser();
        this.options = new Options();
        this.commands = new CommandFactory();
        options.addOption("h", "help", false, "Display help information about tmc-cli.");
        options.addOption("v", "version", false, "Give the version of the tmc-cli.");
    }

    /**
     * Find first argument that isn't flag.
     */
    private int findCommand(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                return i;
            }
        }
        return -1;
    }

    private boolean runCommand(String name, String[] args) {
        CommandInterface command = commands.getCommand(this, name);
        if (command == null) {
            System.out.println("Command " + name + " doesn't exist.");
            return false;
        }

        command.run(args);
        return true;
    }

    private boolean parseArgs(String[] args) {
        CommandLine line;
        try {
            line = this.parser.parse(this.options, args);
        } catch (ParseException e) {
            System.out.println("Invalid command line arguments. " + e);
            return false;
        }

        if (line.hasOption("h")) {
            runCommand("help", new String[0]);
            return false;
        }
        if (line.hasOption("v")) {
            System.out.println("TMC-CLI version " + getVersion());
            return false;
        }
        return true;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tmc-cli", this.options);
    }

    public void run(String[] args) {
        String[] tmcArgs;
        String[] commandArgs;
        String commandName;
        int commandIndex;

        commandIndex = findCommand(args);

        if (commandIndex != -1) {
            commandName = args[commandIndex];

            /* split the arguments to the tmc's and command's arguments */
            tmcArgs = Arrays.copyOfRange(args, 0, commandIndex);
            commandArgs = Arrays.copyOfRange(args, commandIndex + 1, args.length);


        } else {
            commandName = "help";
            tmcArgs = args;
            commandArgs = new String[0];
        }

        if (!parseArgs(tmcArgs)) {
            return;
        }

        runCommand(commandName, commandArgs);
    }

    public void createTmcCore(Settings settings) {
        TaskExecutor tmcLangs;

        tmcLangs = new TaskExecutorImpl();
        this.settings = settings;
        this.tmcCore = new TmcCore(settings, tmcLangs);
        /*XXX should we somehow check if the authentication is successful here */
    }

    public CommandFactory getCommandMap() {
        return this.commands;
    }

    public TmcCore getTmcCore() {
        if (this.tmcCore == null) {
            SettingsIo settingsio = new SettingsIo();
            DirectoryUtil dirutil = new DirectoryUtil();
            Settings settings;

            if (dirutil.getConfigFile() != null) {
                // If we're in a course directory, we load settings matching the course
                // Otherwise we just load the last used settings
                CourseInfoIo courseio = new CourseInfoIo(dirutil.getConfigFile());
                CourseInfo courseinfo = courseio.load();
                if (courseinfo == null) {
                    System.out.println("Course configuration file "
                            + dirutil.getConfigFile().toString()
                            + "is invalid.");
                    return null;
                }
                settings = settingsio.load(courseinfo.getUsername(),
                        courseinfo.getServerAddress());
            } else {
                settings = settingsio.load();
            }

            if (settings == null) {
                // If no settings are present
                System.out.println("You are not logged in. Log in using: tmc login");
                return null;
            }
            createTmcCore(settings);
        }
        return this.tmcCore;
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }

    public static String getVersion() {
        String path = "/maven.prop";
        InputStream stream = Application.class.getResourceAsStream(path);
        if (stream == null) {
            return "n/a";
        }

        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            logger.warn("Failed to get version", e);
            return "n/a";
        }
    }

    public CourseInfo createCourseInfo(String courseName) {
        return new CourseInfo(settings, courseName);
    }
}
