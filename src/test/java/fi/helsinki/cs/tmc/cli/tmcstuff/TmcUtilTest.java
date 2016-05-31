package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TmcUtilTest {

    Application app;

    @Before
    public void setUp() {
        app = new Application(new TerminalIo());
        app.createTmcCore(new Settings(true));
        try {
            FileUtils.deleteDirectory(new File("cert-test"));
        } catch (IOException e) {
        }
    }

    @Test
    public void findCourseIfItExists() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
    }

    @Test
    public void returnNullIfCourseWontExist() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "afuwhf");
        assertNull(course);
    }

    @Test
    public void findExerciseIfItExists() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
        TmcUtil.findExercise(course, "viikko1-002.HeiMaailma");
    }

    @Test
    public void returnNullIfExerciseWontExist() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
        TmcUtil.findExercise(course, "xhu4ew");
    }

    @Test
    public void downloadExercisesWorks() {
        Course course;
        String name = "cert-test";
        course = TmcUtil.findCourse(app.getTmcCore(), name);
        assertNotNull(course);
        TmcUtil.downloadAllExercises(app.getTmcCore(), course);

        boolean exists = Files.exists(Paths.get("./" + name));
        assertTrue(exists);
        try {
            FileUtils.deleteDirectory(new File(name));
        } catch (IOException e) {
        }
    }
}
