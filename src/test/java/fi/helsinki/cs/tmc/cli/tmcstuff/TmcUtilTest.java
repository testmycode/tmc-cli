package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.domain.Course;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TmcUtilTest {

    Application app;
    TestIo testio;

    @Before
    public void setUp() {
        testio = new TestIo();
        app = new Application(testio);
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
        Exercise ex = TmcUtil.findExercise(course, "viikko1-002.HeiMaailma");
        assertNotNull(ex);
        assertEquals("viikko1-002.HeiMaailma", ex.getName());
    }

    @Test
    public void returnNullIfExerciseWontExist() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
        Exercise ex = TmcUtil.findExercise(course, "xhu4ew");
        assertNull(ex);
    }

    @Test
    public void downloadExercisesWorks() {
        Course course;
        String name = "cert-test";
        course = TmcUtil.findCourse(app.getTmcCore(), name);
        assertNotNull(course);
        TmcUtil.downloadAllExercises(app.getTmcCore(), course);

        boolean exists = Files.exists(Paths.get(System.getProperty("user.dir")).resolve(name));
        assertTrue(exists);
        try {
            FileUtils.deleteDirectory(new File(name));
        } catch (IOException e) {
        }
    }
}
