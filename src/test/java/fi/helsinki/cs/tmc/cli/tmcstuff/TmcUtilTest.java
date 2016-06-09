package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;


public class TmcUtilTest {

    static Path workDir;
    Application app;
    TestIo testio;
    TmcCore mockCore;

    @Before
    public void setUp() {
        testio = new TestIo();
        app = new Application(testio);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        doAnswer(new Answer<Callable<Course>>() {
            @Override
            public Callable<Course> answer(InvocationOnMock invocation) throws Throwable {
                final Course course = (Course)invocation.getArguments()[1];
                return new Callable<Course>() {
                    @Override
                    public Course call() throws Exception {
                        return course;
                    }
                };
            }
        }).when(mockCore).getCourseDetails(any(ProgressObserver.class), any(Course.class));
    }

    @Test
    public void findCourseIfItExists() {
        final Course course = new Course("right-course");
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(course, new Course("another"));
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        assertEquals(course, TmcUtil.findCourse(app.getTmcCore(), "right-course"));
    }

    @Test
    public void returnNullIfCourseWontExist() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(new Course("course"), new Course("another"));
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        assertNull(TmcUtil.findCourse(app.getTmcCore(), "not-existing-course"));
    }

    @Test
    public void findExerciseOfCourse() {
        final Course c = new Course("right-course");
        Exercise exercise = new Exercise("second");
        c.setExercises(Arrays.asList(new Exercise("first"), exercise));
        Exercise ex = TmcUtil.findExercise(c, "second");
        assertEquals(exercise, ex);
    }

    @Test
    public void returnNullIfExerciseWontExist() {
        Course course = new Course("right-course");
        course.setExercises(Arrays.asList(new Exercise("first"), new Exercise("second")));
        assertNull(TmcUtil.findExercise(course, "not-existing-exercise"));
    }

    @Test
    public void downloadAllExercises() {
        String name = "right-course";
        Course course = new Course(name);
        course.setExercises(Arrays.asList(new Exercise("first"), new Exercise("second")));
        course.setExercisesLoaded(true);

        doAnswer(new Answer<Callable<List<Exercise>>>() {
            @Override
            public Callable<List<Exercise>> answer(InvocationOnMock invocation) throws Throwable {
                final List<Exercise> exersices = (List<Exercise>)invocation.getArguments()[1];
                return new Callable<List<Exercise>>() {
                    @Override
                    public List<Exercise> call() throws Exception {
                        return exersices;
                    }
                };
            }
        }).when(mockCore).downloadOrUpdateExercises(any(ProgressObserver.class), any(List.class));
        List<Exercise> list = TmcUtil.downloadAllExercises(app.getTmcCore(), course);
        for (Exercise exercise : list) {
            assertNotNull(TmcUtil.findExercise(course, exercise.getName()));
        }
    }

    @Test
    public void downloadSomeExercises() {
        List<Exercise> list = Arrays.asList(new Exercise("first"), new Exercise("second"));

        doAnswer(new Answer<Callable<List<Exercise>>>() {
            @Override
            public Callable<List<Exercise>> answer(InvocationOnMock invocation) throws Throwable {
                final List<Exercise> exersices = (List<Exercise>)invocation.getArguments()[1];
                return new Callable<List<Exercise>>() {
                    @Override
                    public List<Exercise> call() throws Exception {
                        return exersices;
                    }
                };
            }
        }).when(mockCore).downloadOrUpdateExercises(any(ProgressObserver.class), any(List.class));
        List<Exercise> downloadList = TmcUtil.downloadExercises(app.getTmcCore(), list);
        for (Exercise exercise : downloadList) {
            boolean found = false;
            for (Exercise exercise2 : list) {
                if (exercise.getName().equals(exercise2.getName())) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }
}
