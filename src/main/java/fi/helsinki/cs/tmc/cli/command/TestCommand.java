package fi.helsinki.cs.tmc.cli.command;

/**
 * Class is a test command class.
 */
public class TestCommand implements Command {
    @Override
    public String getDescription() {
        return "This is an easter egg test command.";
    }

    @Override
    public String getName() {
        return "EasterEgg";
    }

    @Override
    public void run() {
        System.out.println("Let's run easter egg.");
    }
}
