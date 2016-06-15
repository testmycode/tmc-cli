package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;

public class ShutdownHandler extends Thread {

    private final Io io;

    public ShutdownHandler(Io io) {
        this.io = io;
    }

    /**
     * Executes when the program exits.
     */
    @Override
    public void run() {
        // Reset terminal color back to default in case we exit in the middle of
        // colored printing. Otherwise user is left with a colored terminal.
        io.println(Color.AnsiColor.ANSI_RESET.toString());
    }

}
