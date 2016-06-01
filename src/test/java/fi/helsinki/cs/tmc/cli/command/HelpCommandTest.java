package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Before;
import org.junit.Test;

public class HelpCommandTest {

    Application app;
    TestIo testIo;

    @Before
    public void setUp() {
        testIo = new TestIo();
        app = new Application(testIo);
        app.createTmcCore(new Settings());
    }

    @Test
    public void helpListsAllCommands() {
        String[] args = {"help"};
        app.run(args);
        assertTrue(testIo.printedText.contains("help"));
    }
}
