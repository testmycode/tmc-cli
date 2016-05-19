package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;
import java.io.*;
import java.util.Properties;

public class Application {
    private CommandMap commands;
    //private TmcCore core;
    private boolean initialized;

    public Application() {
        this.initialized = false;
    }

    private void preinit() {
        this.commands = new CommandMap(this);
        this.initialized = true;
    }

    private boolean runCommand(String name, String[] args) {
        Command command;

        command = commands.getCommand(name);
        if (command == null) {
            System.out.println("Command " + name + " doesn't exist.");
            return false;
        }

        command.run();
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

    public CommandMap getCommandMap() {
        return this.commands;
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }

    private static String getVersion() {

        String path = "/maven.prop";
        InputStream stream = Application.class.getResourceAsStream(path);
        if (stream == null)
            return "n/a";
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
