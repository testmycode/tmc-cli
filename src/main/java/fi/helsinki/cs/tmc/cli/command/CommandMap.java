package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
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
    public CommandMap(Application app) {
        this.commands = new HashMap<>();
        createCommand(new TestCommand(app));
        createCommand(new HelpCommand(app));
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

    public Map<String, Command> getCommands() {
        return this.commands;
    }
}
