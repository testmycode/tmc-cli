package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

@Command(name = "help", desc = "Lists every command")
public class HelpCommand implements CommandInterface {
    private Application app;
    private CommandFactory commands;

    public HelpCommand(Application app) {
        this.app = app;
        this.commands = app.getCommandMap();
    }

    @Override
    public void run(String[] args) {
        System.out.println("Usage: tmc-cli [args] COMMAND [command-args]\n");
        System.out.println("TMC commands:");
        for (Command command : this.commands.getCommands()) {
            System.out.println("  " + command.name() + "\t" + command.desc());
        }
        System.out.println("");
        app.printHelp();
    }
}
