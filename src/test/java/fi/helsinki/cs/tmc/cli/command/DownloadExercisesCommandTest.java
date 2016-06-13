package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DownloadExercisesCommandTest {

    Application app;
    TestIo testIo;
    TmcCore mockCore;
    Io mockIo;
    Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("downloadTest");
        WorkDir workDir = new WorkDir(tempDir);
        testIo = new TestIo();
        app = new Application(testIo, workDir);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }
    
    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) { }
    }
    
    @Test
    public void failIfCoreIsNull() {
        app.setTmcCore(null);
        String[] args = {"download", "foo"};
        app.run(args);
        assertFalse(testIo.getPrint().contains("Course doesn't exist"));
    }

    @Test
    public void failIfCourseArgumentNotGiven() {
        String[] args = {"download"};
        app.run(args);
        assertTrue(testIo.getPrint().contains("You must give"));
    }

    @Test
    public void worksRightIfCourseIsNotFound() throws IOException {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return new ArrayList<>();
            }
        };

        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callable);
        String[] args = {"download", "foo"};
        app.run(args);
        assertTrue(testIo.out().contains("Course doesn't exist"));
    }

    @Test
    public void worksRightIfCourseIsFound() throws IOException {
        Callable<List<Course>> callableList = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                ArrayList<Course> tmp = new ArrayList<>();
                tmp.add(new Course("course1"));
                tmp.add(new Course("course2"));
                return tmp;
            }
        };
        
        Callable<Course> callableCourse = new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                Course course = new Course("course1");
                List<Exercise> lst = new ArrayList<>();
                lst.add(new Exercise("exercise"));
                course.setExercises(lst);
                return course;
            }
        };
        
        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
            @Override
            public List<Exercise> call() throws Exception {
                ArrayList<Exercise> tmp = new ArrayList<>();
                tmp.add(new Exercise("exerciseName"));
                return tmp;
            }
        };
        
        Settings settings = new Settings("server", "user", "password");
        settings.setTmcProjectDirectory(tempDir);
        app.setSettings(settings);
        
        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callableList);
        when(mockCore.getCourseDetails((ProgressObserver) anyObject(),
                (Course) anyObject())).thenReturn(callableCourse);
        when(mockCore.downloadOrUpdateExercises((ProgressObserver) anyObject(),
                (List<Exercise>) anyObject())).thenReturn(callableExercise);
        
        String[] args = {"download", "course1"};
        app.run(args);
        assertTrue(testIo.out().contains("exerciseName"));
    }
}
