package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class DownloadExercisesCommandTest {

    Application app;
    TestIo io;
    TmcCore mockCore;
    Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("downloadTest");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        io = new TestIo();
        app = new Application(io, workDir);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        mockStatic(TmcUtil.class);
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) { }
    }

    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"download", "foo"};
        app.run(args);
        io.assertNotContains("Course doesn't exist");
    }

    @Test
    public void failIfCourseArgumentNotGiven() {
        String[] args = {"download"};
        app.run(args);
        io.assertContains("You must give");
    }

    @Test
    public void worksRightIfCourseIsNotFound() throws IOException {
        when(TmcUtil.findCourse(eq(mockCore), eq("foo"))).thenReturn(null);
        String[] args = {"download", "foo"};
        app.run(args);
        io.assertContains("Course doesn't exist");
    }

    @Test
    public void worksRightIfCourseIsFound() throws IOException {
        Course course = new Course("course1");
        course.setExercises(Arrays.asList(new Exercise("exercise")));
        List<Exercise> exercises = Arrays.asList(new Exercise("exerciseName"));

        when(TmcUtil.findCourse(eq(mockCore), eq("course1"))).thenReturn(course);
        when(TmcUtil.downloadExercises(eq(mockCore), anyListOf(Exercise.class),
                any(ProgressObserver.class))).thenReturn(exercises);

        Settings settings = new Settings("server", "user", "password");
        settings.setTmcProjectDirectory(tempDir);
        app.setSettings(settings);

        String[] args = {"download", "course1"};
        app.run(args);

        File courseJson = tempDir.resolve("course1").resolve(".tmc.json").toFile();
        assertTrue(courseJson.exists());
    }

    @Test
    public void filtersCompletedExercisesByDefault() throws ParseException {
        Exercise notCompleted = new Exercise("not-completed");
        Exercise completed1 = new Exercise("completed1");
        Exercise completed2 = new Exercise("completed2");

        notCompleted.setCompleted(false);
        completed1.setCompleted(true);
        completed2.setCompleted(true);

        List<Exercise> filteredExercises = Arrays.asList(notCompleted);

        Course course = new Course("course1");
        course.setExercises(Arrays.asList(completed1, notCompleted, completed2));

        when(TmcUtil.findCourse(eq(mockCore), eq("course1"))).thenReturn(course);
        when(TmcUtil.downloadExercises(eq(mockCore), anyListOf(Exercise.class),
                any(ProgressObserver.class))).thenReturn(filteredExercises);

        Settings settings = new Settings("server", "user", "password");
        settings.setTmcProjectDirectory(tempDir);
        app.setSettings(settings);

        String[] args = {"download", "course1"};
        app.run(args);

        io.assertContains("which 1 exercises were downloaded");
    }

    @Test
    public void getsAllExercisesWithAllSwitch() throws ParseException {
        Exercise notCompleted = new Exercise("not-completed");
        Exercise completed1 = new Exercise("completed1");
        Exercise completed2 = new Exercise("completed2");

        notCompleted.setCompleted(false);
        completed1.setCompleted(true);
        completed2.setCompleted(true);

        List<Exercise> exercises = Arrays.asList(completed1, notCompleted,
                completed2);
        Course course = new Course("course1");
        course.setExercises(exercises);

        when(TmcUtil.findCourse(eq(mockCore), eq("course1"))).thenReturn(course);
        when(TmcUtil.downloadExercises(eq(mockCore), anyListOf(Exercise.class),
                any(ProgressObserver.class))).thenReturn(exercises);

        Settings settings = new Settings("server", "user", "password");
        settings.setTmcProjectDirectory(tempDir);
        app.setSettings(settings);

        String[] args = {"download", "-a", "course1"};
        app.run(args);

        io.assertContains("which 3 exercises were downloaded");
    }
}
