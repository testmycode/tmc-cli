package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ExerciseUpdaterTest {

    TmcCore tmcCore;
    ExerciseUpdater exerciseUpdater;

    @Before
    public void setUp() {
        tmcCore = mock(TmcCore.class);

        exerciseUpdater = new ExerciseUpdater(tmcCore, new Course());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void constructorWorks() {
        assertTrue(exerciseUpdater.getNewExercises().isEmpty());
        assertTrue(exerciseUpdater.getUpdatedExercises().isEmpty());
    }

    @Test
    public void basicStuffWorks() {
        assertFalse(exerciseUpdater.newExercisesAvailable());
        assertFalse(exerciseUpdater.updatedExercisesAvailable());

        List<Exercise> newExercises = new ArrayList<>();
        List<Exercise> updatedExercises = new ArrayList<>();

        Exercise newExercise = new Exercise("new");
        Exercise updatedExercise = new Exercise("updated");

        newExercises.add(newExercise);
        updatedExercises.add(updatedExercise);

        exerciseUpdater.setNewExercises(newExercises);
        exerciseUpdater.setUpdatedExercises(updatedExercises);

        assertTrue(exerciseUpdater.newExercisesAvailable());
        assertTrue(exerciseUpdater.updatedExercisesAvailable());

        assertTrue(exerciseUpdater.getNewAndUpdatedExercises().contains(newExercise));
        assertTrue(exerciseUpdater.getNewAndUpdatedExercises().contains(updatedExercise));
    }

    @Test
    public void basicStuffWorks1() {
        Callable<UpdateResult> result = new Callable<UpdateResult>() {
            @Override
            public UpdateResult call() throws Exception {
                UpdateResult result = mock(UpdateResult.class);
                List<Exercise> exercises = new ArrayList<>();
                exercises.add(new Exercise());
                when(result.getNewExercises()).thenReturn(exercises);
                when(result.getUpdatedExercises()).thenReturn(exercises);
                return result;
            }
        };
        when(tmcCore.getExerciseUpdates(any(ProgressObserver.class), any(Course.class)))
                .thenReturn(result);

        assertTrue(exerciseUpdater.updatesAvailable());
    }
}
