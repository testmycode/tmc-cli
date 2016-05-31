package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;

@Command(name = "help", desc = "Lists every command")
public class HelpCommand implements CommandInterface {
    private final Application app;
    private final CommandFactory commands;

    public HelpCommand(Application app) {
        this.app = app;
        this.commands = app.getCommandFactory();
    }

    @Override
    public void run(String[] args) {
        System.out.println("Usage: tmc-cli [args] COMMAND [command-args]\n");
        System.out.println("TMC commands:");

        for (Class<Command> commandClass : this.commands.getCommands()) {
            Command command = commands.getCommand(commandClass);
            if ((Class)commandClass == (Class)TestCommand.class) {
                continue;
            }
            System.out.println("  " + command.name() + "\t" + command.desc());
        }
        System.out.println("");
        app.printHelp();
    }
}
