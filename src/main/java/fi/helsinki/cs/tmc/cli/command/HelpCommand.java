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
import java.util.Set;

@Command(name = "help", desc = "List every command")
public class HelpCommand extends AbstractCommand {
    private int longestNameLength;
    private Io io;

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        Application app = getContext().getApp();
        this.io = io;

        StringBuilder sb = new StringBuilder();
        sb.append("TMC commands:\n");

        List<String> commandStrings = getCommandStrings();
        Collections.sort(commandStrings);
        for (String commandString : commandStrings) {
            sb.append(commandString).append("\n");
        }

        app.printHelp(sb.toString());
    }

    private List<String> getCommandStrings() {
        List<String> strings = new ArrayList<>();
        Set<Class<Command>> commands = CommandFactory.getCommands();
        commands.remove(castToCommandClass(ShellHelperCommand.class));

        longestNameLength = longestName(commands);
        for (Class<Command> commandClass : commands) {
            Command command = CommandFactory.getCommand(commandClass);
            strings.add(createCommandString(command));
        }
        return strings;
    }

    @SuppressWarnings("unchecked")
    private Class<Command> castToCommandClass(Class klass) {
        return (Class<Command>)klass;
    }

    private String createCommandString(Command command) {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(command.name());
        for (int i = 0; i < longestNameLength - command.name().length() + 1; i++) {
            builder.append(" ");
        }
        builder.append(command.desc());
        return builder.toString();
    }

    private int longestName(Set<Class<Command>> commandList) {
        int longest = 0;
        for (Class<Command> commandClass : commandList) {
            Command command = CommandFactory.getCommand(commandClass);
            longest = Math.max(longest, command.name().length());
        }
        return longest;
    }
}