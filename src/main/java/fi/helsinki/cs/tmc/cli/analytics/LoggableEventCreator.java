package fi.helsinki.cs.tmc.cli.analytics;

import com.google.gson.Gson;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.snapshots.LoggableEvent;

import java.nio.charset.Charset;
import java.util.Collections;

public class LoggableEventCreator {

    public static LoggableEvent createEvent(String command) {
        byte[] jsonBytes = getBytes(command);
        return new LoggableEvent("project_action", jsonBytes);
    }

    public static LoggableEvent createEvent(String courseName, String command) {
        byte[] jsonBytes = getBytes(command);
        return new LoggableEvent(courseName, "", "project_action", jsonBytes);
    }

    public static LoggableEvent createEvent(Course course, String command) {
        byte[] jsonBytes = getBytes(command);
        return new LoggableEvent(course, "project_action", jsonBytes);
    }

    public static LoggableEvent createEvent(Exercise exercise, String command) {
        byte[] jsonBytes = getBytes(command);
        return new LoggableEvent(exercise, "project_action", jsonBytes);
    }

    private static byte[] getBytes(String command) {
        Object data = Collections.singletonMap("command", command);
        String json = new Gson().toJson(data);
        return json.getBytes(Charset.forName("UTF-8"));
    }
}
