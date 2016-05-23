package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import fi.helsinki.cs.tmc.cli.Application;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class TmcUtilTest {

    @Test
    public void findCouseIfItExists() {
        Application app;
        Course course;

        app = new Application();
        course = TmcUtil.findCourse(app.getTmcCore(), "demo");
        assertNotNull(course);
    }

    @Test
    public void returnNullIfCourseWontExist() {
        Application app;
        Course course;

        app = new Application();
        course = TmcUtil.findCourse(app.getTmcCore(), "afuwhf");
        assertNull(course);
    }
}
