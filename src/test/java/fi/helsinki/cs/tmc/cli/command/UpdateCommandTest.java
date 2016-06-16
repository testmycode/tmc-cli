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
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class UpdateCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";

    static Path pathToDummyCourse;
    static Path tempDir;

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        tempDir = Paths.get(System.getProperty("java.io.tmpdir"))
                .resolve("updateCommandTests");
        assertNotNull(tempDir);
    }

    @Before
    public void setUp() throws IOException {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        FileUtils.copyDirectory(pathToDummyCourse.toFile(), tempDir.toFile());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir.toFile());
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
//        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
//            @Override
//            public List<Exercise> call() throws Exception {
//                ArrayList<Exercise> tmp = new ArrayList<>();
//                return tmp;
//            }
//        };
//
//        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
//                .thenReturn(callableExercise);

        Callable<GetUpdatableExercises.UpdateResult> callableResult
                = new Callable<GetUpdatableExercises.UpdateResult>() {

                    @Override
                    public GetUpdatableExercises.UpdateResult call() throws Exception {
                        GetUpdatableExercises.UpdateResult result = mock(
                                GetUpdatableExercises.UpdateResult.class);
                        when(result.getNewExercises()).thenReturn(
                                new ArrayList<Exercise>());
                        when(result.getUpdatedExercises()).thenReturn(
                                new ArrayList<Exercise>());
                        return result;
                    }
                };

        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(callableResult);

        workDir = new WorkDir(tempDir);
        app.setWorkdir(workDir);

        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("All exercises are up-to-date"));
    }

    @Test
    @Ignore
    public void worksRightIfUpdatesAvailable() {
//        Callable<List<Exercise>> callableExercise = new Callable<List<Exercise>>() {
//            @Override
//            public List<Exercise> call() throws Exception {
//                ArrayList<Exercise> tmp = new ArrayList<>();
//                tmp.add(new Exercise("exercise1"));
//                tmp.add(new Exercise("exercise2"));
//                return tmp;
//            }
//        };
//
//        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
//                .thenReturn(callableExercise);

        final List<Exercise> unlockedList = new ArrayList<>();
        unlockedList.add(new Exercise("unlocked_exercise"));

        final List<Exercise> changedList = new ArrayList<>();
        unlockedList.add(new Exercise("Module_1-02_intro"));

        Callable<GetUpdatableExercises.UpdateResult> callableResult
                = new Callable<GetUpdatableExercises.UpdateResult>() {
                    @Override
                    public GetUpdatableExercises.UpdateResult call() throws Exception {
                        GetUpdatableExercises.UpdateResult result = mock(
                                GetUpdatableExercises.UpdateResult.class);
                        when(result.getNewExercises()).thenReturn(unlockedList);
                        when(result.getUpdatedExercises()).thenReturn(changedList);
                        return result;
                    }
                };
        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(callableResult);

        Callable<List<Course>> callableCourseList = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return new ArrayList<>();
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callableCourseList);

        Callable<Course> callableCourse = new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                Course course = new Course("2016-aalto-c");
                List<Exercise> exercises = new ArrayList<>();
                exercises.add(new Exercise("Module_1-02_intro"));
                exercises.add(new Exercise("unlocked_course"));
                course.setExercises(exercises);
                return course;
            }
        };

        when(mockCore.getCourseDetails(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(callableCourse);

        workDir = new WorkDir(tempDir);
        app.setWorkdir(workDir);

        String[] args = {"update"};
        app.run(args);
        assertTrue(io.getPrint().contains("New exercises:"));
        assertTrue(io.getPrint().contains("unlocked_exercise"));

        //assertTrue(io.getPrint().contains("Modified exercises:"));
        //assertTrue(io.getPrint().contains("Module_1-02_intro"));
    }
}
