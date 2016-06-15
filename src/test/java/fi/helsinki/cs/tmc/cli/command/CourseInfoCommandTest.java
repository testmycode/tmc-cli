package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class CourseInfoCommandTest {
    
    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private Course course;
    private Callable<List<Course>> callableList;
    private Callable<Course> callableCourse;
    
    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
        
        course = new Course("test-course123");
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("exercise10"));
        exercises.get(0).setCompleted(true);
        course.setExercises(exercises);
        
        callableList = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(course);
            }
        };
        
        callableCourse = new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                return course;
            }
        };
    }
    
    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"info"};
        app.run(args);
        assertFalse(io.getPrint().contains("You must give"));
    }
    
    @Test
    public void showMessageIfCourseIsNotGiven() {
        String[] args = {"info"};
        app.run(args);
        assertTrue(io.getPrint().contains("You must give the course name as a parameter"));
    }
    
    @Test
    public void showMessageIfCourseDoesNotExist() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        String[] args = {"info", "foo"};
        app.run(args);
        assertTrue(io.getPrint().contains("The course foo doesn't exist on this server"));
    }
    
    @Test
    public void printCourseWithoutOption() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);
        
        String[] args = {"info", "test-course123"};
        app.run(args);
        assertTrue(io.getPrint().contains("Course name: test-course123"));
        assertFalse(io.getPrint().contains("Statistics URLs"));
    }
    
    @Test
    public void printCourseWithOption() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);
        
        String[] args = {"info", "test-course123", "-a"};
        app.run(args);
        assertTrue(io.getPrint().contains("Statistics URLs"));
    }
    
    @Test
    public void printCourseWithNoExercises() {
        course.setExercises(new ArrayList<Exercise>());
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableList);
        when(mockCore.getCourseDetails(any(ProgressObserver.class),
                any(Course.class))).thenReturn(callableCourse);
        
        String[] args = {"info", "test-course123"};
        app.run(args);
        assertTrue(io.getPrint().contains("Exercises: -"));
    }

}
