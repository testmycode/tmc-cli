package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ApplicationTest {
    @Test
    public void testThatProgramWontCrashWithEmptyArguments() {
        Application app = new Application();
        app.run(new String[]{});
    }
}
