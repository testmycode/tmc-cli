package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
    }

    @Test
    public void versionWorksWithRightParameter() {
        String[] args = {"-v"};
        app.run(args);
        assertTrue(io.out().contains("TMC-CLI version"));
    }

    @Test
    public void failWhenInvalidOption() {
        String[] args = {"-a34t3"};
        app.run(args);
        assertTrue(io.out().contains("Unrecognized option"));
    }

    @Test
    public void helpWorksWithRightParameter() {
        String[] args = {"-h"};
        app.run(args);
        assertTrue(io.out().contains("Usage: tmc"));
    }

    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        app.run(args);
        assertTrue(io.out().contains("Command foo doesn't exist"));
    }
}
