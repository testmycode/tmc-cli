package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import fi.helsinki.cs.tmc.cli.Application;

import fi.helsinki.cs.tmc.core.domain.Course;

import org.junit.Before;
import org.junit.Test;

public class TmcUtilTest {
    Application app;

    @Before
    public void setUp() {
        app = new Application();
        app.createTmcCore(new Settings());
    }

    @Test
    public void findCouseIfItExists() {
        Course course;
        app.createTmcCore(new Settings(true));
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
    }

    @Test
    public void returnNullIfCourseWontExist() {
        Course course;
        course = TmcUtil.findCourse(app.getTmcCore(), "afuwhf");
        assertNull(course);
    }
}
