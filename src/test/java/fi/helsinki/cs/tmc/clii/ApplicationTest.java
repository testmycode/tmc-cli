package fi.helsinki.cs.tmc.clii;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ApplicationTest {

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
    public void versionWorksWithRightParameter() {
        String[] args = {"-v"};
        app.run(args);
        assertTrue(os.toString().contains("TMC-CLI version"));
    }
    
    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        app.run(args);
        assertTrue(os.toString().contains("Command foo doesn't exist"));
    }
}
