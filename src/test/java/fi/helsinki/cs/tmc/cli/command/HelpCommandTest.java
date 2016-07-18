package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Mockito.mock;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;

import org.junit.Before;
import org.junit.Test;

public class HelpCommandTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        CliContext ctx = new CliContext(io, mock(TmcCore.class));
        app = new Application(ctx);
    }

    @Test
    public void helpListsAllCommands() {
        String[] args = {"help"};
        app.run(args);
        io.assertContains("help");
    }
}
