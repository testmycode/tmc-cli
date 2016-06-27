package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.CliContext;

import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class used for creating new instances of commands.
 * TODO make this class completely static.
 */
public class CommandFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandFactory.class);
    private static final Map<String, Class<Command>> commands = new HashMap<>();

    static {
        /* force load the CommandList so that it's static initialization block is executed
           this is used instead of import so that the ide's won't cry about the nonexistent class
         */
        try {
            Class.forName("fi.helsinki.cs.tmc.cli.command.core.CommandList");
        } catch (ClassNotFoundException ex) {
            System.out.println("Fail " + ex);
        }
    }

    /**
     * Put a command to the command list.
     * This method is used for generating the commands list from the annotations.
     *
     * @param name the name visible to the user
     * @param commandClass the class of the command objects
     */
    public static void addCommand(String name, Class commandClass) {
        Class<Command> klass = castToCommandClass(commandClass);
        CommandFactory.commands.put(name, klass);
    }

    /**
     * Merge this method implementation with the above version.
     *
     * @param commandClass The class of the command
     */
    public static void addCommand(Class commandClass) {
        Class<Command> klass = castToCommandClass(commandClass);
        Annotation annotation = klass.getAnnotation(Command.class);
        if (annotation == null) {
            throw new RuntimeException("Command must have Command annotation");
        }
        Command command = (Command) annotation;
        if (!AbstractCommand.class.isAssignableFrom(commandClass)) {
            throw new RuntimeException("Command must implement CommandInterface");
        }
        CommandFactory.commands.put(command.name(), klass);
    }

    /**
     * Create new instance of the command.
     *
     * @param context Execution context given to the created command
     * @param name Name of the command
     * @return A new command instance
     */
    public static AbstractCommand createCommand(CliContext context, String name) {
        Class commandClass = CommandFactory.commands.get(name);
        if (commandClass == null) {
            return null;
        }
        try {
            AbstractCommand command = (AbstractCommand)commandClass.newInstance();
            command.setContext(context);
            return command;
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
        Class<?> klass = commandClass;
        Annotation annotation = klass.getAnnotation(Command.class);
        return (Command)annotation;
    }

    /**
     * Get list of all commands.
     * This is used for creating help listing.
     *
     * @return Set of commands.
     */
    public static Set<Class<Command>> getCommands() {
        return new HashSet<>(CommandFactory.commands.values());
    }

    @SuppressWarnings("unchecked")
    public static Class<Command> castToCommandClass(Class command) {
        return command;
    }
}
