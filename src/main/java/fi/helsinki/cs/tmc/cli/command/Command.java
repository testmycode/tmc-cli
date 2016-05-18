package fi.helsinki.cs.tmc.cli.command;

/**
 * Class is an interface for commands.
 */
public interface Command {
    
    /**
     * Get command description.
     * @return Description
     */
    String description();

    /**
     * Get default command name.
     * @return Name
     */
    String name();

    /**
     * Run command.
     */
    void run();

}
