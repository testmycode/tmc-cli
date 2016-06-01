package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;

import org.junit.Before;
import org.junit.Test;

public class CommandFactoryTest {

    CommandFactory factory;
    Application app;

    @Before
    public void setUp() {
        app = new Application(new TerminalIo());
        factory = app.getCommandFactory();
    }

    @Test
    public void constructorAddsCommands() {
        assertTrue(!factory.getCommands().isEmpty());
    }

    @Test
    public void createCommandWorksWithRealCommand() {
        assertNotNull(factory.createCommand(app, "help"));
    }

    @Test
    public void createCommandWorksWithBadCommand() {
        assertNull(factory.createCommand(app, "foobar"));
    }
}

