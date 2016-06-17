package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExerciseUpdater;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UpdateCommand.class)
public class UpdateCommandTest {

    private static final String COURSE_NAME = "2016-aalto-c";
    private static final String EXERCISE1_NAME = "Module_1-02_intro";
    private static final String EXERCISE2_NAME = "Module_1-04_func";

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;
    static Path pathToDummyExerciseSrc;
    static Path pathToNonCourseDir;

    TestIo io;
    Application app;
    TmcCore mockCore;
    ExerciseUpdater exerciseUpdater;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        pathToDummyExercise = pathToDummyCourse.resolve(EXERCISE1_NAME);
        assertNotNull(pathToDummyExercise);

        pathToDummyExerciseSrc = pathToDummyExercise.resolve("src");
        assertNotNull(pathToDummyExerciseSrc);

        pathToNonCourseDir = pathToDummyCourse.getParent();
        assertNotNull(pathToNonCourseDir);
    }

    @Before
    public void setUp() throws Exception {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        exerciseUpdater = PowerMockito.mock(ExerciseUpdater.class);
        PowerMockito.whenNew(ExerciseUpdater.class).withAnyArguments().thenReturn(exerciseUpdater);
    }

    @Test
    public void failIfCoreIsNull() {
        app.setWorkdir(new WorkDir(pathToNonCourseDir));
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
        assertThat(io.out(), containsString("Use in the course directory"));
    }

    @Test
    public void printsAnErrorMessageIfUsedOutsideCourseDirectory() {
        app.setWorkdir(new WorkDir(pathToNonCourseDir));
        String[] args = {"update"};
        app.run(args);
        assertThat(io.out(), containsString("Not a course directory"));
    }

    @Test
    public void worksRightIfAllExercisesAreUpToDate() {
        when(exerciseUpdater.updatesAvailable()).thenReturn(false);

        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"update"};
        app.run(args);

        assertThat(io.out(), containsString("All exercises are up-to-date"));
    }

    @Test
    public void updatesNewAndChangedExercises() {
        when(exerciseUpdater.updatesAvailable()).thenReturn(true);

        String newExerciseName = "new_exercise";
        Exercise newExercise = new Exercise(newExerciseName, COURSE_NAME);
        List<Exercise> newExercises = new ArrayList<>();
        newExercises.add(newExercise);

        String changedExerciseName = EXERCISE1_NAME;
        Exercise changedExercise = new Exercise(changedExerciseName, COURSE_NAME);
        List<Exercise> changedExercises = new ArrayList<>();
        changedExercises.add(changedExercise);

        when(exerciseUpdater.getNewExercises()).thenReturn(newExercises);
        when(exerciseUpdater.getUpdatedExercises()).thenReturn(changedExercises);

        List<Exercise> newAndChanged = new ArrayList<>();
        newAndChanged.addAll(newExercises);
        newAndChanged.addAll(changedExercises);
        when(exerciseUpdater.downloadUpdates(any(TmcCliProgressObserver.class)))
                .thenReturn(newAndChanged);
        when(exerciseUpdater.updateCourseJson(any(CourseInfo.class), any(Path.class)))
                .thenReturn(true);
        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"update"};
        app.run(args);

        assertThat(io.out(), containsString("New exercises:"));
        assertThat(io.out(), containsString(newExerciseName));

        assertThat(io.out(), containsString("Modified exercises:"));
        assertThat(io.out(), containsString(changedExerciseName));

        verify(exerciseUpdater).updateCourseJson(any(CourseInfo.class), any(Path.class));
    }
}
