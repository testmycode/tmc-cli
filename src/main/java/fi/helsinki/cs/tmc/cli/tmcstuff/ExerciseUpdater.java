package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExerciseUpdater {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExerciseUpdater.class);

    private final TmcCore core;
    private final Course course;

    private List<Exercise> newExercises;
    private List<Exercise> updatedExercises;

    public ExerciseUpdater(TmcCore core, Course course) {
        this.core = core;
        this.course = course;

        this.newExercises = new ArrayList<>();
        this.updatedExercises = new ArrayList<>();
    }

    public List<Exercise> getNewExercises() {
        return newExercises;
    }

    public List<Exercise> getUpdatedExercises() {
        return updatedExercises;
    }

    public List<Exercise> getNewAndUpdatedExercises() {
        List<Exercise> list = new ArrayList<>();
        list.addAll(newExercises);
        list.addAll(updatedExercises);
        return list;
    }

    protected void setNewExercises(List<Exercise> newExercises) {
        this.newExercises = newExercises;
    }

    protected void setUpdatedExercises(List<Exercise> updatedExercises) {
        this.updatedExercises = updatedExercises;
    }

    public boolean newExercisesAvailable() {
        return !newExercises.isEmpty();
    }

    public boolean updatedExercisesAvailable() {
        return !updatedExercises.isEmpty();
    }

    /**
     * Asks tmc-core if there are new or updated exercises available. Call this
     * before other methods.
     *
     * @return true if there is something new to download, false if not.
     */
    public boolean updatesAvailable() {
        UpdateResult result = TmcUtil.getUpdatableExercises(core, course);
        if (result == null) {
            return false;
        }

        newExercises = result.getNewExercises();
        updatedExercises = result.getUpdatedExercises();

        return newExercisesAvailable() || updatedExercisesAvailable();
    }

    public List<Exercise> downloadUpdates(TmcCliProgressObserver progobs) {
        List<Exercise> newAndUpdated = getNewAndUpdatedExercises();
        for (Iterator<Exercise> iterator = newAndUpdated.iterator(); iterator.hasNext();) {
            Exercise next = iterator.next();
            if (next.isCompleted()) {
                iterator.remove();
            }
        }
        return TmcUtil.downloadExercises(core, newAndUpdated, progobs);
    }

    public boolean updateCourseJson(CourseInfo info, Path configFile) {
        Course newDetailsCourse = TmcUtil.findCourse(core, course.getName());
        if (newDetailsCourse == null) {
            return false;
        }
        info.setExercises(newDetailsCourse.getExercises());
        return CourseInfoIo.save(info, configFile);
    }
}
