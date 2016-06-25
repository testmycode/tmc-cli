package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Before;
import org.junit.Test;

public class TestCommandTest {

    Application app;
    TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        app.createTmcCore(new Settings());
    }

    @Test
    public void runWorksRightWithoutOption() {
        String[] args = {"easter-egg"};
        app.run(args);
        io.assertContains("Let's run easter egg.");
    }
    
    @Test
    public void runWorksRightWithOption() {
        String[] args = {"easter-egg", "-a"};
        app.run(args);
        io.assertContains("Let's run easter egg with -a");
    }
}
