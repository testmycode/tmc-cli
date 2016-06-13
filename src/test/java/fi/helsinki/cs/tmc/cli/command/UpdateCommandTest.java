package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class UpdateCommandTest {
    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;


    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }

    @Test
    public void printsAnErrorMessageIfGivenCourseName() {
        String[] args = {"update", "course"};
        app.run(args);
        assertTrue(io.getPrint().contains("Use in the course directory"));
    }

    @Test
    public void printsAnErrorMessageIfUsedOutsideCourseDirectory() {
        workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        app.setWorkdir(workDir);
        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("Not a course directory"));
    }

    @Test
    public void worksRightIfAllExercisesAreUpToDate() {
        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
            @Override
            public List<Exercise> call() throws Exception {
                ArrayList<Exercise> tmp = new ArrayList<>();
                return tmp;
            }
        };

        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(callableExercise);

        String pathToDummycourse = UpdateCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c").getPath();

        workDir = new WorkDir(Paths.get(pathToDummycourse));
        app.setWorkdir(workDir);

        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("All exercises are up-to-date"));
    }

    @Test
    public void worksRightIfUpdatesAvailable() {
        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
            @Override
            public List<Exercise> call() throws Exception {
                ArrayList<Exercise> tmp = new ArrayList<>();
                tmp.add(new Exercise("exercise1"));
                tmp.add(new Exercise("exercise2"));
                return tmp;
            }
        };

        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(callableExercise);

        String pathToDummycourse = UpdateCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c").getPath();

        workDir = new WorkDir(Paths.get(pathToDummycourse));
        app.setWorkdir(workDir);

        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("Updates available for:"));
        assertTrue(io.getPrint().contains("exercise1"));
        assertTrue(io.getPrint().contains("exercise2"));
    }
}
