package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.util.List;

/**
 * This is a class for storing course information in .tmc.json files.
 */
public class CourseInfo {

    private String username;
    private String serverAddress;
    private Course course;
    private List<Exercise> exercises;

    public CourseInfo(TmcSettings settings, Course course) {
        this.username = settings.getUsername();
        this.serverAddress = settings.getServerAddress();
        this.course = course;
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

    public List<Exercise> getExercises() {
        return this.exercises;
    }

    public Exercise getExercise(String name) {
        for (Exercise exercise : this.exercises) {
            if (exercise.getName().equals(name)) {
                return exercise;
            }
        }
        return null;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}
