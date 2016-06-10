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
 * Class creates a map for commands.
 */
public class CommandFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommandFactory.class);
    private final Map<String, Class<Command>> commands;

    public CommandFactory() {
        commands = new HashMap<>();
    }

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
        this.commands.put(command.name(), klass);
    }

    public void addCommand(String name, Class commandClass) {
        Class<Command> klass = commandClass;
        this.commands.put(name, klass);
    }

    public AbstractCommand createCommand(Application app, String name) {
        Class commandClass = commands.get(name);
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

    public Command getCommand(Class<Command> commandClass) {
        Class<?> klass = commandClass;
        Annotation annotation = klass.getAnnotation(Command.class);
        return (Command)annotation;
    }

    public Set<Class<Command>> getCommands() {
        return new HashSet<>(this.commands.values());
    }
}
