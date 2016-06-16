package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
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
@PrepareForTest({TmcUtil.class, CourseInfoIo.class})
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

    UpdateResult mockUpdateResult;

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
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        PowerMockito.mockStatic(TmcUtil.class);
        PowerMockito.mockStatic(CourseInfoIo.class);
        when(CourseInfoIo.load(any(Path.class))).thenCallRealMethod();
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
        mockUpdateResult = mock(UpdateResult.class);
        when(mockUpdateResult.getNewExercises()).thenReturn(new ArrayList<Exercise>());
        when(mockUpdateResult.getUpdatedExercises()).thenReturn(new ArrayList<Exercise>());
        when(TmcUtil.getUpdatableExercises(any(TmcCore.class), any(Course.class)))
                .thenReturn(mockUpdateResult);

        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"update"};
        app.run(args);

        assertThat(io.out(), containsString("All exercises are up-to-date"));

        verifyStatic(times(0));
        TmcUtil.downloadExercises(any(TmcCore.class), any(List.class));
    }

    @Test
    public void updateIfNewExercisesAvailable() {
        String newExerciseName = "new_exercise";
        ArrayList<Exercise> newExercises = new ArrayList<>();
        newExercises.add(new Exercise(newExerciseName, COURSE_NAME));

        mockUpdateResult = mock(UpdateResult.class);
        when(mockUpdateResult.getNewExercises()).thenReturn(newExercises);
        when(mockUpdateResult.getUpdatedExercises()).thenReturn(new ArrayList<Exercise>());
        when(TmcUtil.getUpdatableExercises(any(TmcCore.class), any(Course.class)))
                .thenReturn(mockUpdateResult);

        when(TmcUtil.downloadExercises(any(TmcCore.class), any(List.class)))
                .thenReturn(newExercises);

        Course foundCourse = new Course(COURSE_NAME);
        ArrayList<Exercise> allAvailableExercises = new ArrayList<>();
        allAvailableExercises.add(new Exercise(newExerciseName, COURSE_NAME));
        allAvailableExercises.add(new Exercise(EXERCISE1_NAME, COURSE_NAME));
        allAvailableExercises.add(new Exercise(EXERCISE2_NAME, COURSE_NAME));
        foundCourse.setExercises(allAvailableExercises);
        when(TmcUtil.findCourse(mockCore, COURSE_NAME)).thenReturn(foundCourse);

        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"update"};
        app.run(args);

        assertThat(io.out(), containsString("New exercises:"));
        assertThat(io.out(), containsString(newExerciseName));

        verifyStatic(times(1));
        CourseInfoIo.save(any(CourseInfo.class), any(Path.class));
    }

    @Test
    public void updateIfExerciseHasBeenChanged() {
        String changedExercise = EXERCISE1_NAME;
        ArrayList<Exercise> changedExercises = new ArrayList<>();
        changedExercises.add(new Exercise(changedExercise, COURSE_NAME));

        mockUpdateResult = mock(UpdateResult.class);
        when(mockUpdateResult.getNewExercises()).thenReturn(new ArrayList<Exercise>());
        when(mockUpdateResult.getUpdatedExercises()).thenReturn(changedExercises);
        when(TmcUtil.getUpdatableExercises(any(TmcCore.class), any(Course.class)))
                .thenReturn(mockUpdateResult);

        when(TmcUtil.downloadExercises(any(TmcCore.class), any(List.class)))
                .thenReturn(changedExercises);

        Course foundCourse = new Course(COURSE_NAME);
        ArrayList<Exercise> allAvailableExercises = new ArrayList<>();
        allAvailableExercises.add(new Exercise(EXERCISE1_NAME, COURSE_NAME));
        allAvailableExercises.add(new Exercise(EXERCISE2_NAME, COURSE_NAME));
        foundCourse.setExercises(allAvailableExercises);
        when(TmcUtil.findCourse(mockCore, COURSE_NAME)).thenReturn(foundCourse);

        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"update"};
        app.run(args);

        assertThat(io.out(), containsString("Modified exercises:"));
        assertThat(io.out(), containsString(changedExercise));

        verifyStatic(times(1));
        CourseInfoIo.save(any(CourseInfo.class), any(Path.class));
    }
}
