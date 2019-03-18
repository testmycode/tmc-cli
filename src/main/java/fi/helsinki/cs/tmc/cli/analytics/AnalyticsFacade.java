package fi.helsinki.cs.tmc.cli.analytics;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.LoggableEvent;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class AnalyticsFacade {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AnalyticsFacade.class);

    private EventSendBuffer eventSendBuffer;

    public AnalyticsFacade(EventSendBuffer eventSendBuffer) {
        this.eventSendBuffer = eventSendBuffer;
    }

    public void saveAnalytics(String command) {
        LoggableEvent event = LoggableEventCreator.createEvent(command);
        saveEvent(event);
    }

    public void saveAnalytics(String courseName, String command) {
        LoggableEvent event = LoggableEventCreator.createEvent(courseName, command);
        saveEvent(event);
    }

    public void saveAnalytics(Course course, String command) {
        LoggableEvent event = LoggableEventCreator.createEvent(course, command);
        saveEvent(event);
    }

    public void saveAnalytics(Exercise exercise, String command) {
        LoggableEvent event = LoggableEventCreator.createEvent(exercise, command);
        saveEvent(event);
    }

    public Optional<Thread> sendAnalytics() {
        Thread t = new Thread(() -> this.eventSendBuffer.sendNow());
        t.run();
        return Optional.of(t);
    }

    private void saveEvent(LoggableEvent event) {
        this.eventSendBuffer.receiveEvent(event);
        try {
            eventSendBuffer.saveNow(1000);
        } catch (Exception e) {
            logger.warn("Failed to save events: ", e.getStackTrace());
        }
    }
}
