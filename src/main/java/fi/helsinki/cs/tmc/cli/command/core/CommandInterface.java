package fi.helsinki.cs.tmc.cli.command.core;

/**
 * Class is an interface for commands.
 */
public interface CommandInterface {
    /**
     * Method runs command.
     * @param args Command line arguments for this command.
     */
    void run(String[] args);
}
