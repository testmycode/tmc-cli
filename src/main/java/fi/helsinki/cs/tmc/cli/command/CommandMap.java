package fi.helsinki.cs.tmc.cli.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Class creates a map for commands.
 */
public class CommandMap {

    private Map<String, Command> commands;

    /**
     * Constructor.
     */
    public CommandMap() {
        this.commands = new HashMap<>();
        createCommand(new TestCommand());
        createCommand(new ListCoursesCommand());
    }

    private void createCommand(Command command) {
        this.commands.put(command.getName(), command);
    }

    /**
     * Get command by default name.
     * @param name
     * @return Command
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }
}
