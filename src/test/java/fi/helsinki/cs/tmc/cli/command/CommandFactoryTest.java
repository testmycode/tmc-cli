package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @Command(name = "good", desc = "test")
    public static class GoodCommand extends AbstractCommand {
        public GoodCommand(Application app) {
            System.out.println("hello");
        }

        @Override
        public void run(String[] args, Io io) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void addGoodCommand() {
        factory.addCommand(GoodCommand.class);
        //TODO check the all the stuff in the command
        assertNotNull(factory.createCommand(app, "good"));
    }

    public static class BadCommand extends AbstractCommand {
        public BadCommand(Application app) {
            System.out.println("hello");
        }

        @Override
        public void run(String[] args, Io io) {
            throw new UnsupportedOperationException();
        }
    }

    @Test(expected = RuntimeException.class)
    public void addCommandWithoutProperAnnotation() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("annotation"));
        factory.addCommand(BadCommand.class);
    }

    public static class ReallyBadCommand {
        public ReallyBadCommand(Application app) {
            System.out.println("hello");
        }
    }

    @Test(expected = RuntimeException.class)
    public void addCommandThatDoesntExtendInterface() {
        //WARNING The exception code leads to unpredicatble test results
        //exception.expect(RuntimeException.class);
        //exception.expectMessage(contains("Interface"));
        factory.addCommand(ReallyBadCommand.class);
    }
}

