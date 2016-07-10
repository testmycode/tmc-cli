package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CourseInfoTest {

    private CourseInfo courseInfo;

    @Before
    public void setUp() {
        courseInfo = new CourseInfo(new Account(), new Course("test-course"));
    }

    @Test
    public void replaceOldExerciseMethodAddsIfNoOldIsFound() {
        assertTrue(courseInfo.getExercises().isEmpty());
        courseInfo.replaceOldExercise(new Exercise("test-exercise", "test-course"));
        assertEquals(1, courseInfo.getExercises().size());
    }

    @Test
    public void replacesOldExercise() {
        Exercise oldEx = new Exercise("test-exercise", "test-course");
        oldEx.setCompleted(false);

        Exercise newEx = new Exercise("test-exercise", "test-course");
        newEx.setCompleted(true);

        courseInfo.getExercises().add(oldEx);
        assertFalse(courseInfo.getExercise("test-exercise").isCompleted());
        courseInfo.replaceOldExercise(newEx);
        assertTrue(courseInfo.getExercise("test-exercise").isCompleted());

        assertEquals(1, courseInfo.getExercises().size());
    }

    @Test
    public void replacesAllOldExercises() {
        Exercise oldEx1 = new Exercise("test-exercise1", "test-course");
        oldEx1.setCompleted(false);
        Exercise oldEx2 = new Exercise("test-exercise2", "test-course");
        oldEx2.setCompleted(false);

        Exercise newEx1 = new Exercise("test-exercise1", "test-course");
        newEx1.setCompleted(true);
        Exercise newEx2 = new Exercise("test-exercise2", "test-course");
        newEx2.setCompleted(true);

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(newEx1);
        exercises.add(newEx2);

        courseInfo.getExercises().add(oldEx1);
        courseInfo.getExercises().add(oldEx2);
        assertFalse(courseInfo.getExercise("test-exercise1").isCompleted());
        assertFalse(courseInfo.getExercise("test-exercise2").isCompleted());
        courseInfo.replaceOldExercises(exercises);
        assertTrue(courseInfo.getExercise("test-exercise1").isCompleted());
        assertTrue(courseInfo.getExercise("test-exercise2").isCompleted());

        assertEquals(2, courseInfo.getExercises().size());
    }

    @Test
    public void canGetListOfExercisesByTheirNames() {
        Exercise ex1 = new Exercise("test-exercise1", "test-course");
        Exercise ex2 = new Exercise("test-exercise2", "test-course");
        Exercise ex3 = new Exercise("test-exercise3", "test-course");

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(ex1);
        exercises.add(ex2);
        exercises.add(ex3);
        courseInfo.setExercises(exercises);

        List<String> names = new ArrayList<>();
        names.add(ex1.getName());
        names.add(ex3.getName());

        List<Exercise> got = courseInfo.getExercises(names);
        assertTrue(got.contains(ex1));
        assertTrue(got.contains(ex3));
        assertEquals(2, got.size());
    }

    @Test
    public void canGetListOfExerciseNames() {
        Exercise ex1 = new Exercise("test-exercise1", "test-course");
        Exercise ex2 = new Exercise("test-exercise2", "test-course");

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(ex1);
        exercises.add(ex2);

        courseInfo.setExercises(exercises);

        List<String> got = courseInfo.getExerciseNames();

        assertEquals(got.get(0), "test-exercise1");
        assertEquals(got.get(1), "test-exercise2");
    }
}
