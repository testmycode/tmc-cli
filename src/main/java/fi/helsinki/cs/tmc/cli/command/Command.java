package fi.helsinki.cs.tmc.cli.command;

/**
 * Class is an interface for commands.
 */
public interface Command {
    /**
     * Get command description.
     * 
     * @return Description
     */
    String getDescription();

    /**
     * Get default command name.
     * 
     * @return Name
     */
    String getName();

    /**
     * Run command.
     */
    void run();
}
