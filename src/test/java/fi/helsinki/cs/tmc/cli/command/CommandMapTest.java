package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.Test;

public class CommandMapTest {

    CommandMap cm;

    @Before
    public void setUp() {
        cm = new CommandMap();
        cm.createCommands(new Application());
    }

    @Test
    public void constructorAddsCommands() {
        assertTrue(!cm.getCommands().isEmpty());
    }
    
    @Test
    public void getCommandWorksWithRealCommand() {
        assertNotNull(cm.getCommand("help"));
    }
    
    @Test
    public void getCommandWorksWithBadCommand() {
        assertNull(cm.getCommand("foobar"));
    }
}
