package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ListExercisesCommandTest {
    
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
    public void runWorksRight() {
        String[] args = {"list-exercises", "demo"};
        app.run(args);
        assertTrue(os.toString().contains("HeiMaailma"));
    }
}
