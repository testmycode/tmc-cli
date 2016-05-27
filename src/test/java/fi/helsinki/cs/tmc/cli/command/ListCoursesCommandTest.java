package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ListCoursesCommandTest {

    Application app;
    OutputStream os;

    @Before
    public void setUp() {
        app = new Application();
        app.createTmcCore(new Settings(true));

        os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);
    }

    @Test
    public void normalCourseListingWorksRight() {
        String[] args = {"list-courses"};
        app.run(args);
        assertTrue(os.toString().contains("demo"));
    }
}
