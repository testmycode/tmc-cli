package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.Application;

import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class used for creating new instances of commands.
 */
public class CommandFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandFactory.class);
    private static final Map<String, Class<Command>> commands = new HashMap<>();

    public CommandFactory() {
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
        Class<Command> klass = commandClass;
        CommandFactory.commands.put(name, klass);
    }

    /**
     * Merge this method implementation with the above version.
     *
     * @param commandClass
     */
    public void addCommand(Class commandClass) {
        Class<Command> klass = commandClass;
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
     * @param app Application that is given to the commands
     * @param name Name of the command
     * @return A new command instance
     */
    public AbstractCommand createCommand(Application app, String name) {
        Class commandClass = CommandFactory.commands.get(name);
        Constructor<?> cons;
        if (commandClass == null) {
            return null;
        }
        try {
            //NOTE: if you create the command as inner class then
            //      you HAVE TO make it static class.
            Class<?> klass = commandClass;
            cons = klass.getConstructor(Application.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Every command MUST have constructor "
                    + "that takes Application object as it's only argument.", ex);
        } catch (SecurityException ex) {
            logger.error("getCommand failed.", ex);
            return null;
        }
        try {
            return (AbstractCommand)cons.newInstance(app);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("getCommand failed", ex);
        }
    }

    /**
     * Get the annotation of the command class.
     * This is only used in help command.
     *
     * @param commandClass
     * @return the command annotation object
     */
    public Command getCommand(Class<Command> commandClass) {
        Class<?> klass = commandClass;
        Annotation annotation = klass.getAnnotation(Command.class);
        return (Command)annotation;
    }

    /**
     * Get list of all commands.
     * This is used for creating help listing.
     *
     * @return set of commands.
     */
    public Set<Class<Command>> getCommands() {
        return new HashSet<>(CommandFactory.commands.values());
    }
}
