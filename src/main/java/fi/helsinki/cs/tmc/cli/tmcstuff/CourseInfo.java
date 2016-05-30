package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.util.List;

/**
 * This is a class for storing course information in .tmc.json files.
 */
public class CourseInfo {

    private String username;
    private String serverAddress;
    private String course;
    private List<Exercise> exercises;

    public CourseInfo(TmcSettings settings, String course) {
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

    public String getCourse() {
        return this.course;
    }

    public List<Exercise> getExercises() {
        return this.exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}
