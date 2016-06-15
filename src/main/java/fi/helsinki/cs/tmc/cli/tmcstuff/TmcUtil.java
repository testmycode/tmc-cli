package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.core.TmcCore;
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
        return new ArrayList<Course>();
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

    public static List<Exercise> downloadExercises(TmcCore core, List<Exercise> exercises) {
        try {
            return core.downloadOrUpdateExercises(new TmcCliProgressObserver(), exercises)
                    .call();
        } catch (Exception e) {
            logger.warn("Failed to download exercises", e);
            return new ArrayList<>();
        }
    }

    public static List<Exercise> downloadAllExercises(TmcCore core, Course course) {
        if (!course.isExercisesLoaded()) {
            course = getDetails(core, course);
        }
        List<Exercise> exercises = course.getExercises();
        return downloadExercises(core, exercises);
    }

    public static SubmissionResult submitExercise(TmcCore core, Course course, String name) {
        // Exercise has directories separated with a - but core needs them to be separated with a / for submission
        Exercise exercise = TmcUtil.findExercise(course, name);
        //String fixedName = exercise.getName().replace("-", File.separator);
        //exercise.setName(fixedName);
        try {
            return core.submit(new TmcCliProgressObserver(),
                    exercise).call();
        } catch (Exception e) {
            logger.warn("Failed to submit the exercise", e);
            return null;
        }
    }
}
