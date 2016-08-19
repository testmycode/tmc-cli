package fi.helsinki.cs.tmc.cli.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.HelpCommand;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandFactoryTest {

    Application app;

    @Rule public ExpectedException exception = ExpectedException.none();

    private CliContext ctx;

    @Before
    public void setUp() {
        ctx = new CliContext(new TestIo());
        CommandFactory.reload();
    }

    @Test
    public void constructorAddsCommands() {
        assertTrue(!CommandFactory.getCommands().isEmpty());
    }

    @Test
    public void createHelpCommand() {
        assertNotNull(CommandFactory.createCommand("help"));
    }

    @Test
    public void createNonexistingCommand() {
        assertNull(CommandFactory.createCommand("foobar"));
    }

    @Command(name = "good", desc = "test")
    public static class GoodCommand extends AbstractCommand {

        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CliContext context, CommandLine args) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void addGoodCommand() {
        CommandFactory.addCommand("category", GoodCommand.class);
        //TODO check the all the stuff in the command
        assertNotNull(CommandFactory.createCommand("good"));
    }

    public static class BadCommand extends AbstractCommand {

        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CliContext context, CommandLine args) {
            throw new UnsupportedOperationException();
        }
    }

    @Test(expected = RuntimeException.class)
    public void addCommandWithoutProperAnnotation() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("annotation"));
        CommandFactory.addCommand("category", BadCommand.class);
    }

    private static class ReallyBadCommand {}

    @Test(expected = RuntimeException.class)
    public void addCommandThatDoesntExtendInterface() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("Interface"));
        CommandFactory.addCommand("category", ReallyBadCommand.class);
    }
}
