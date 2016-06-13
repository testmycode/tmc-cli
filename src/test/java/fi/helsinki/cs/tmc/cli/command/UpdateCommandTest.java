package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class UpdateCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";

    static Path pathToDummyCourse;

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }

    @Test
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"update"};
        app.run(args);
        assertFalse(io.getPrint().contains("Not a course directory"));
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

        workDir = new WorkDir(pathToDummyCourse);
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

        workDir = new WorkDir(pathToDummyCourse);
        app.setWorkdir(workDir);

        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("Updates available for:"));
        assertTrue(io.getPrint().contains("exercise1"));
        assertTrue(io.getPrint().contains("exercise2"));
    }
}
