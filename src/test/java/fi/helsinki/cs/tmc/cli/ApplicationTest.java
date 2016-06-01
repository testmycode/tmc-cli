package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest {

    private Application app;
    private TestIo testIo;

    @Before
    public void setUp() {
        testIo = new TestIo();
        app = new Application(testIo);
    }

    @Test
    public void versionWorksWithRightParameter() {
        String[] args = {"-v"};
        app.run(args);
        assertTrue(testIo.printedText.contains("TMC-CLI version"));
    }
    
    @Test
    public void helpWorksWithRightParameter() {
        String[] args = {"-h"};
        app.run(args);
        assertTrue(testIo.printedText.contains("Usage: tmc"));
    }

    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        app.run(args);
        assertTrue(testIo.printedText.contains("Command foo doesn't exist"));
    }
}
