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

import java.util.List;
import java.util.Set;

public class CommandFactoryTest {

    Application app;

    @Rule public ExpectedException exception = ExpectedException.none();

    private CliContext ctx;

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

    @Test
    public void getCommandsGivesDefaultCommands() {
        assertNotSame(0, CommandFactory.getCommands().size());
        assertTrue(CommandFactory.getCommands().contains(
                CommandFactory.castToCommandClass(HelpCommand.class)));
    }

    @Test
    public void getCommandsWhenSingleCommandIsAdded() {
        int oldSize = CommandFactory.getCommands().size();
        CommandFactory.addCommand("good", "", GoodCommand.class);
        assertEquals(oldSize + 1, CommandFactory.getCommands().size());
        assertTrue(CommandFactory.getCommands().contains(
                CommandFactory.castToCommandClass(GoodCommand.class)));
    }

    @Test
    public void getCategoryCommandsWhenItsEmpty() {
        assertEquals(null, CommandFactory.getCategoryCommands("xyz"));
    }

    @Test
    public void getCategoryCommandsWhenSingleCategorizedCommandIsAdded() {
        int oldSize = CommandFactory.getCommands().size();
        CommandFactory.addCommand("good", "xyz", GoodCommand.class);
        List<Class<Command>> list = CommandFactory.getCategoryCommands("xyz");
        assertEquals(1, list.size());
        assertEquals(GoodCommand.class, list.get(0));
    }

    @Test
    public void getCommandCategoriesIsNotEmpty() {
        Set<String> list = CommandFactory.getCommandCategories();
        assertNotSame(0, list.size());
    }

    @Test
    public void getCommandCategoriesContainsDefaultCategory() {
        Set<String> list = CommandFactory.getCommandCategories();
        assertTrue(list.contains(""));
    }

    @Test
    public void getCommandCategoriesContainsHiddenCategory() {
        Set<String> list = CommandFactory.getCommandCategories();
        assertTrue(list.contains("hidden"));
    }

    @Test
    public void getCommandCategoriesContainsAdminCategory() {
        Set<String> list = CommandFactory.getCommandCategories();
        assertTrue(list.contains("admin"));
    }
}
