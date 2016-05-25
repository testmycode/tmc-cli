package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

public class SubmitCommand implements Command {
    private Application app;

    public SubmitCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "Submit exercises";
    }

    @Override
    public String getName() {
        return "submit";
    }

    @Override
    public void run(String[] args) {
    }
}
