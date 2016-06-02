package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadExercisesCommandTest {

    Application app;
    TestIo testIo;

    @Before
    public void setUp() {
        testIo = new TestIo();
        app = new Application(testIo);
        app.createTmcCore(new Settings(true));
    }

    @Test
    public void failIfCourseArgumentNotGiven() {
        String[] args = {"download"};
        app.run(args);
        assertTrue(testIo.getPrint().contains("You must give"));
    }

    @Test
    public void downloadWorks() throws IOException {
        String course = "cert-test";
        String[] args = {"download", course};
        app.run(args);

        assertTrue(Files.exists(Paths.get(System.getProperty("user.dir")).resolve(course)));
        try {
            FileUtils.deleteDirectory(new File(course));
        } catch (Exception e) { }
    }
}
