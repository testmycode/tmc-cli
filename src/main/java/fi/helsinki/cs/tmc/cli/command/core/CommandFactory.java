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

    private void addCommand(Class commandClass) {
        Class<Command> klass = commandClass;
        Annotation annotation = klass.getAnnotation(Command.class);
        Command command = (Command) annotation;
        this.commands.put(command.name(), klass);
    }

    public void addCommand(String name, Class commandClass) {
        Class<Command> klass = commandClass;
        this.commands.put(name, klass);
    }

    public CommandInterface createCommand(Application app, String name)  {
        Class commandClass = commands.get(name);
        Constructor<?> cons;
        if (commandClass == null) {
            return null;
        }
        try {
            Class<?> klass = commandClass;
            cons = klass.getConstructor(Application.class);
        } catch (NoSuchMethodException ex) {
            logger.error("Every command MUST have constructor that takes "
                    + "Application object as argument", ex);
            System.exit(1);
            return null;
        } catch (SecurityException ex) {
            logger.error("getCommand failed ", ex);
            return null;
        }
        try {
            return (CommandInterface)cons.newInstance(app);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            logger.error("getCommand failed", ex);
        }
        return null;
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
