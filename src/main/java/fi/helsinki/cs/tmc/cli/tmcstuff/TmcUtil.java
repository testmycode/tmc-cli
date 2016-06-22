package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.commands.GetUpdatableExercises.UpdateResult;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TmcUtil {

    private static final Logger logger = LoggerFactory.getLogger(TmcUtil.class);

    public static List<Course> listCourses(TmcCore core) {
        Callable<List<Course>> callable;
        callable = core.listCourses(ProgressObserver.NULL_OBSERVER);

        try {
            return callable.call();
        } catch (Exception e) {
            TmcUtil.logger.warn("Failed to get courses to list the exercises", e);
        }
        return new ArrayList<>();
    }

    public static Course getDetails(TmcCore core, Course course) {
        try {
            return core.getCourseDetails(ProgressObserver.NULL_OBSERVER, course).call();
        } catch (Exception e) {
            logger.warn("Failed to get course details to list the exercises", e);
            return null;
        }
    }

    public static Course findCourse(TmcCore core, String name) {
        List<Course> courses;
        courses = TmcUtil.listCourses(core);

        for (Course item : courses) {
            if (item.getName().equals(name)) {
                return TmcUtil.getDetails(core, item);
            }
        }
        return null;
    }

    public static Exercise findExercise(Course course, String name) {
        List<Exercise> exercises;
        exercises = course.getExercises();

        for (Exercise item : exercises) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public static int findExercise(List<Exercise> exercises, String name) {
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static List<Exercise> downloadExercises(TmcCore core, List<Exercise> exercises,
            ProgressObserver progobs) {
        try {
            return core.downloadOrUpdateExercises(progobs, exercises).call();
        } catch (Exception e) {
            logger.warn("Failed to download exercises", e);
            return new ArrayList<>();
        }
    }

    public static List<Exercise> downloadAllExercises(TmcCore core, Course course,
            ProgressObserver progobs) {
        if (!course.isExercisesLoaded()) {
            course = getDetails(core, course);
        }
        List<Exercise> exercises = course.getExercises();
        return downloadExercises(core, exercises, progobs);
    }

    public static SubmissionResult submitExercise(TmcCore core, Course course, String name) {
        Exercise exercise = TmcUtil.findExercise(course, name);
        return submitExercise(core, exercise);
    }

    public static SubmissionResult submitExercise(TmcCore core, Exercise exercise) {
        try {
            return core.submit(ProgressObserver.NULL_OBSERVER, exercise).call();
        } catch (Exception ex) {
            logger.warn("Failed to submit the exercise", ex);
            return null;
        }
    }

    public static UpdateResult getUpdatableExercises(
            TmcCore core, Course course) {
        try {
            return core.getExerciseUpdates(ProgressObserver.NULL_OBSERVER, course)
                    .call();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
