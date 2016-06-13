package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
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
    private HashMap<String, String> properties;

    public CourseInfo(TmcSettings settings, Course course) {
        this.username = settings.getUsername();
        this.serverAddress = settings.getServerAddress();
        this.course = course;
        this.properties = new HashMap<>();
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
        return this.course.getExercises();
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
