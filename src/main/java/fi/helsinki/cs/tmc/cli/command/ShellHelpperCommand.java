package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "shell-helpper", desc = "Submit exercises")
public class ShellHelpperCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
        options.addOption("c", false, "List of all commands");
        options.addOption("f", false, "Flags of the main program");
    }

    @Override
    public void run(CommandLine args, Io io) {
        CommandFactory commands = getApp().getCommandFactory();
        if (args.hasOption("c")) {
            for (Class<Command> commandClass : commands.getCommands()) {
                Command command = CommandFactory.getCommand(commandClass);
                io.println(command.name());
            }
        }
    }
    
}
