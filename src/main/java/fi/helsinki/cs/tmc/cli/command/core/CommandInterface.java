package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.io.Io;

/**
 * Class is an interface for commands.
 */
public interface CommandInterface {
    /**
     * Method runs command.
     * @param args Command line arguments for this command.
     * @param io The terminal IO object
     */
    void run(String[] args, Io io);
}
