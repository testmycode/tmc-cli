package fi.helsinki.cs.tmc.cli.shared;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class ExerciseUpdaterTest {

    private CliContext ctx;
    private TmcCore mockCore;
    private ExerciseUpdater exerciseUpdater;

    @Before
    public void setUp() {
        mockCore = mock(TmcCore.class);
        ctx = new CliContext(new TestIo(), mockCore);

        mockStatic(TmcUtil.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void noUpdatesAfterConstructor() {
        exerciseUpdater = new ExerciseUpdater(ctx, new Course());
        assertFalse(exerciseUpdater.newExercisesAvailable());
        assertFalse(exerciseUpdater.updatedExercisesAvailable());
    }

    @Test
    public void updateListsAreEmptyAfterConstructor() {
        exerciseUpdater = new ExerciseUpdater(ctx, new Course());
        assertTrue(exerciseUpdater.getNewExercises().isEmpty());
        assertTrue(exerciseUpdater.getUpdatedExercises().isEmpty());
    }

    @Test
    public void updatesAvailableAfterSetters() {
        List<Exercise> newExercises = Arrays.asList(new Exercise("new"));
        List<Exercise> updatedExercises = Arrays.asList(new Exercise("updated"));

        exerciseUpdater = new ExerciseUpdater(ctx, new Course());
        exerciseUpdater.setNewExercises(newExercises);
        exerciseUpdater.setUpdatedExercises(updatedExercises);

        assertTrue(exerciseUpdater.newExercisesAvailable());
        assertTrue(exerciseUpdater.updatedExercisesAvailable());

        assertTrue(exerciseUpdater.getNewAndUpdatedExercises().contains(
                newExercises.get(0)));
        assertTrue(exerciseUpdater.getNewAndUpdatedExercises().contains(
                updatedExercises.get(0)));
    }

    @Test
    public void worksWithActualResultObject() {
        UpdateResult result = mock(UpdateResult.class);
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise());

        when(result.getNewExercises()).thenReturn(exercises);
        when(result.getUpdatedExercises()).thenReturn(exercises);
        when(TmcUtil.getUpdatableExercises(eq(ctx), any(Course.class)))
                .thenReturn(result);

        exerciseUpdater = new ExerciseUpdater(ctx, new Course());
        assertTrue(exerciseUpdater.updatesAvailable());
    }
}
