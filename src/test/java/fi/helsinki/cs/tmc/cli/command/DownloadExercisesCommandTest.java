package fi.helsinki.cs.tmc.cli.command;


import static fi.helsinki.cs.tmc.cli.command.ListExercisesCommandTest.pathToDummyCourse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DownloadExercisesCommandTest {

    static Path pathToDummyCourse;
    private static final String COURSE_NAME = "2016-aalto-c";
    
    Application app;
    TestIo testIo;
    TmcCore mockCore;
    Path tempDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);
    }
    
    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("downloadTest");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        testIo = new TestIo();
        app = new Application(testIo, workDir);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) {
        }
    }

    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"download", "foo"};
        app.run(args);
        assertFalse(testIo.out().contains("Course doesn't exist"));
    }

    @Test
    public void failIfCourseArgumentNotGiven() {
        String[] args = {"download"};
        app.run(args);
        assertTrue(testIo.out().contains("You must give"));
    }
    
    @Test
    public void canNotDownloadIfInsideCourseDirectory() {
        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"download", "foo"};
        app.run(args);
        assertTrue(testIo.out().contains("Can't download a course inside a course directory."));
    }

    @Test
    public void worksRightIfCourseIsNotFound() throws IOException {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return new ArrayList<>();
            }
        };

        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
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

        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);
        when(mockCore.downloadOrUpdateExercises(any(ProgressObserver.class),
                anyListOf(Exercise.class))).thenReturn(callableExercise);

        String[] args = {"download", "course1"};
        app.run(args);

        File courseJson = tempDir.resolve("course1").resolve(".tmc.json").toFile();
        assertTrue(courseJson.exists());
    }
    
    @Test
    public void worksRightIfCourseIsFoundWithNoExercises() throws IOException {
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
                course.setExercises(lst);
                return course;
            }
        };

        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
            @Override
            public List<Exercise> call() throws Exception {
                ArrayList<Exercise> tmp = new ArrayList<>();
                return tmp;
            }
        };

        Settings settings = new Settings("server", "user", "password");
        settings.setTmcProjectDirectory(tempDir);
        app.setSettings(settings);

        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);
        when(mockCore.downloadOrUpdateExercises(any(ProgressObserver.class),
                anyListOf(Exercise.class))).thenReturn(callableExercise);

        String[] args = {"download", "course1"};
        app.run(args);
        assertTrue(testIo.out().contains("You may have already downloaded the exercises."));
    }

    @Test
    public void filtersCompletedExercisesByDefault() throws ParseException {
        Exercise notCompleted = new Exercise("not-completed");
        Exercise completed1 = new Exercise("completed1");
        Exercise completed2 = new Exercise("completed2");

        notCompleted.setCompleted(false);
        completed1.setCompleted(true);
        completed2.setCompleted(true);

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(completed1);
        exercises.add(notCompleted);
        exercises.add(completed2);

        Course course = new Course("test-course");
        course.setExercises(exercises);

        GnuParser parser = new GnuParser();
        CommandLine args = parser.parse(new Options(), new String[]{});

        DownloadExercisesCommand dlCommand = new DownloadExercisesCommand();
        List<Exercise> filtered = dlCommand.getFilteredExercises(course, args);

        assertTrue(filtered.size() == 1);
        assertEquals(notCompleted, filtered.get(0));
    }

    @Test
    public void getsAllExercisesWithAllSwitch() throws ParseException {
        Exercise notCompleted = new Exercise("not-completed");
        Exercise completed1 = new Exercise("completed1");
        Exercise completed2 = new Exercise("completed2");

        notCompleted.setCompleted(false);
        completed1.setCompleted(true);
        completed2.setCompleted(true);

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(completed1);
        exercises.add(notCompleted);
        exercises.add(completed2);

        Course course = new Course("test-course");
        course.setExercises(exercises);

        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("a", "all", false, "");
        CommandLine args = parser.parse(options, new String[]{"-a"});

        DownloadExercisesCommand dlCommand = new DownloadExercisesCommand();
        List<Exercise> filtered = dlCommand.getFilteredExercises(course, args);

        assertTrue(filtered.size() == 3);
    }
}
