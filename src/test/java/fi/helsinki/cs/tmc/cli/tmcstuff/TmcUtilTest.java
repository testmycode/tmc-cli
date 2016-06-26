package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RunResult.class)
public class TmcUtilTest {

    Application app;
    TestIo io;
    TmcCore mockCore;
    static Path workDir;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        Answer<Callable<Course>> answer = new Answer<Callable<Course>>() {
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
        };
        when(mockCore.getCourseDetails(any(ProgressObserver.class), any(Course.class)))
                .then(answer);
    }

    public <T> Callable<T> createReturningCallback(final T result) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                return result;
            }
        };
    }

    public Callable createThrowingCallback(final String errorMsg) {
        return new Callable() {
            @Override
            public Object call() throws Exception {
                throw new Exception(errorMsg);
            }
        };
    }

    @Test
    public void failToLogin() throws URISyntaxException {
        when(mockCore.listCourses(any(ProgressObserver.class)))
                .thenReturn(createThrowingCallback("failed"));
        boolean fail = true;
        try {
            assertFalse(TmcUtil.tryToLogin(mockCore));
        } catch (Exception e) {
            assertEquals(Exception.class, e.getClass());
            assertEquals("failed", e.getMessage());
            fail = false;
        }
        if (fail) {
            fail("Login should throw the exception");
        }
    }

    @Test
    public void listCourses() {
        List<Course> expectedResult = Arrays.asList(new Course("test-course"),
                new Course("test-course2"));
        when(mockCore.listCourses(any(ProgressObserver.class)))
                .thenReturn(createReturningCallback(expectedResult));

        List<Course> result = TmcUtil.listCourses(mockCore);
        assertEquals(expectedResult, result);
    }

    @Test
    public void findCourseWhenItExists() {
        Course expectedResult = new Course("test-course");
        List<Course> courses = Arrays.asList(new Course("test-course"));

        when(mockCore.listCourses(any(ProgressObserver.class)))
                .thenReturn(createReturningCallback(courses));

        Course result = TmcUtil.findCourse(mockCore, "test-course");
        assertEquals(expectedResult, result);
    }

    @Test
    public void returnNullWhenCourseWontExist() {
        List<Course> courses = Arrays.asList(new Course("course"),
                new Course("another"));
        when(mockCore.listCourses(any(ProgressObserver.class)))
                .thenReturn(createReturningCallback(courses));
        assertNull(TmcUtil.findCourse(mockCore, "not-existing-course"));
    }

    @Test
    public void findExerciseOfCourse() {
        final Course course = new Course("test-course");
        Exercise exercise = new Exercise("second");
        course.setExercises(Arrays.asList(new Exercise("first"), exercise));

        Exercise result = TmcUtil.findExercise(course, "second");
        assertEquals(exercise, result);
    }

    @Test
    public void returnNullIfExerciseWontExist() {
        Course course = new Course("test-course");
        course.setExercises(Arrays.asList(new Exercise("first"), new Exercise("second")));

        assertNull(TmcUtil.findExercise(course, "not-existing-exercise"));
    }

    @Test
    public void downloadSomeExercises() {
        List<Exercise> expectedResult = Arrays.asList(new Exercise("first"),
                new Exercise("second"));

        when(mockCore.downloadOrUpdateExercises(any(ProgressObserver.class),
                anyListOf(Exercise.class)))
                .thenReturn(createReturningCallback(expectedResult));

        List<Exercise> result = TmcUtil.downloadExercises(mockCore, expectedResult,
                new TmcCliProgressObserver(io));
        assertEquals(expectedResult, result);
    }

    @Test
    public void failToDownloadCourses() {
        List<Exercise> exercises = Arrays.asList(new Exercise("first"));
        when(mockCore.downloadOrUpdateExercises(any(ProgressObserver.class),
                eq(exercises))).thenReturn(createThrowingCallback("failed"));

        assertNull(TmcUtil.downloadExercises(mockCore, exercises,
                new TmcCliProgressObserver(io)));
    }

    @Test
    public void submitExercise() {
        Course course = new Course("test-course");
        Exercise exercise = new Exercise("first");
        course.setExercises(Arrays.asList(exercise, new Exercise("second")));
        course.setExercisesLoaded(true);
        final SubmissionResult expectedResult = new SubmissionResult();

        when(mockCore.submit(any(ProgressObserver.class), eq(exercise)))
                .thenReturn(createReturningCallback(expectedResult));

        SubmissionResult result = TmcUtil.submitExercise(mockCore, exercise);
        assertEquals(expectedResult, result);
    }

    @Test
    public void failToSubmitExercise() {
        Exercise exercise = new Exercise("first");
        when(mockCore.submit(any(ProgressObserver.class), eq(exercise)))
                .thenReturn(createThrowingCallback("failed"));

        assertNull(TmcUtil.submitExercise(mockCore, exercise));
    }

    @Test
    public void getUpdatableExercises() {
        final UpdateResult expectedResult = mock(UpdateResult.class);
        Course course = new Course("test-course");

        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), eq(course)))
                .thenReturn(createReturningCallback(expectedResult));

        UpdateResult result = TmcUtil.getUpdatableExercises(mockCore, course);
        assertEquals(expectedResult, result);
    }

    @Test
    public void failToGetUpdatableExercises() {
        Course course = new Course("test-course");
        when(mockCore.getExerciseUpdates(any(ProgressObserver.class), eq(course)))
                .thenReturn(createThrowingCallback("failed"));

        assertNull(TmcUtil.getUpdatableExercises(mockCore, course));
    }

    @Test
    public void sendPaste() throws URISyntaxException {
        final URI expectedResult = new URI("www.abc.org");
        Exercise exercise = new Exercise("test-course");

        when(mockCore.pasteWithComment(any(ProgressObserver.class), eq(exercise),
                eq("message"))).thenReturn(createReturningCallback(expectedResult));

        URI result = TmcUtil.sendPaste(mockCore, exercise, "message");
        assertEquals(expectedResult, result);
    }

    @Test
    public void failToSendPaste() {
        Exercise exercise = new Exercise("test-course");
        when(mockCore.pasteWithComment(any(ProgressObserver.class), eq(exercise),
                eq("message"))).thenReturn(createThrowingCallback("failed"));
        assertNull(TmcUtil.sendPaste(mockCore, exercise, "message"));
    }

    @Test
    public void runLocalTests() {
        final RunResult expectedResult = PowerMockito.mock(RunResult.class);
        Exercise exercise = new Exercise("test-course");

        when(mockCore.runTests(any(ProgressObserver.class), eq(exercise)))
                .thenReturn(createReturningCallback(expectedResult));

        RunResult result = TmcUtil.runLocalTests(mockCore, exercise);
        assertEquals(expectedResult, result);
    }

    @Test
    public void failToRunLocalTests() {
        Exercise exercise = new Exercise("test-course");
        when(mockCore.runTests(any(ProgressObserver.class), eq(exercise)))
                .thenReturn(createThrowingCallback("failed"));
        assertNull(TmcUtil.runLocalTests(mockCore, exercise));
    }

    @Test
    public void sendFeedback() throws URISyntaxException {
        List<FeedbackAnswer> answers = null;
        URI feedbackUri = new URI("www.abc.org");

        when(mockCore.sendFeedback(any(ProgressObserver.class), eq(answers),
                eq(feedbackUri))).thenReturn(createReturningCallback(true));

        assertTrue(TmcUtil.sendFeedback(mockCore, answers, feedbackUri));
    }

    @Test
    public void failToSendFeedback() throws URISyntaxException {
        List<FeedbackAnswer> answers = null;
        URI feedbackUri = new URI("www.abc.org");

        when(mockCore.sendFeedback(any(ProgressObserver.class), eq(answers),
                eq(feedbackUri))).thenReturn(createThrowingCallback("failed"));

        assertFalse(TmcUtil.sendFeedback(mockCore, answers, feedbackUri));
    }
}
