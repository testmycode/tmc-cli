package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ListCoursesCommandTest {

    Application app;
    TestIo io;
    TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }
    
    @Test
    public void failIfCoreIsNull() {
        TestIo io = new TestIo();
        app = new Application(io);
        app.setTmcCore(null);
        String[] args = {"courses", "foo"};
        app.run(args);
        io.assertNotContains("Course doesn't exist");
    }
    
    @Test
    public void listCoursesWorksWithNoCourses() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return new ArrayList<>();
            }
        };
        
        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callable);
        String[] args = {"courses"};
        app.run(args);
        io.assertContains("No courses found on this server");
    }
    
    @Test
    public void listCoursesWorksWithCourses() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                ArrayList<Course> tmp = new ArrayList<>();
                tmp.add(new Course("course1"));
                tmp.add(new Course("course2"));
                return tmp;
            }
        };
        
        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callable);
        String[] args = {"courses"};
        app.run(args);
        io.assertContains("Found 2 courses");
    }
}
