package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;

public class HelpCommand implements Command {
    private Application app;
    private CommandMap commands;

    public HelpCommand(Application app) {
        this.app = app;
        this.commands = app.getCommandMap();
    }

    @Override
    public String getDescription() {
        return "Lists every command";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run(String[] args, Io io) {
        io.println("Usage: tmc-cli [args] COMMAND [command-args]\n");
        io.println("TMC commands:");
        for (Command command : this.commands.getCommands().values()) {
            io.println("  " + command.getName() + "\t" + command.getDescription());
        }
        io.println("");
        app.printHelp();
    }
}
