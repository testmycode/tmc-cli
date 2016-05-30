package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;

public class RunTestsCommand implements Command {

    private final Application app;

    public RunTestsCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "Run local exercise tests";
    }

    @Override
    public String getName() {
        return "run-tests";
    }

    @Override
    public void run(String[] args, Io io) {
    }

}
