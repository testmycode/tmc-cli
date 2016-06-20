package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ListCoursesCommandTest {

    Application app;
    Io mockIo;
    TestIo testIo;
    TmcCore mockCore;

    @Before
    public void setUp() {
        mockIo = mock(TerminalIo.class);
        app = new Application(mockIo);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }
    
    @Test
    public void failIfCoreIsNull() {
        testIo = new TestIo();
        app = new Application(testIo);
        app.setTmcCore(null);
        String[] args = {"courses", "foo"};
        app.run(args);
        assertFalse(testIo.out().contains("Course doesn't exist"));
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
        verify(mockIo).println(Mockito.contains("No courses found on this server"));
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
        verify(mockIo).println(Mockito.contains("Found 2 courses"));
    }
}
