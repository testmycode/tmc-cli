package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.io.Io;

public abstract class AbstractCommand {
    /**
     * Method runs command.
     * @param args Command line arguments for this command.
     * @param io The terminal IO object
     */
    public abstract void run(String[] args, Io io);
}
