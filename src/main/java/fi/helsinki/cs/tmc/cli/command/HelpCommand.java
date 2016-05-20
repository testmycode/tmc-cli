package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

public class HelpCommand implements Command {
    private CommandMap commands;

    public HelpCommand(Application app) {
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
    public void run(String[] args) {
        System.out.println("TMC commands:");
        for (Command command : this.commands.getCommands().values()) {
            System.out.println("  " + command.getName() + "\t" + command.getDescription());
        }
    }
}