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
        this.app.run(args);
        assertTrue(testIo.printedText.contains("TMC-CLI version"));
    }
    
    @Test
    public void helpWorksWithRightParameter() {
        String[] args = {"-h"};
        this.app.run(args);
        assertTrue(testIo.printedText.contains("Usage: tmc-cli [args] COMMAND [command-args]"));
    }
    
    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        this.app.run(args);
        assertTrue(testIo.printedText.contains("Command foo doesn't exist"));
    }
}
