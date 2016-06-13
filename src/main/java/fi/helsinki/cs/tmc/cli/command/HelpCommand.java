package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Command(name = "help", desc = "List every command")
public class HelpCommand extends AbstractCommand {
    private final int longestName = 14; // Length of the longest command name
    private CommandFactory commands;

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.commands = app.getCommandFactory();

        io.println("Usage: tmc [args] COMMAND [command-args]\n");
        io.println("TMC commands:");
        
        List<String> commandStrings = getCommandStrings();
        Collections.sort(commandStrings);
        for (String commandString : commandStrings) {
            io.println(commandString);
        }
        
        io.println("");
        app.printHelp();
    }
    
    private List<String> getCommandStrings() {
        List<String> strings = new ArrayList<>();
        for (Class<Command> commandClass : this.commands.getCommands()) {
            Command command = commands.getCommand(commandClass);
            if ((Class)commandClass == (Class)TestCommand.class) {
                continue;
            }
            strings.add(createCommandString(command));
        }
        return strings;
    }
    
    private String createCommandString(Command command) {
        StringBuilder builder = new StringBuilder();
        builder.append("  " + command.name());
        for (int i = 0; i < longestName - command.name().length() + 2; i++) {
            builder.append(" ");
        }
        builder.append(command.desc());
        return builder.toString();
    }
}
