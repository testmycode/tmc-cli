package fi.helsinki.cs.tmc.cli.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandFactoryTest {

    Application app;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    CliContext ctx;

    @Before
    public void setUp() {
        ctx = new CliContext(new TestIo());
    }

    @Test
    public void constructorAddsCommands() {
        assertTrue(!CommandFactory.getCommands().isEmpty());
    }

    @Test
    public void createCommandWorksWithRealCommand() {
        assertNotNull(CommandFactory.createCommand(ctx, "help"));
    }

    @Test
    public void createCommandWorksWithBadCommand() {
        assertNull(CommandFactory.createCommand(ctx, "foobar"));
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
        assertNotNull(CommandFactory.createCommand(ctx, "good"));
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

