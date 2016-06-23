package fi.helsinki.cs.tmc.cli.command.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    Application app;

    @Before
    public void setUp() {
        app = new Application(new TerminalIo());
    }

    @Test
    public void constructorAddsCommands() {
        assertTrue(!CommandFactory.getCommands().isEmpty());
    }

    @Test
    public void createCommandWorksWithRealCommand() {
        assertNotNull(CommandFactory.createCommand(app, "help"));
    }

    @Test
    public void createCommandWorksWithBadCommand() {
        assertNull(CommandFactory.createCommand(app, "foobar"));
    }

    @Command(name = "good", desc = "test")
    public static class GoodCommand extends AbstractCommand {

        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CommandLine args, Io io) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void addGoodCommand() {
        CommandFactory.addCommand(GoodCommand.class);
        //TODO check the all the stuff in the command
        assertNotNull(CommandFactory.createCommand(app, "good"));
    }

    public static class BadCommand extends AbstractCommand {

        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CommandLine args, Io io) {
            throw new UnsupportedOperationException();
        }
    }

    @Test(expected = RuntimeException.class)
    public void addCommandWithoutProperAnnotation() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("annotation"));
        CommandFactory.addCommand(BadCommand.class);
    }

    public static class ReallyBadCommand {
    }

    @Test(expected = RuntimeException.class)
    public void addCommandThatDoesntExtendInterface() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("Interface"));
        CommandFactory.addCommand(ReallyBadCommand.class);
    }
}

