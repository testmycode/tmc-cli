package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;

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
}
