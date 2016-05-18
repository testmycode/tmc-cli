package fi.helsinki.cs.tmc.cli.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Class creates a map for commands.
 */
public class CommandMap {

    private Map<String, Command> commands;

    public CommandMap() {
        this.commands = new HashMap<>();
        createCommand(new TestCommand());
    }
    
    private void createCommand(Command command) {
        this.commands.put(command.name(), command);
    }
    
    public Command getCommand(String name) {
        return commands.get(name);
    }
}
