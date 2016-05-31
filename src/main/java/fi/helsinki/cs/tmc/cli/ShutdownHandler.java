package fi.helsinki.cs.tmc.cli;

public class ShutdownHandler extends Thread {

    // ANSI escape code that resets console text color back to default.
    // todo: remove this and get it from elsewhere..-
    private static final String ANSI_RESET = "\u001B[0m";

    /**
     * Executes when the program exits.
     */
    @Override
    public void run() {
        // Reset terminal color back to default in case we exit in the middle of
        // colored printing. Otherwise user is left with a colored terminal.
        System.out.println(ANSI_RESET);
    }

}
