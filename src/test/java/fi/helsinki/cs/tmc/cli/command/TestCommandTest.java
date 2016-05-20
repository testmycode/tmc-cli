package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TestCommandTest {

    Application app;
    OutputStream os;

    @Before
    public void setUp() {
        app = new Application();

        os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);
    }

    @Test
    public void runWorksRightWithoutOption() {
        String[] args = {"EasterEgg"};
        app.run(args);
        assertTrue(os.toString().contains("Let's run easter egg."));
    }
    
    @Test
    public void runWorksRightWithOption() {
        String[] args = {"EasterEgg", "-a"};
        app.run(args);
        assertTrue(os.toString().contains("Let's run easter egg with -a"));
    }

}
