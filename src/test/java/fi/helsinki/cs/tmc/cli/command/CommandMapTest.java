package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandMapTest {

    CommandMap cm;

    public CommandMapTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        cm = new CommandMap();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void constructorAddsCommands() {
        assertNotNull(cm.getCommands());
    }
    
    @Test
    public void getCommandWorksWithRealCommand() {
        assertNotNull(cm.getCommands());
    }
}
