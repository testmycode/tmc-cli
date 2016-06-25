package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Before;
import org.junit.Test;

public class HelpCommandTest {

    Application app;
    TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        app.createTmcCore(new Settings());
    }

    @Test
    public void helpListsAllCommands() {
        String[] args = {"help"};
        app.run(args);
        io.assertContains("help");
    }
}
