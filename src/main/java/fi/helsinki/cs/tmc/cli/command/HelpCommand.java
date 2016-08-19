package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.core.CommandFactory;
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
    private CliContext context;
    private Io io;

    @Override
    public String[] getUsages() {
        return new String[] {"[category]"};
    }

    @Override
    public void getOptions(Options options) {}

    @Override
    public void run(CliContext context, CommandLine args) {
        this.context = context;
        this.io = context.getIo();

        String category = handleArgs(args);
        if (category == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (category.equals("")) {
            sb.append("TMC commands:\n");
        } else {
            sb.append("TMC commands in ").append(category).append(":\n");
        }

        List<String> commandStrings = getCommandStrings(category);
        Collections.sort(commandStrings);
        for (String commandString : commandStrings) {
            sb.append(commandString).append("\n");
        }

        context.getApp().printHelp(sb.toString());
    }

    private String handleArgs(CommandLine args) {
        String[] stringArguments = args.getArgs();
        if (stringArguments.length > 1) {
            io.errorln("Too many arguments.");
            printUsage(context);
            return null;
        }
        String category = "";
        if (stringArguments.length == 1) {
            category = stringArguments[0];
        }
        if (category.equals("all")) {
            return category;
        }
        Set<String> helpCategories = CommandFactory.getCommandCategories();
        if (!helpCategories.contains(category)) {
            io.errorln("Unknown command category \"" + category + "\".");
            return null;
        }
        return category;
    }

    private List<String> getCommandStrings(String category) {
        List<String> strings = new ArrayList<>();
        List<Class<Command>> commands;
        if (category.equals("all")) {
            commands = CommandFactory.getCommands();
        } else {
            commands = CommandFactory.getCategoryCommands(category);
        }

        longestNameLength = longestName(commands);
        for (Class<Command> commandClass : commands) {
            Command command = CommandFactory.getCommand(commandClass);
            strings.add(createCommandString(command));
        }
        longestNameLength = Math.max(longestNameLength, 8);
        return strings;
    }

    @SuppressWarnings("unchecked")
    private Class<Command> castToCommandClass(Class klass) {
        return (Class<Command>) klass;
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

    private int longestName(List<Class<Command>> commandList) {
        int longest = 0;
        for (Class<Command> commandClass : commandList) {
            Command command = CommandFactory.getCommand(commandClass);
            longest = Math.max(longest, command.name().length());
        }
        return longest;
    }
}
