package fi.helsinki.cs.tmc.cli.analytics;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import fi.helsinki.cs.tmc.spyware.LoggableEvent;
import fi.helsinki.cs.tmc.spyware.SpywareSettings;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.security.spec.ECField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class AnalyticsFacade {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AnalyticsFacade.class);

    private EventSendBuffer eventSendBuffer;
    private SpywareSettings settings;

    public AnalyticsFacade(SpywareSettings settings, EventSendBuffer eventSendBuffer) {
        this.settings = settings;
        this.eventSendBuffer = eventSendBuffer;
    }

    public void saveAnalytics(String command) {
        if (!settings.isSpywareEnabled()) {
            return;
        }
        LoggableEvent event = LoggableEventCreator.createEvent(command);
        saveEvent(event);
    }

    public void saveAnalytics(String courseName, String command) {
        if (!settings.isSpywareEnabled()) {
            return;
        }
        LoggableEvent event = LoggableEventCreator.createEvent(courseName, command);
        saveEvent(event);
    }

    public void saveAnalytics(Course course, String command) {
        if (!settings.isSpywareEnabled()) {
            return;
        }
        LoggableEvent event = LoggableEventCreator.createEvent(course, command);
        saveEvent(event);
    }

    public void saveAnalytics(Exercise exercise, String command) {
        if (!settings.isSpywareEnabled()) {
            return;
        }
        LoggableEvent event = LoggableEventCreator.createEvent(exercise, command);
        saveEvent(event);
    }

    private void saveEvent(LoggableEvent event) {
        this.eventSendBuffer.receiveEvent(event);
        try {
            eventSendBuffer.saveNow(1000);
        } catch (Exception e) {
            logger.warn("Failed to save events: ", e.getStackTrace());
        }
    }

    public Optional<Thread> sendAnalytics() {
        if (!settings.isSpywareEnabled()) {
            return Optional.empty();
        }
        Thread t = new Thread(() -> this.eventSendBuffer.sendNow());
        t.run();
        return Optional.of(t);
    }
}
