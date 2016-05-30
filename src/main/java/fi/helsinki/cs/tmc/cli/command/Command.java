package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.io.Io;

/**
 * Class is an interface for commands.
 */
public interface Command {
    /**
     * Method returns command description.
     * 
     * @return Description
     */
    String getDescription();

    /**
     * Method returns default command name.
     * 
     * @return Name
     */
    String getName();

    /**
     * Method runs command.
     */
    void run(String[] args, Io io);
}
