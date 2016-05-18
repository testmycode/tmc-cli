package fi.helsinki.cs.tmc.cli.command;

/**
 * Class is a test command class
 */
public class TestCommand implements Command {

    @Override
    public String description() {
        return "This is an easter egg test command.";
    }

    @Override
    public String name() {
        return "EasterEgg";
    }

    @Override
    public void run() {
        System.out.println("Let's run easter egg.");
    }
    
}
