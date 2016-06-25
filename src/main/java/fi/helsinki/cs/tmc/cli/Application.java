package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.HelpGenerator;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.ShutdownHandler;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.cli.updater.TmcCliUpdater;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The application class for the program.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String previousUpdateDateKey = "update-date";
    private static final long defaultUpdateInterval = 60 * 60 * 1000;
    private static final String usage = "tmc [args] COMMAND [command-args]";

    private HashMap<String, String> properties;
    private TmcCore tmcCore;
    private Settings settings;
    private WorkDir workDir;
    private Io io;

    private boolean inTest;
    private ShutdownHandler shutdownHandler;

    private Options options;
    private GnuParser parser;
    private String commandName;

    public Application(Io io) {
        this.parser = new GnuParser();
        this.options = new Options();
        options.addOption("h", "help", false, "Display help information about tmc-cli");
        options.addOption("v", "version", false, "Give the version of the tmc-cli");

        inTest = true;
        if (io == null) {
            inTest = false;
            io = new TerminalIo();
            shutdownHandler = new ShutdownHandler(io);
            shutdownHandler.enable();
        }

        this.io = io;
        this.workDir = new WorkDir();
        this.properties = SettingsIo.loadProperties();
    }

    public Application(Io io, WorkDir workDir) {
        this(io);
        this.workDir = workDir;
    }

    private boolean runCommand(String name, String[] args) {
        AbstractCommand command = CommandFactory.createCommand(this, name);
        if (command == null) {
            io.println("Command " + name + " doesn't exist.");
            return false;
        }

        command.execute(args, io);
        return true;
    }

    private String[] parseArgs(String[] args) {
        CommandLine line;
        try {
            line = this.parser.parse(this.options, args, true);
        } catch (ParseException e) {
            io.println("Invalid command line arguments.");
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

        if (line.hasOption("h")) {
            // don't run the help sub-command with -h switch
            if (commandName.equals("help")) {
                runCommand("help", new String[0]);
                return null;
            }
            runCommand(commandName, new String[]{"-h"});
            return null;
        }
        if (line.hasOption("v")) {
            io.println("TMC-CLI version " + EnvironmentUtil.getVersion());
            return null;
        }
        return subArgs.toArray(new String[subArgs.size()]);
    }

    public void printHelp(String description) {
        HelpGenerator.run(io, usage, description, this.options);
    }

    public void run(String[] args) {
        if (!inTest) {
            versionCheck();
        }

        String[] commandArgs = parseArgs(args);
        if (commandArgs == null) {
            return;
        }

        runCommand(commandName, commandArgs);

        if (!inTest) {
            shutdownHandler.disable();
        }
    }

    public void createTmcCore(Settings settings) {
        TaskExecutor tmcLangs;

        tmcLangs = new TaskExecutorImpl();
        this.settings = settings;
        this.tmcCore = new TmcCore(settings, tmcLangs);
        /*XXX should we somehow check if the authentication is successful here */
        Path path = getWorkDir().getCourseDirectory();
        if (path == null) {
            settings.setTmcProjectDirectory(Paths.get(System.getProperty("user.dir")));
            return;
        }
        settings.setTmcProjectDirectory(path.getParent());
    }
    
    // Method is used to help testing
    public void setTmcCore(TmcCore tmcCore) {
        this.tmcCore = tmcCore;
    }

    public TmcCore getTmcCore() {
        if (this.tmcCore == null) {
            SettingsIo settingsio = new SettingsIo();
            Settings settings;

            if (workDir.getConfigFile() != null) {
                // If we're in a course directory, we load settings matching the course
                // Otherwise we just load the last used settings
                CourseInfo courseinfo = CourseInfoIo.load(workDir.getConfigFile());
                if (courseinfo == null) {
                    io.println("Course configuration file "
                            + workDir.getConfigFile().toString()
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
                io.println("You are not logged in. Log in using: tmc login");
                return null;
            }
            createTmcCore(settings);
        }
        return this.tmcCore;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public static void main(String[] args) {
        Application app = new Application(null);
        app.run(args);
    }

    public WorkDir getWorkDir() {
        return this.workDir;
    }

    public void setWorkdir(WorkDir workDir) {
        this.workDir = workDir;
    }

    public void setTmcProjectDirectory(Path path) {
        this.settings.setTmcProjectDirectory(path);
    }

    public HashMap<String, String> getProperties() {
        // Loads properties from the global configuration file in .config/tmc-cli/
        return this.properties;
    }

    public Boolean saveProperties() {
        // Saves properties to the global configuration file in .config/tmc-cli/
        return SettingsIo.saveProperties(properties);
    }

    public CourseInfo createCourseInfo(Course course) {
        return new CourseInfo(settings, course);
    }

    private void versionCheck() {
        String previousTimestamp = properties.get(previousUpdateDateKey);
        Date previous = null;

        if (previousTimestamp != null) {
            long time;
            try {
                time = Long.parseLong(previousTimestamp);
            } catch (NumberFormatException ex) {
                io.println("The previous update date isn't number.");
                logger.warn("The previous update date isn't number.", ex);
                return;
            }
            previous = new Date(time);
        }

        Date now = new Date();
        if (previous != null && previous.getTime() + defaultUpdateInterval > now.getTime()) {
            return;
        }

        TmcCliUpdater update = new TmcCliUpdater(io, EnvironmentUtil.getVersion(),
                EnvironmentUtil.isWindows());
        update.run();

        long timestamp = now.getTime();
        properties.put(previousUpdateDateKey, Long.toString(timestamp));
        saveProperties();
    }

    public Color.AnsiColor getColor(String propertyName) {
        String propertyValue = this.properties.get(propertyName);
        Color.AnsiColor color = Color.getColor(propertyValue);
        if (color == null) {
            switch (propertyName) {
                case "progressbar-left":    return Color.AnsiColor.ANSI_CYAN;
                case "progressbar-right":   return Color.AnsiColor.ANSI_CYAN;
                case "testresults-left":    return Color.AnsiColor.ANSI_GREEN;
                case "testresults-right":   return Color.AnsiColor.ANSI_RED;
                default:    return Color.AnsiColor.ANSI_NONE;
            }
        }
        return color;
    }
}
