package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Class creates a map for commands.
 */
public class CommandMap {

    private Map<String, Command> commands;

    public CommandMap() {
        this.commands = new HashMap<>();
    }

    public void createCommands(Application app) {
        createCommand(new TestCommand(app));
        createCommand(new HelpCommand(app));
        createCommand(new ListCoursesCommand(app));
        createCommand(new ListExercisesCommand(app));
        createCommand(new LoginCommand(app));
        createCommand(new DownloadExercisesCommand(app));
        createCommand(new SubmitCommand(app));
    }

    private void createCommand(Command command) {
        this.commands.put(command.getName(), command);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public Map<String, Command> getCommands() {
        return this.commands;
    }
}
