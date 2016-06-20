package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ListExercisesCommandTest {
    
    private static final String COURSE_NAME = "2016-aalto-c";
    static Path pathToDummyCourse;
    static Path pathToNonCourseDir;

    Application app;
    TestIo io;
    TmcCore mockCore;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/" + COURSE_NAME).toURI());
        assertNotNull(pathToDummyCourse);

        pathToNonCourseDir = pathToDummyCourse.getParent();
        assertNotNull(pathToNonCourseDir);
    }
    
    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        doAnswer(new Answer<Callable<Course>>() {
            @Override
            public Callable<Course> answer(InvocationOnMock invocation) throws Throwable {
                final Course course = (Course) invocation.getArguments()[1];
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
    public void failIfCoreIsNull() {
        app = spy(app);
        doReturn(null).when(app).getTmcCore();

        String[] args = {"exercises", "-n", "foo", "-i"};
        app.run(args);
        assertFalse(io.getPrint().contains("Course 'foo' doesn't exist"));
    }

    @Test
    public void worksLocallyIfNotInCourseDirectoryAndCourseIsSpecified() {
        app.setWorkdir(new WorkDir(pathToNonCourseDir));
        String[] args = {"exercises", "fooCourse", "-n"};
        app.run(args);
        assertThat(io.out(), containsString("You have to be in a course directory or use the -i"));
    }
    
    @Test
    public void worksLocallyIfInCourseDirectoryAndRightCourseIsSpecified() {
        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"exercises", COURSE_NAME, "-n"};
        app.run(args);
        assertThat(io.out(), containsString("Deadline:"));
    }
    
    @Test
    public void worksLocallyIfInCourseDirectoryAndCourseIsNotSpecified() {
        app.setWorkdir(new WorkDir(pathToDummyCourse));
        String[] args = {"exercises", "-n"};
        app.run(args);
        assertThat(io.out(), containsString("Deadline:"));
    }

    @Test
    public void giveMessageIfNoExercisesOnCourse() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(new Course("test-course123"));
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);

        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("have any exercises"));
    }

    @Test
    public void listExercisesGivesCorrectExercises() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);

        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("hello-exerciseNames"));
    }

    @Test
    public void emptyArgsGivesAnErrorMessage() {
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(null);
        String[] args = {"exercises", "-n"};
        app.run(args);
        assertThat(io.out(), containsString("No course specified"));
    }

    @Test
    public void failIfCourseDoesNotExist() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return Arrays.asList(new Course("hello-exerciseNames"));
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);

        String[] args = {"exercises", "-n", "abc", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Course 'abc' doesn't exist"));
    }

    @Test
    public void exerciseIsCompletedButRequiresReview() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));
                list.get(1).setRequiresReview(true);
                list.get(1).setReviewed(false);
                list.get(1).setCompleted(true);

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Requires review"));
    }

    @Test
    public void exerciseIsCompletedAndReviewed() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));
                list.get(1).setRequiresReview(true);
                list.get(1).setReviewed(true);
                list.get(1).setCompleted(true);

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Completed"));
    }

    @Test
    public void exerciseIsCompleted() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));
                list.get(1).setRequiresReview(false);
                list.get(1).setCompleted(true);

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Completed"));
    }

    @Test
    public void exerciseIsNotCompleted() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Not completed"));
    }

    @Test
    public void exerciseHasBeenAttempted() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));
                list.get(1).setAttempted(true);

                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Attempted"));
    }

    @Test
    public void exerciseDeadLinePassed() {
        Callable<List<Course>> callable = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                List<Exercise> list = Arrays.asList(
                        new Exercise("hello-exerciseNames"),
                        new Exercise("cool-exerciseNames"));
                list.get(1).setDeadline("2014-09-10T14:00:00.000+03:00");
                Course course = new Course("test-course123");
                course.setExercises(list);

                return Arrays.asList(course);
            }
        };
        when(mockCore.listCourses(any(ProgressObserver.class))).thenReturn(callable);
        String[] args = {"exercises", "-n", "test-course123", "-i"};
        app.run(args);
        assertThat(io.out(), containsString("Deadline passed"));
    }
}
