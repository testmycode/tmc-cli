package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is a class for storing course information in .tmc.json files.
 */
public class CourseInfo {

    private String username;
    private String serverAddress;
    private Course course;
    private List<String> localCompletedExercises;
    private HashMap<String, String> properties;

    public CourseInfo(Account account, Course course) {
        this.username = account.getUsername();
        this.serverAddress = account.getServerAddress();
        this.course = course;
        this.properties = new HashMap<>();
        this.localCompletedExercises = new ArrayList<>();
    }

    public String getUsername() {
        return this.username;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public String getCourseName() {
        return this.course.getName();
    }

    public Course getCourse() {
        return this.course;
    }

    public List<String> getLocalCompletedExercises() {
        // Check for null pointer in case of old .tmc.json files
        // Remove this when we are sure nobody's using 0.5.1 anymore
        if (this.localCompletedExercises == null) {
            this.localCompletedExercises = new ArrayList<String>();
        }
        return this.localCompletedExercises;
    }

    public List<Exercise> getExercises() {
        return this.course.getExercises();
    }

    /**
     * Get a list of exercises by their names.
     */
    public List<Exercise> getExercises(List<String> exerciseNames) {
        List<Exercise> exercises = new ArrayList<>();
        for (String exerciseName : exerciseNames) {
            Exercise exercise = getExercise(exerciseName);
            if (exercise != null) {
                exercises.add(exercise);
            }
        }
        return exercises;
    }

    public List<String> getExerciseNames() {
        List<String> names = new ArrayList<>();
        for (Exercise ex : this.course.getExercises()) {
            names.add(ex.getName());
        }
        return names;
    }

    public Exercise getExercise(String name) {
        for (Exercise exercise : this.course.getExercises()) {
            if (exercise.getName().equals(name)) {
                return exercise;
            }
        }
        return null;
    }

    public void setExercises(List<Exercise> exercises) {
        this.course.setExercises(exercises);
    }

    /**
     * Replaces an old identically named exercise with a new one. Adds if no old exercise is found.
     */
    public void replaceOldExercise(Exercise newExercise) {
        List<Exercise> exercises = getExercises();
        String exerciseName = newExercise.getName();

        Exercise oldExercise = null;
        for (Exercise exercise : exercises) {
            if (exercise.getName().equals(exerciseName)) {
                oldExercise = exercise;
                break;
            }
        }

        if (oldExercise == null) {
            exercises.add(newExercise);
        } else {
            int index = exercises.indexOf(oldExercise);
            exercises.set(index, newExercise);
        }

        setExercises(exercises);
    }

    public void replaceOldExercises(List<Exercise> newExercises) {
        for (Exercise newExercise : newExercises) {
            replaceOldExercise(newExercise);
        }
    }

    public void removeProperty(String prop) {
        this.properties.remove(prop);
    }

    public void setProperty(String prop, String value) {
        if (value != null) {
            this.properties.put(prop, value);
        } else {
            this.properties.remove(prop);
        }
    }

    public void setProperty(String prop, int value) {
        this.properties.put(prop, Integer.toString(value));
    }

    public String getPropertyString(String prop) {
        return this.properties.get(prop);
    }

    public int getPropertyInt(String prop) {
        return Integer.parseInt(this.properties.get(prop));
    }
}
