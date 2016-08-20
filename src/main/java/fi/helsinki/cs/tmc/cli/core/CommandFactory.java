package fi.helsinki.cs.tmc.cli.core;

import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class used for creating new instances of commands.
 */
public class CommandFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandFactory.class);
    private static Map<String, Class<Command>> commands;
    private static Map<String, List<Class<Command>>> packages;

    static {
        CommandFactory.reload();
    }

    /**
     * Put a command to the command list.
     * This method is used for generating the commands list from the annotations.
     *
     * @param name the name visible to the user
     * @param packageName the package name that is used to categorize the commands
     * @param commandClass the class of the command objects
     */
    public static void addCommand(String name, String packageName, Class commandClass) {
        Class<Command> klass = castToCommandClass(commandClass);
        CommandFactory.commands.put(name, klass);

        List<Class<Command>> list = CommandFactory.packages.get(packageName);
        if (list == null) {
            list = new ArrayList<>();
            CommandFactory.packages.put(packageName, list);
        }
        list.add(klass);
    }

    /**
     * Create new instance of the command.
     *
     * @param name Name of the command
     * @return A new command instance
     */
    public static AbstractCommand createCommand(String name) {
        Class commandClass = CommandFactory.commands.get(name);
        if (commandClass == null) {
            return null;
        }
        try {
            return (AbstractCommand) commandClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("getCommand failed", ex);
        }
    }

    /**
     * Get the annotation of the command class.
     * This is only used in help command.
     *
     * @param commandClass The class of the command
     * @return The command annotation object
     */
    public static Command getCommand(Class<Command> commandClass) {
        Annotation annotation = commandClass.getAnnotation(Command.class);
        return (Command) annotation;
    }

    /**
     * Get list of all commands.
     * This is used for creating help listing.
     *
     * @return Set of commands.
     */
    public static List<Class<Command>> getCommands() {
        return new ArrayList<>(CommandFactory.commands.values());
    }

    public static Set<String> getCommandCategories() {
        return packages.keySet();
    }

    public static List<Class<Command>> getCategoryCommands(String category) {
        return packages.get(category);
    }

    @SuppressWarnings("unchecked")
    public static Class<Command> castToCommandClass(Class command) {
        return command;
    }

    protected static void reload() {
        CommandFactory.commands = new HashMap<>();
        CommandFactory.packages = new HashMap<>();

        /* Run constructor of the CommandList.
         * This hack is used instead of import so that the IDEs won't cry about the nonexistent
         * class.
         */
        try {
            Class.forName("fi.helsinki.cs.tmc.cli.core.CommandList").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.warn("CommandList initialization failed", ex);
        }
    }
}
