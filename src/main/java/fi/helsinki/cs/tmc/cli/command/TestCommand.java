package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

/**
 * Class is a test command class
 */
public class TestCommand implements Command {
    public TestCommand(Application app) {
    }

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
