package fi.helsinki.cs.tmc.cli.io;

public class ShutdownHandler extends Thread {

    private final Io io;

    public ShutdownHandler(Io io) {
        this.io = io;
    }

    /**
     * Executed when the program exits.
     */
    @Override
    public void run() {
        // Reset terminal color back to default in case we exit in the middle of
        // colored printing. Otherwise user is left with a colored terminal.
        io.println(Color.RESET.toString());
    }

    public void enable() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    public void disable() {
        Runtime.getRuntime().removeShutdownHook(this);
    }
}
