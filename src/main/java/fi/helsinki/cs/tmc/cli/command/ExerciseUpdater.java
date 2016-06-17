package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExerciseUpdater {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExerciseUpdater.class);

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

    public void setNewExercises(List<Exercise> newExercises) {
        this.newExercises = newExercises;
    }

    public void setUpdatedExercises(List<Exercise> updatedExercises) {
        this.updatedExercises = updatedExercises;
    }

    public boolean newExercisesAvailable() {
        return !newExercises.isEmpty();
    }

    public boolean updatedExercisesAvailable() {
        return !updatedExercises.isEmpty();
    }

    public boolean updatesAvailable() {
        UpdateResult result;
        try {
            result = core.getExerciseUpdates(ProgressObserver.NULL_OBSERVER, course).call();
        } catch (Exception ex) {
            LOGGER.warn("Failed to get exercise updates.", ex);
            return false;
        }

        if (result == null) {
            return false;
        }

        newExercises = result.getNewExercises();
        updatedExercises = result.getUpdatedExercises();

        return newExercisesAvailable() || updatedExercisesAvailable();
    }

    public List<Exercise> downloadUpdates() {
        return TmcUtil.downloadExercises(core, getNewAndUpdatedExercises());
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
