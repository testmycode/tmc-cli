package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class Application {
    private CommandMap commands;
    private TmcCore tmcCore;
    private boolean initialized;

    public Application() {
        this.initialized = false;
    }

    private void preinit() {
        this.commands = new CommandMap();
        this.commands.createCommands(this);
        this.initialized = true;
    }

    private boolean runCommand(String name, String[] args) {

        if (name.equals("-v")) {
            System.out.println("TMC-CLI version " + getVersion());
            return true;
        }

        Command command = commands.getCommand(name);
        if (command == null) {
            System.out.println("Command " + name + " doesn't exist.");
            return false;
        }

        command.run(args);
        return true;
    }

    public void run(String[] args) {
        String commandName;

        if (!this.initialized) {
            preinit();
        }

        if (args.length > 0) {
            commandName = args[0];
        } else {
            commandName = "help";
        }

        runCommand(commandName, args);
    }

    private void createTmcCore() {
        TaskExecutor tmcLangs;
        Settings settings = new Settings();

        tmcLangs = new TaskExecutorImpl();
        this.tmcCore = new TmcCore(settings, tmcLangs);
        /*XXX should we somehow check if the authentication is successful here */
    }

    public CommandMap getCommandMap() {
        return this.commands;
    }

    public TmcCore getTmcCore() {
        if (this.tmcCore == null) {
            createTmcCore();
        }
        return this.tmcCore;
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }

    private static String getVersion() {

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
            return "n/a";
        }
    }
}
